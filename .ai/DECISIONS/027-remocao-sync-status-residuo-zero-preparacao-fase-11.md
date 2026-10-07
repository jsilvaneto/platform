# ADR 027: Remoção do Campo syncStatus, Política de Resíduo Zero e Desenho Especializado da Fase 11

## Status
Aprovado

## Contexto
O campo `syncStatus: String = "PENDENTE"` persistia como resquício histórico em apenas 4 modelos de dados:
- `Category` / `CategoryEntity` (`categories`)
- `ExpenseItem` / `ExpenseItemEntity` (`expense_items`)
- `CreditCard` / `CreditCardEntity` (`credit_cards`)
- `CreditCardInvoice` / `CreditCardInvoiceEntity` (`credit_card_invoices`)

Em contrapartida, as entidades fundamentais do aplicativo (`Bill`, `BillInstallment`, `Contact`, `FinancialAccount`, `Goal`, `Budget`, `PaymentMethod`) não possuíam qualquer menção a `syncStatus`.

Uma análise abrangente da base de código confirmou que:
1. Nenhuma tela (Compose), ViewModel, Caso de Uso (`UseCase`), Repositório ou DAO realizava leituras, filtros condicionais ou mutações com base em `syncStatus`.
2. O campo era exportado estaticamente em backups JSON (`BackupDataDto`), gerando redundância sem propósito funcional.
3. Tratava-se de um artefato de implementação incompleta pré-Fase 11, introduzido de forma dispersa nas migrações históricas v5 e v6.

Diante do planejamento da **Fase 11 (Sincronização em Nuvem via API Bidirecional + Postgres)** documentada em `.ai/STATUS.md`, surgiram duas abordagens:
- **Opção (a)**: Remover `syncStatus` de todas as entidades agora, aplicando o princípio de Resíduo Zero, e conceber a arquitetura de sincronização do zero e com rigor técnico na Fase 11.
- **Opção (b)**: Padronizar o campo em todas as entidades sincronizáveis através de um enum estrito `SyncStatus` como preparação antecipada.

## Decisão

Adotada a **Opção (a)**: Remoção integral do campo `syncStatus` de todos os modelos de domínio e entidades Room, com migração de banco de dados física v14 $\rightarrow$ v15.

### Fundamentação Técnica

1. **Princípio do Resíduo Zero (`.agents/rules/test_data_cleanup.md`)**:
   O projeto veda a permanência de código morto, campos zumbis e resíduos de dados sem uso real. Manter `syncStatus` transmitia uma falsa premissa de prontidão de sincronização.

2. **Rejeição a Arquitetura Especulativa (YAGNI - You Aren't Gonna Need It)**:
   A sincronização offline-first bidirecional moderna em nível de produção (Fase 11) não se apoia em uma simples flag estática `syncStatus` dentro de cada linha da tabela. Ela requer um protocolo robusto composto por:
   - **Tabela de Outbox / Fila de Mutações (Sync Queue)**: Registro transacional ordenado das operações de criação, atualização e exclusão, garantindo entrega garantida e idempotência.
   - **Rastreamento de Exclusões (Tombstones / Soft Delete)**: Ao excluir um registro localmente, um simples campo `syncStatus` no registro apagado seria destruído no SQLite local e a API remota nunca seria notificada da deleção.
   - **Controle Concorrente e Resolução de Conflitos**: Vetores de versão, marcas temporais imutáveis (`updatedAt`), hashes ou carimbos de auditoria.
   - **Identificadores Universais Estáveis**: Estratégia de mapeamento entre IDs locais e universais (UUIDs).
   Antecipar um enum `SyncStatus` isolado em 10 entidades (Opção b) geraria código descartável que inevitavelmente seria substituído ou reestruturado no momento da implementação da Fase 11.

3. **Pureza da Camada de Domínio (`architecture.md`)**:
   Entidades de domínio puro (`domain/model/`) não devem carregar detalhes de implementação de transporte ou infraestrutura não suportados pelas regras de negócio locais.

4. **Higiene e Tamanho de Backups**:
   A remoção de `syncStatus` elimina nós repetitivos vazios nos arquivos de backup JSON locais protegidos por senha.

### Implementação da Migração Room v14 -> v15

Foi incrementada a versão do banco em [PlatformDatabase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/PlatformDatabase.kt) para `version = 15`.

A migração `MIGRATION_14_15` foi desenhada para garantir retrocompatibilidade universal com qualquer versão do SQLite nativo do Android, executando recriação atômica com isolamento de integridade referencial:
```kotlin
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")

        // 1. categories: expurgo de syncStatus
        db.execSQL("CREATE TABLE IF NOT EXISTS categories_new (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, colorHex TEXT NOT NULL, iconName TEXT NOT NULL, nature TEXT NOT NULL DEFAULT 'NECESSARIO')")
        db.execSQL("INSERT INTO categories_new (id, name, colorHex, iconName, nature) SELECT id, name, colorHex, iconName, nature FROM categories")
        db.execSQL("DROP TABLE categories")
        db.execSQL("ALTER TABLE categories_new RENAME TO categories")

        // 2. expense_items: expurgo de syncStatus e reconstrução de FK/índices
        db.execSQL("CREATE TABLE IF NOT EXISTS expense_items_new (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, categoryId TEXT NOT NULL, FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT)")
        db.execSQL("INSERT INTO expense_items_new (id, name, categoryId) SELECT id, name, categoryId FROM expense_items")
        db.execSQL("DROP TABLE expense_items")
        db.execSQL("ALTER TABLE expense_items_new RENAME TO expense_items")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_items_categoryId ON expense_items(categoryId)")

        // 3. credit_cards: expurgo de syncStatus
        db.execSQL("CREATE TABLE IF NOT EXISTS credit_cards_new (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, totalLimitCents INTEGER NOT NULL, closingDay INTEGER NOT NULL, dueDay INTEGER NOT NULL, colorHex TEXT NOT NULL DEFAULT '#3B82F6')")
        db.execSQL("INSERT INTO credit_cards_new (id, name, totalLimitCents, closingDay, dueDay, colorHex) SELECT id, name, totalLimitCents, closingDay, dueDay, colorHex FROM credit_cards")
        db.execSQL("DROP TABLE credit_cards")
        db.execSQL("ALTER TABLE credit_cards_new RENAME TO credit_cards")

        // 4. credit_card_invoices: expurgo de syncStatus e reconstrução de FK/índices
        db.execSQL("CREATE TABLE IF NOT EXISTS credit_card_invoices_new (id TEXT NOT NULL PRIMARY KEY, creditCardId TEXT NOT NULL, referenceMonth TEXT NOT NULL, closingDate INTEGER NOT NULL, dueDate INTEGER NOT NULL, status TEXT NOT NULL DEFAULT 'ABERTA', FOREIGN KEY(creditCardId) REFERENCES credit_cards(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO credit_card_invoices_new (id, creditCardId, referenceMonth, closingDate, dueDate, status) SELECT id, creditCardId, referenceMonth, closingDate, dueDate, status FROM credit_card_invoices")
        db.execSQL("DROP TABLE credit_card_invoices")
        db.execSQL("ALTER TABLE credit_card_invoices_new RENAME TO credit_card_invoices")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_invoices_creditCardId ON credit_card_invoices(creditCardId)")

        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
```

## Consequências

### Positivas
- Base de dados e modelos 100% livres de campos fantasmas ou assimetrias.
- Backups mais concisos e limpos.
- Terreno completamente desobstruído para modelar o protocolo formal de sincronização da Fase 11 (Outbox, Tombstones e API REST/gRPC) sem vícios legados.

### Negativas / Mitigações
- Requer migração estrutural nas 4 tabelas envolvidas. Mitigada através de testes unitários automatizados cobrindo a migração Room e transação atômica protegida com `PRAGMA foreign_keys`.
