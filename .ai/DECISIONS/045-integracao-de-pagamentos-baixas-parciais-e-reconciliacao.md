# ADR 045: Integração do Agregado Payment, Baixas Parciais e Reconciliação Transacional

## Contexto e Motivação
Historicamente, o Platform gerenciava quitações financeiras gravando colunas de cache (`isPaid`, `paidAt`, `actualPaymentDate`) diretamente na tabela `bill_installments`. Embora a entidade `PaymentEntity`, a tabela `payments` e o repositório `PaymentRepository` tivessem sido introduzidos preliminarmente, as telas operacionais, os casos de uso e o fluxo de quitação continuavam gravando apenas nas parcelas. Isso gerava defasagem:
1. A tabela `payments` permanecia vazia ou desatualizada a partir da migração v18.
2. Amortizações parciais, acréscimos (juros/multa) e deduções (descontos) não podiam ser rastreados de forma auditável e granular.
3. Não havia sincronização dos pagamentos de faturas de cartão de crédito (`payInvoice`) com o razão geral de baixas.
4. Parcelas já pagas pós-migração v18 não tinham contrapartida na tabela `payments`.

## Decisões Tomadas

### 1. Pagamento como Fonte Primária de Verdade
Toda e qualquer liquidação de dívida (baixa simples, baixa com data real customizada, pagamento com juros/desconto ou quitação de fatura de cartão de crédito) gera um ou mais registros auditáveis na tabela `payments`:
- `id`: UUID v4 único do pagamento.
- `billInstallmentId`: Vínculo direto com a parcela amortizada.
- `amountCents`: Valor efetivamente pago em centavos inteiros (`Long`).
- `paymentDate`: Data real da operação financeira em milissegundos UTC.
- `paymentMethodId` e `financialAccountId`: Conta e meio financeiro utilizados na baixa.
- `interestCents`, `fineCents`, `discountCents`: Acréscimos e descontos aplicados.
- `notes`: Observações contextuais (ex: `"Pagamento da fatura (<id>)"`).

### 2. Status Derivado da Parcela e Cache Atômico
O status de liquidação de `bill_installments` passa a ser calculado pela soma aritmética dos pagamentos ativos (`deletedAt IS NULL`):
$$\text{Total Amortizado} = \sum (\text{amountCents} + \text{discountCents})$$
- Se $\text{Total Amortizado} \ge \text{inst.amountCents}$: Status = `PAID`, `paidAt` preenchido com a data da liquidação total e `actualPaymentDate` preenchido com a data real informada.
- Se $0 < \text{Total Amortizado} < \text{inst.amountCents}$: Status = `PARTIALLY_PAID` ("Pago em parte"), `paidAt` permanece `null` e a parcela continua exigível pelo saldo remanescente.
- Se $\text{Total Amortizado} = 0$: Status = `PENDING`, `paidAt` e `actualPaymentDate` revertidos para `null`.

As colunas `paidAt`, `actualPaymentDate` e `status` de `bill_installments` atuam como cache de alto desempenho, atualizadas na mesma transação SQLite (`withTransaction`) em que o `Payment` é inserido ou removido logicamente.

### 3. Estorno por Soft Delete Transacional
Ao desfazer uma quitação (toggle de pagamento para pendente ou reabertura de fatura de cartão via `reopenInvoice`):
- Os registros correspondentes na tabela `payments` sofrem exclusão lógica com `deletedAt = now` e `updatedAt = now`.
- É enfileirado evento `DELETE_ALL_FOR_INSTALLMENT` ou `DELETE_ALL_FOR_INVOICE` na `sync_outbox`.
- O cache da parcela reverte de forma atômica para `PENDING`.

### 4. Quitação de Faturas de Cartão (`payInvoice`)
Ao liquidar uma fatura de cartão de crédito:
- Para cada parcela anexada à fatura (`getInstallmentsListForInvoice`), gera-se um `PaymentEntity` individualizado com `notes = "Pagamento da fatura ($invoiceId)"` e a data de liquidação da fatura como `paymentDate`.
- Isso unifica o fluxo: relatórios mensais, extratos de pagamentos e pontualidade passam a enxergar as despesas do cartão através da mesma tabela `payments`.

### 5. Reconciliação Automática no Banco de Dados (`MIGRATION_20_21`)
Para garantir integridade retrospectiva para bases existentes:
- Criada a migração Room `MIGRATION_20_21` (Versão 21 do `PlatformDatabase`).
- Executa SQL de reconciliação que detecta todas as parcelas marcadas como pagas (`status = 'PAID' OR paidAt IS NOT NULL`) sem pagamentos correspondentes e gera um `PaymentEntity` com ID prefixado `'rec_' || id`, preservando data real e valores históricos.

### 6. Interface de Baixa Enriquecida e Responsiva
O componente `ConfirmPaymentDialog` foi evoluído para suportar:
- Campo editável de valor pago (preenchido por padrão com o saldo total da parcela).
- Seção opcional expansível para juros/multa e descontos.
- Atalhos "Hoje" e "No Vencimento" combinados com seleção de calendário arbitrário.
- Callback `onConfirmWithDetails` integrado a `BillsUiAction.TogglePayment` e `ToggleInstallmentPaymentUseCase.recordPayment`.

## Consequências e Benefícios
- **Precisão Financeira Estrita**: Suporte nativo a quitações parciais sem perda de rastreabilidade contábil.
- **Preparação Plena para Sincronização**: Cada evento financeiro é uma linha imutável com `createdAt`, `updatedAt`, `deletedAt` e evento na `sync_outbox`.
- **Zero Inconsistência**: O cache na parcela elimina overhead em listagens diárias enquanto a tabela `payments` garante a verdade do razão contábil.
