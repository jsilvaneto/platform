# ADR 037: Notificações Confiáveis via WorkManager com Ações Rápidas e UseCase de Orçamento

- **Status**: Aceito
- **Data**: 2026-10-10
- **Contexto**: Prompts 37 e 41 (Notificações Confiáveis de Vencimento e Cálculo de Orçamento sem Duplicidade)

## Contexto e Problema

1. **Notificações**:
   - O agendamento de lembretes dependia exclusivamente de `AlarmManager.setInexactRepeating` iniciado apenas em `MainActivity.onCreate`. Após reinicializações do dispositivo, o alarme era perdido por falta de `BootReceiver`.
   - No Android 13+ (API 33+), a permissão `POST_NOTIFICATIONS` nunca era solicitada em runtime, resultando em cancelamento silencioso das notificações do sistema.
   - Os alertas eram restritos a um horário fixo sem suporte a atrasadas ou véspera, com ícones genéricos e exibição de valores em texto puro na tela de bloqueio, sem capacidade de interação rápida.
   - A rotina de checagem carregava todas as parcelas do banco em memória.
2. **Cálculo de Orçamento**:
   - A lógica residia no `BudgetsViewModel`, duplicando a contagem quando o usuário possuía simultaneamente um "Teto Geral" e tetos por categoria.
   - Não havia diferenciação entre valores pagos e pendentes, ocorrências com status `PAUSED` eram somadas indevidamente, e compras de cartão de crédito eram alocadas na data de vencimento da fatura e não na competência da compra (`createdAt`).

## Decisões Tomadas

1. **WorkManager Periódico com Ações Rápidas (`ExpenseNotificationWorker`)**:
   - Substituímos o alarme por um `PeriodicWorkRequest` de 24 horas (`ExpenseNotificationWorker`), re-agendado no `PlatformApplication.onCreate()`, garantindo sobrevivência a reboots.
   - Implementamos solicitação contextual de `POST_NOTIFICATIONS` na criação da primeira despesa e adicionamos um banner explicativo na tela de Configurações com link para os ajustes do sistema caso a permissão seja revogada.
   - Criamos o `ExpenseNotificationActionReceiver` suportando ações rápidas diretamente na notificação: "Paguei", "Adiar 1 dia" e "Pagar Fatura".
   - Aplicamos `NotificationCompat.VISIBILITY_PRIVATE` com layout resumido para telas de bloqueio desprovido de valores financeiros abertos.
   - Implementamos queries SQL otimizadas (`getPendingInstallmentsInRange`, `getOverduePendingInstallments`, `getPendingInvoicesInRange`, `getOverdueInvoices`).
2. **Centralização das Regras de Orçamento em `GetBudgetProgressUseCase`**:
   - Criamos o caso de uso `GetBudgetProgressUseCase` no domínio.
   - **Regra do Teto Geral**: Quando configurado um teto geral (`categoryId == null`), ele dita o total limite e gasto geral, tratando tetos de categoria como sublimites analíticos sem duplicar `totalSpent`.
   - **Competência de Compra de Cartão**: Compras parceladas ou à vista no cartão (`creditCardId != null` ou `invoiceId != null`) são contabilizadas no mês da compra (`createdAt`) e não no vencimento futuro da fatura.
   - **Exclusão de Pausadas e Quebra Pago/Pendente**: Ocorrências com `isPaused == true` são sumariamente desconsideradas. Os montantes são discriminados em `totalPaidCents` e `totalPendingCents`.
   - Adicionamos navegação temporal mensal com `MonthNavigationHeader` na tela de orçamentos.

## Consequências

- **Positivas**:
  - Confiabilidade absoluta nos avisos de contas a pagar, com experiência fluida de marcação de pagamento na própria barra de notificações do Android.
  - Precisão contábil e orçamentária no planejamento mensal.
  - Desempenho otimizado no banco de dados SQLite sem alocação desnecessária de memória.
- **Negativas / Mitigações**:
  - Ações na notificação fecham a aba de alerta e requerem desbloqueio do aparelho pelo usuário se houver chave biométrica restrita ativa.
