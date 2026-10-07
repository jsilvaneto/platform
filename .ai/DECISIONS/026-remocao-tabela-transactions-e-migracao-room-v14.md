# ADR 026: Remoção da Tabela Legada transactions, Resíduo Zero e Migração Room v14

## Status
Aprovado

## Contexto
Na versão 6 do banco de dados Room (introduzida na `MIGRATION_6_7`), existia a entidade `TransactionEntity` e seu respectivo `TransactionDao`. Essa tabela continha atributos de despesas/receitas (`description`, `amountCents`, `dueDate`, `paymentDate`, `status`, `isRecurring`, `syncStatus`, chaves estrangeiras para categoria e método de pagamento).

Com a evolução arquitetural para o modelo unificado de Contas e Parcelas (`bills` e `bill_installments`), todo o fluxo financeiro do aplicativo passou a ser gerido exclusivamente por `BillRepository`, `BillDao` e casos de uso específicos. Como consequência:
1. Nenhuma ViewModel, UseCase, Repository ou Composable de UI consultava ou persistia dados na tabela `transactions`.
2. A entidade persistia unicamente como código zumbi registrada em `PlatformDatabase`, provida pelo Hilt em `AppModule` e serializada (como lista vazia) nos arquivos de exportação/restauração de backup (`BackupRepositoryImpl` e `BackupDataDto`).
3. O simples abandono da classe sem exclusão física manteria a tabela `transactions`, suas constraints e índices órfãos ocupando espaço físico no SQLite de todos os usuários com versões anteriores instaladas, violando a diretriz de governança técnica.

Conforme estabelecido na política de **Resíduo Zero** (`.agents/rules/test_data_cleanup.md`), resíduos em bancos de dados locais, artefatos descartáveis e códigos obsoletos devem ser higienizados e eliminados fisicamente.

## Decisão

### 1. Eliminação Completa de Código Obsoleto
- Exclusão dos arquivos:
  - `app/src/main/java/com/platform/app/data/local/entity/TransactionEntity.kt`
  - `app/src/main/java/com/platform/app/data/local/dao/TransactionDao.kt`
- Remoção do provider `@Provides @Singleton fun provideTransactionDao(...)` em `AppModule.kt`.
- Remoção de `TransactionEntity` e `transactionDao` do contrato de `PlatformDatabase.kt`.
- Remoção do campo `transactions: List<TransactionEntity>` de `BackupDataDto.kt` e de todas as chamadas correspondentes em `BackupRepositoryImpl.kt` e `BackupRepositoryImplTest.kt`.

### 2. Migração de Banco de Dados Room v13 -> v14 (DROP TABLE Físico)
- Versão do `PlatformDatabase` incrementada de `13` para `14`.
- Implementada a migração destrutiva segura `MIGRATION_13_14`:
  ```kotlin
  val MIGRATION_13_14 = object : Migration(13, 14) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("DROP TABLE IF EXISTS transactions")
      }
  }
  ```
- Migração devidamente registrada no Room Database Builder em `AppModule.kt`.
- A instrução `DROP TABLE IF EXISTS transactions` garante a eliminação física da tabela, índices associados e foreign keys órfãs no dispositivo do usuário, garantindo integridade e conformidade com o princípio de Resíduo Zero.

### 3. Garantia de Qualidade e Testes
- Criado o teste unitário `PlatformDatabaseMigrationTest` validando que a transição de versão `13 -> 14` executa exatamente a instrução `DROP TABLE IF EXISTS transactions`.
- Atualizado `BackupRepositoryImplTest` para validar backup e restauração sem dependências de `TransactionDao`.
- Execução de 100% dos testes da suíte com aprovação integral.

## Consequências

### Positivas
- **Resíduo Zero**: Nenhuma tabela fantasma remanescente no banco local dos usuários já existentes.
- **Eficiência de Backup**: Payloads de exportação e restauração JSON não contêm mais nós vazios nem overhead de serialização de `transactions`.
- **Higiene do Grafo de DI**: Menos instâncias singleton carregadas no container do Hilt na inicialização do aplicativo.
- **Clareza Arquitetural**: Desaparecimento de ambiguidades entre `Transaction` e `Bill`/`BillInstallment`.

### Negativas / Mitigações
- Requer execução de migração Room (`13 -> 14`). Como a tabela não continha dados em uso desde as versões anteriores, a operação de `DROP TABLE` é instantânea, atômica e segura.
