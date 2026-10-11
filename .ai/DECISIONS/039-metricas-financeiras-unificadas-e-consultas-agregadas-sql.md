# ADR 039: Métricas Financeiras Unificadas, Agregações SQL no Room e Contrato Backend

- **Status**: Aceito
- **Data**: 2026-10-10
- **Contexto**: Prompt 44 (Agregados em um só lugar, eliminação de somas redundantes e contrato analítico)

## Contexto e Problema

Anteriormente, métricas essenciais como totais mensais de despesas, valores pendentes, vencidos e saldos por contato eram recalculadas de forma dispersa em mais de 11 arquivos da camada de apresentação (`presentation/`), via chamadas manuais `filter { ... }.sumOf { it.amountCents }`.
Isso ocasionava três fragilidades centrais:
1. **Ineficiência de memória e I/O**: Telas como Contatos carregavam todas as parcelas do banco de dados na memória RAM apenas para agrupar e somar valores devedores em aberto.
2. **Inconsistência de regras**: Algumas telas desconsideravam ocorrências pausadas (`PAUSED`), enquanto outras as somavam; regras de vencido vs a vencer variavam sutilmente.
3. **Ausência de contrato canônico para o Backend**: Falta de uma especificação formal de como a competência, o regime de caixa, as faturas de cartão e os status especiais devem ser agregados de forma idêntica tanto no SQLite do aplicativo quanto nas APIs e relatórios do backend.

## Decisões Tomadas

1. **Separação Canônica entre Regime de Caixa e Regime de Competência**:
   - **Regime de Caixa (Desembolso Efetivo)**:
     - Orientado a pagamentos reais e liquidez financeira.
     - Utiliza `actualPaymentDate` quando a obrigação está quitada (`isPaid == true`), e `dueDate` quando pendente ou atrasada.
     - Aplicado nas telas operacionais e de execução: `Hoje`, `Contas` e `Calendário`.
   - **Regime de Competência (Fato Gerador / Consumo)**:
     - Orientado ao momento da tomada da despesa ou compra.
     - Para cartões de crédito: a despesa pertence ao ciclo da fatura (`referenceMonth`) correspondente à data de fechamento e criação (`bill.createdAt`), independentemente da data futura em que a fatura será paga.
     - Aplicado no planejamento orçamentário (`GetBudgetProgressUseCase`, regra 50-30-20 e tetos por categoria).

2. **Invariante Absoluto do Status PAUSED**:
   - Compromissos recorrentes temporariamente suspensos (`status == 'PAUSED'` ou `isPaused == true`) possuem tratamento contábil padronizado:
     - Excluídos sumariamente da materialização de faturas.
     - Excluídos das somas de faturas de cartão (`BillInstallmentDao.getInvoiceTotals`).
     - Excluídos de totais a pagar, pendentes e vencidos (`MonthSummary`).
     - Excluídos do saldo devedor de contatos (`GetOpenAmountByContactUseCase`).
     - Excluídos do consumo de tetos de gastos.

3. **Faturas de Cartão de Crédito e Regra Única de Limite**:
   - **Unidade de Pagamento**: Compras no cartão associadas a faturas (`invoiceId != null`) são quitadas em lote através da fatura (`payInvoice`), que materializa preventivamente as ocorrências do ciclo antes da liquidação.
   - **Consumo do Limite (`CardUsage`)**:
     - `INSTALLMENT` (Parceladas): Consome o saldo devedor restante de todas as parcelas futuras ainda não pagas.
     - `RECURRING` e `SINGLE` (Recorrentes e Avulsas): Consomem apenas a fatura corrente aberta ou fechada não paga (`status != PAGA`).

4. **Agregações em SQL no Room (`BillInstallmentDao`)**:
   - `getMonthSummary(startMillis, endMillis, nowMillis)`: Agrega atomicamente via `SUM(CASE WHEN ...)` e `COUNT(CASE WHEN ...)` na própria query SQLite, excluindo `PAUSED`.
   - `getOpenAmountByContact()`: Agrupa por `COALESCE(i.contactId, b.contactId)` e soma montantes em aberto sem alocar listas de parcelas na memória.
   - Criados os casos de uso `GetMonthSummaryUseCase`, `GetOpenAmountByContactUseCase` e `GetCardUsageUseCase`.
   - Implementadas as extensões de domínio `calculateTotalCents()`, `calculatePaidCents()` e `calculateOpenCents()` para listas filtradas em memória, garantindo paridade numérica estrita com as queries do banco.

5. **Contrato de API para o Backend e Web**:
   - Os endpoints de sincronização e relatórios agregados do backend adotarão exatamente as mesmas cláusulas SQL e invariantes descritos neste documento, garantindo 100% de paridade entre a experiência offline no app e o painel web.

## Consequências

- **Positivas**:
  - Eliminação de centenas de recálculos ad-hoc na UI.
  - Performance aprimorada em SQLite e redução drástica do uso de memória RAM.
  - Segurança contábil: uma única fonte da verdade para o cálculo de faturas, orçamentos e saldos.
  - Facilidade de auditoria e manutenção via testes de paridade unitários (`MonthSummaryParityTest`).
- **Negativas / Mitigações**:
  - Filtros locais complexos (e.g. busca textual somada a filtros múltiplos de categoria) utilizam a função pura `getMonthSummaryUseCase.calculate()`, mantendo o mesmo algoritmo da query SQL.
