# ADR 031: Concorrência Thread-Safe com Mutex, Ponto Único de Disparo e Índice Único de Parcelas

## Status
Aprovado

## Contexto
O caso de uso [ExtendRecurringBillsUseCase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/usecase/ExtendRecurringBillsUseCase.kt) era invocado em 5 pontos diferentes do código (`MainActivity`, `DueReminderReceiver`, `BillsViewModel`, `RecurringInstallmentsViewModel` e `DashboardViewModel`), a maioria disparada exatamente no instante de abertura do aplicativo.

Esse modelo apresentava graves vulnerabilidades de concorrência:
1. **Condição de Corrida (Race Condition)**: O ciclo `ler parcelas existentes -> decidir se precisa estender -> calcular novo lote -> inserir no banco` não possuía nenhum mecanismo de exclusão mútua (`Mutex`). Duas ou mais corrotinas liam simultaneamente o mesmo estado defasado do banco, ambas concluíam que a extensão era necessária e ambas inseriam o mesmo lote de parcelas (ex: parcelas 13 a 24).
2. **Ausência de Restrição de Unicidade no Banco**: A tabela `bill_installments` possuía índices em colunas individuais (`billId`, `dueDate`, etc.), mas não possuía índice único composto em `(billId, installmentNumber)`. Assim, o SQLite aceitava inserções duplicadas sem rejeição.
3. **Dispersão e Desperdício de Recursos**: Disparar a verificação de extensão em 5 locais sobrecarregava desnecessariamente a thread de I/O na inicialização e em trocas de tela.

## Decisão

### 1. Escopo `@Singleton` e Proteção com `Mutex.withLock`
- O caso de uso [ExtendRecurringBillsUseCase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/usecase/ExtendRecurringBillsUseCase.kt) foi anotado com `@Singleton` (garantindo instância única gerenciada pelo Hilt).
- A execução do operador `invoke` foi completamente envolvida em `mutex.withLock { ... }`. Caso uma segunda corrotina tente executar a extensão enquanto uma já está em andamento, ela aguardará a conclusão da primeira; ao adquirir o lock, lerá o banco já atualizado e encerrará imediatamente (retornando 0) com garantia de idempotência.

### 2. Ponto Único Canônico de Disparo (`PlatformApplication`)
- A extensão contínua agora é acionada exclusivamente a partir de [PlatformApplication.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/PlatformApplication.kt) dentro de `applicationScope.launch` no `onCreate()`, logo após `seedInitialDataUseCase()`.
- Removidas todas as chamadas redundantes e concorrentes de:
  - [MainActivity.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/MainActivity.kt)
  - [DueReminderReceiver.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/core/notification/DueReminderReceiver.kt)
  - [BillsViewModel.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/presentation/bills/BillsViewModel.kt)
  - [RecurringInstallmentsViewModel.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/presentation/recurring/RecurringInstallmentsViewModel.kt)
  - [DashboardViewModel.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/presentation/dashboard/DashboardViewModel.kt)

### 3. Migração Room v17 (`MIGRATION_16_17`) e Índice Único
Em [PlatformDatabase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/PlatformDatabase.kt):
- O banco Room foi elevado para a versão 17.
- **Saneamento de Duplicatas Históricas**: Executada query de exclusão atômica mantendo a parcela mais antiga por `(billId, installmentNumber)` e preservando estritamente aquelas com `status = 'PAID'` ou `paidAt IS NOT NULL`.
- **Recálculo de Totais**: `totalInstallments` na tabela `bills` recalculado dinamicamente para refletir a quantidade real de parcelas únicas pós-limpeza.
- **Criação de Índice Único**: `CREATE UNIQUE INDEX IF NOT EXISTS index_bill_installments_billId_installmentNumber ON bill_installments(billId, installmentNumber)`.
- Adicionado `Index(value = ["billId", "installmentNumber"], unique = true)` em [BillInstallmentEntity.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/entity/BillInstallmentEntity.kt).

### 4. Inserção Segura com `OnConflictStrategy.IGNORE`
- Em [BillInstallmentDao.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/dao/BillInstallmentDao.kt), `insertAll` e o novo método `insertAllIgnore` utilizam `@Insert(onConflict = OnConflictStrategy.IGNORE)`.
- Em [FinancialRepositoryImpl.addInstallments](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/FinancialRepositoryImpl.kt), as parcelas são persistidas via `insertAllIgnore`, prevenindo qualquer substituição indesejada de dados preexistentes.

## Consequências

### Positivas
- **Dupla Proteção contra Duplicatas**: Bloqueio preventivo no nível de lógica de domínio com `Mutex` e proteção definitiva no nível de persistência com `UNIQUE INDEX` no SQLite.
- **Inicialização Otimizada e Previsível**: A abertura do app não sofre mais contenção de I/O de 5 corrotinas disputando a mesma tabela.
- **Preservação de Dados de Pagamento**: O script de migração respeita e prioriza todas as parcelas já liquidadas pelo usuário.
- **Cobertura Automatizada Rigorosa**: Teste unitário de concorrência com corrotinas assíncronas concorrentes (`async(Dispatchers.Default)`) comprovando ausência de duplicidade.
