# ADR 044: Política de Conflito, Modelo Outbox Transacional e Soft Delete Consistente

## Contexto e Motivação
Na evolução da arquitetura do aplicativo Platform (100% offline-first), identificou-se divergência entre a especificação declarada e a implementação real de persistência e sincronização:
1. **Exclusões Físicas Dispersas e Ausência de Rastreamento**: Diversas tabelas realizavam `DELETE` físico direto (32 ocorrências mapeadas), impedindo que deleções fossem propagadas ao backend durante o sync.
2. **Defasagem Temporal**: Metadados de sincronização (`updatedAt` e `deletedAt`) existiam apenas em parte das entidades, gerando lacunas que impossibilitavam o cálculo de deltas incrementais.
3. **Visibilidade Inconsistente de Recorrências Excluídas**: Contas recorrentes excluídas com preservação de histórico continuavam visíveis na listagem principal como "Encerrada" porque consultas como `BillDao.getAll()` não filtravam `deletedAt IS NULL`.
4. **Campos Só-Data em Epoch Milliseconds**: Campos como vencimento (`dueDate`) e fechamento de fatura (`closingDate`) eram gravados em milissegundos UTC/local, suscetíveis a bugs de virada de mês e deslocamentos em diferentes fusos horários.

## Decisões Tomadas

### 1. Modelo de Entidades Sincronizáveis Padronizado
Todas as 12 entidades sincronizáveis da aplicação passam a conter obrigatoriamente:
- `createdAt: Long`: timestamp de criação em milissegundos UTC.
- `updatedAt: Long`: timestamp da última mutação em milissegundos UTC.
- `deletedAt: Long?`: timestamp de exclusão lógica (soft delete) em milissegundos UTC (`null` para entidades ativas).

Tabelas sincronizáveis:
- `bills`
- `bill_installments`
- `budgets`
- `categories`
- `contacts`
- `credit_cards`
- `credit_card_invoices`
- `expense_items`
- `financial_accounts`
- `goals`
- `goal_contributions`
- `payments`

### 2. Guard Rail Automatizado de Consultas (`SynchronizableQueriesGuardTest`)
Implementado teste unitário automatizado por reflexão que varre 100% dos métodos `@Query` em todos os DAOs de tabelas sincronizáveis. O teste falha caso qualquer query de leitura, cálculo de totais, agregação ou mutação omita a cláusula `deletedAt IS NULL`.
Exceções estritas e validadas:
- Queries explícitas de purga pós-sincronização (`WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis`).
- Queries de reset e isolamento de testes unitários (`DELETE FROM <table>` sem predicados).

### 3. Padrão Outbox Transacional (`sync_outbox`)
Toda alteração de estado no banco de dados local (inclusão, alteração de valores/status ou exclusão lógica) grava atomicamente na tabela `sync_outbox` dentro da mesma transação Room (`database.withTransaction`):
- `id`: UUID v4 único da operação.
- `entityType`: Nome da entidade (ex: `"BILL"`, `"BILL_INSTALLMENT"`, `"PAYMENT"`).
- `entityId`: Identificador da entidade afetada.
- `operation`: `"INSERT"`, `"UPDATE"`, `"DELETE"` ou `"UPSERT"`.
- `payload`: Snapshot JSON serializado da mutação.
- `status`: `"PENDING"` | `"SYNCED"` | `"FAILED"`.
- `createdAt`: Timestamp UTC da operação.

### 4. Política de Conflito de Sincronização (Sync Conflict Resolution)
Para a sincronização bidirecional entre clientes móveis offline e o futuro backend:
- **Estratégia Base**: *Last-Write-Wins (LWW)* fundamentada estritamente no timestamp `updatedAt` em milissegundos UTC gerado no momento da mutação.
- **Precedência de Soft Delete**: Se uma entidade receber uma atualização concorrente mas possuir `deletedAt >= updatedAt`, a exclusão prevalece.
- **Desempate Determinístico**: Em caso de timestamps de `updatedAt` milissegundo a milissegundo idênticos entre cliente e servidor, adota-se ordenação lexicográfica estável de UUID da entidade (`entityId`).
- **Purga Atômica (`PurgeSynchronizedDataUseCase`)**: Exclusão física definitiva (`DELETE`) de registros marcados com `deletedAt` só ocorre após confirmação formal do servidor (status `SYNCED` na outbox) ou decorrida uma janela segura de retenção padrão de 30 dias.

### 5. Distinção Rígida: "Excluir Recorrência" vs. "Encerrar Recorrência"
- **Excluir Recorrência**:
  - Marca a conta com `deletedAt = now` e suas parcelas futuras com `deletedAt = now`.
  - A conta **desaparece completamente** das listagens operacionais e de contas ativas (`WHERE deletedAt IS NULL`).
  - Parcelas passadas já quitadas (`paidAt IS NOT NULL`) mantêm `deletedAt = null`, garantindo que o histórico financeiro permaneça fidedigno nos relatórios e resumos mensais através da regra `(b.deletedAt IS NULL OR i.paidAt IS NOT NULL)`.
- **Encerrar Recorrência**:
  - Mantém a conta ativa no banco (`deletedAt = null`), registrando `endedAt = now` e `recurrenceEndType = 'UNTIL_DATE'`.
  - A conta continua listada na aba Contas com o selo e status "Encerrada".

### 6. Migração de Campos Só-Data para `LocalDate`
- Campos que representam exclusivamente datas do calendário civil (`dueDate` em `bill_installments`, `closingDate` e `dueDate` em `credit_card_invoices`) foram migrados de milissegundos para `java.time.LocalDate`.
- Na camada de persistência SQLite, são armazenados como inteiros de dias desde o epoch (`INTEGER`, via `LocalDate.toEpochDay()`), convertidos transparentemente pelo `LocalDateEpochDayConverter`.
- A migração `MIGRATION_19_20` converte dados existentes através de `CAST(coluna / 86400000 AS INTEGER)`.

## Consequências e Benefícios
- **Integridade Offline-First Total**: Toda mutação é rastreada na outbox e idempotente.
- **Zero Vazamento de Itens Excluídos**: O guard rail automatizado impede regressões onde novos desenvolvedores ou agentes pudessem esquecer filtros de soft-delete.
- **Relatórios Históricos Intactos**: Histórico de gastos passados nunca é corrompido pela exclusão de uma recorrência.
- **Eliminação de Erros de Fuso Horário**: Datas de vencimento não sofrem deslocamentos em viagens ou alterações de horário de verão.
