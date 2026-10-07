# ADR 020: Data Real de Pagamento, Baixa Retroativa e Migração Room v10

## Status
Aprovado

## Contexto
No fluxo anterior de quitação de parcelas (`ToggleInstallmentPaymentUseCase`), o timestamp `paidAt` era sempre gravado com o momento exato do clique no app (`System.currentTimeMillis()`).
Como consequência, no cálculo do indicador `onTimePaymentRate` em `GetFinancialDashboardUseCase`, a comparação `(it.paidAt ?: it.dueDate) <= it.dueDate` penalizava qualquer baixa realizada dias após o pagamento real (cenário frequente de conciliação e backfill retroativo), marcando parcelas pontuais como atrasadas.

## Decisões

### 1. Extensão do Modelo e Entidade com `actualPaymentDate`
- Adicionado o campo opcional `actualPaymentDate: Long? = null` em:
  - `domain.model.BillInstallment`
  - `data.local.entity.BillInstallmentEntity`
- Mapeamento bidirecional atualizado nas funções `toDomain()` e `fromDomain()`.

### 2. Migração de Banco de Dados Room (`PlatformDatabase` v10)
- Versão do banco elevada de `9` para `10`.
- Criação e registro da migração `MIGRATION_9_10`:
  ```sql
  ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL
  ```
- Migração adicionada à lista de migrações automáticas em `AppModule.kt`.

### 3. Diálogo de Confirmação e Ajuste de Data (`ConfirmPaymentDialog`)
- Criação de um componente visual Material 3 reutilizável para confirmação de pagamento:
  - Exibe identificadores da despesa (título e valor em destaque).
  - Permite seleção rápida via chips: "Hoje" e "No Vencimento".
  - Permite seleção de qualquer data passada ou futura via `DatePickerDialog` nativo.
  - Integrado de forma consistente em `BillsScreen`, `HomeScreen`, `RecurringInstallmentsScreen`, `ContactDetailScreen` e `EditInstallmentBottomSheet`.

### 4. Recálculo Pontual da Taxa de Pontualidade (`GetFinancialDashboardUseCase`)
- A métrica `onTimeCount` passa a utilizar prioritariamente `actualPaymentDate`, mantendo fallback para `paidAt` e `dueDate`:
  ```kotlin
  val paymentTimestamp = it.actualPaymentDate ?: it.paidAt ?: it.dueDate
  ```
- Normalização para o início do dia (`startOfDay`) para pagamentos efetuados dentro do dia do vencimento.

### 5. Repositório e Use Case
- `FinancialRepository.toggleInstallmentPayment`: recebe `actualPaymentDate: Long? = null`.
- `ToggleInstallmentPaymentUseCase`: ao marcar como pago (`isPaid = true`), se `actualPaymentDate` não for explicitamente fornecido, assume o timestamp da quitação (`paidTimestamp`); ao desmarcar (`isPaid = false`), ambos os timestamps são limpos (`null`).
- Suporte mantido para liquidação de faturas de cartão (`payInvoice` / `reopenInvoice`).

## Consequências
- Métricas e taxa de pontualidade no Dashboard e Estatísticas passam a refletir fielmente a realidade financeira do usuário, mesmo em baixas retroativas.
- 100% de compatibilidade com parcelas existentes no banco local sem necessidade de recadastro ou perda de dados.
- Experiência de uso consistente, ágil e em conformidade com os princípios offline-first da aplicação.
