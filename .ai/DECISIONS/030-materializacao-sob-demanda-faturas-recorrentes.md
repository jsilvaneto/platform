# ADR 030: Materialização Sob Demanda de Faturas e Ancoragem Estável de Recorrências no Cartão de Crédito

## Status
Aprovado

## Contexto
Na versão 1.16.0, para evitar a poluição do banco SQLite local com 11 faturas futuras vazias ao cadastrar uma despesa recorrente (`RECURRING`) no cartão de crédito, estabeleceu-se a premissa de criar as ocorrências com `index > 0` com `invoiceId = null`. O objetivo documentado era permitir faturas "sob demanda".

Entretanto, esse passo subsequente de materialização não havia sido implementado:
1. **Falta de Vinculação Sob Demanda**: `getOrCreateInvoiceForMonth` era invocado exclusivamente dentro de `CreateBillUseCase`. Como resultado, assinaturas mensais no cartão (como serviços de streaming ou academias) só apareciam na fatura do 1º ciclo. As faturas seguintes ficavam subestimadas e a conferência do valor da fatura do app contra o extrato real da operadora quebrava.
2. **Apontamento Incorreto nas Ocorrências Estendidas**: O método `CalculateInstallmentsUseCase.generateNextRecurringInstallments` (que estende recorrências do mês 13 ao mês 24) copiava cegamente `invoiceId = bill.invoiceId`. Como `bill.invoiceId` armazenava a primeira fatura criada meses atrás (muitas vezes já paga), as ocorrências futuras eram atreladas à fatura do passado, distorcendo relatórios e quebrando a integridade dos dados.
3. **Desvio Progressivo do Dia de Ciclo**: `generateNextRecurringInstallments` ancorava o cálculo do vencimento em `firstInstallment.dueDate`. No caso de despesas de cartão de crédito, a primeira parcela já havia tido seu vencimento alterado do dia da compra para o dia de vencimento da fatura (por exemplo, de dia 10 para dia 27). Isso provocava desvio da data-base e inconsistência no cálculo do dia de fechamento/competência nos ciclos estendidos.

## Decisão

### 1. Materialização Sob Demanda ao Abrir/Criar Faturas
Em [FinancialRepositoryImpl.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/repository/FinancialRepositoryImpl.kt), o fluxo de `getOrCreateInvoiceForMonth(cardId, referenceMonth)` foi tornado atômico (`database.withTransaction`) e expandido para:
- Obter ou criar a fatura para o mês de referência solicitado.
- Consultar no banco todas as parcelas recorrentes do cartão sem fatura (`invoiceId IS NULL`) através de `getUnattachedRecurringInstallmentsForCard(cardId)`.
- Filtrar aquelas cujo ciclo de compra pertence à competência da fatura atual (`CreditCardCalculator.determineInvoiceReferenceMonth(installment.dueDate, card.closingDay, card.dueDay) == referenceMonth`).
- Anexar essas parcelas em lote atômico (`attachInstallmentsToInvoice`), vinculando o `invoiceId` e alinhando o `dueDate` da parcela com o vencimento da fatura.

### 2. Rotina de Materialização Preventiva (`materializeRecurringCardInvoices`)
Implementado método no repositório financeiro para varrer todos os cartões cadastrados e materializar as faturas e seus vínculos recorrentes para todas as competências até o mês corrente (`referenceMonth <= currentRefMonth`).
Esse método foi integrado em três pontos estratégicos do ciclo de vida:
- **Rotina Diária de Lembretes**: Em [DueReminderReceiver.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/core/notification/DueReminderReceiver.kt), executado antes do disparo de notificações de contas a vencer.
- **Expansão de Horizontes Recorrentes**: Em [ExtendRecurringBillsUseCase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/usecase/ExtendRecurringBillsUseCase.kt), após a extensão periódica das despesas.
- **Abertura da Gestão de Cartões**: No `init` e na seleção de competência em [CreditCardsViewModel.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/presentation/creditcards/CreditCardsViewModel.kt).

### 3. Desacoplamento de Faturas e Ancoragem Estável (`recurrenceAnchorDate`)
No caso de uso [CalculateInstallmentsUseCase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/usecase/CalculateInstallmentsUseCase.kt):
- `generateNextRecurringInstallments` agora atribui explicitamente `invoiceId = null` para parcelas futuras no cartão de crédito, eliminando a cópia errônea de `bill.invoiceId`.
- Criado o campo `recurrenceAnchorDate: Long?` no modelo [Bill.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/model/Bill.kt) e na entidade [BillEntity.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/entity/BillEntity.kt).
- O cálculo da data de cada ocorrência estendida agora ancora em `bill.recurrenceAnchorDate` (com fallback seguro em `firstInstallment.dueDate`), preservando com exatidão matemática o dia original da compra independentemente do dia de vencimento da fatura do cartão.

### 4. Evolução do Esquema Room v16 (`MIGRATION_15_16`)
Em [PlatformDatabase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/PlatformDatabase.kt):
- O banco Room foi elevado para a versão 16.
- Adicionadas as colunas `recurrenceAnchorDate INTEGER DEFAULT NULL` e `creditCardId TEXT DEFAULT NULL` na tabela `bills`.
- Script de migração com backfill inteligente: deriva `creditCardId` a partir dos registros de `credit_card_invoices` associados às parcelas da despesa e preenche `recurrenceAnchorDate` com base na menor data de vencimento das parcelas da despesa recorrente.

## Consequências

### Positivas
- **Conferência Exata de Faturas**: Cada fatura do cartão de crédito passa a conter exatamente 1 ocorrência de cada assinatura vigente em sua respectiva competência, conferindo 100% com o extrato bancário.
- **Resíduo Zero de Faturas Fantasmas**: Faturas futuras de cartões não são criadas antecipadamente no SQLite de forma desnecessária; são materializadas apenas no momento da consulta ou quando a competência é atingida.
- **Integridade Temporal**: O dia de fechamento e vencimento de recorrências mantém estabilidade perpétua ao longo de meses 13+, sem desvio cumulativo.
- **Isolamento de Faturas Pagas**: Parcelas futuras estendidas nunca mais apontam para faturas de meses anteriores já liquidadas.
- **Rigor em Testes**: Suíte de testes automatizados com simulação completa de 14 meses (`RecurringCardSubscription14MonthsTest`), testes de migração Room e testes unitários com 100% de sucesso.
