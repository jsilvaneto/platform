# ADR 043: Ciclo de Vida de Recorrências, Status SKIPPED e Gestão Resiliente

## Status
Aprovado / Implementado

## Contexto
Na versão v1.25.0 do Platform, a gestão do ciclo de vida de compromissos recorrentes (assinaturas, mensalidades, custos fixos) apresentava três falhas críticas que inviabilizavam o uso e corrompiam a integridade dos dados:
1. **Orfandade de Ações**: Os botões "Pausar" e "Encerrar recorrência" existiam unicamente no `RecurringDetailBottomSheet`, que só era acionado a partir da tela legada de pagamentos planejados (`RecurringInstallmentsScreen`). A aba *Contas* não disponibilizava acesso a tais controles.
2. **Encerramento Falso e Ressurreição de Contas**: O método `stopRecurringBill` apagava indiscriminadamente todas as parcelas não pagas (inclusive cobranças atrasadas e a do mês atual), sem alterar o `recurrenceEndType` ou registrar corte definitivo. Como o `ExtendRecurringBillsUseCase` filtrava apenas por `recurrenceEndType == FOREVER`, a cada reinicialização do aplicativo ou abertura de tela novas parcelas eram regeneradas, ressuscitando a conta excluída/encerrada.
3. **Criação de Dívida Fantasma ao Retomar Pausa**: Pausar uma conta marcava as parcelas pendentes como pausadas sem considerar janelas de tempo. Ao retomar a conta (manual ou automaticamente após 1, 2 ou 3 meses), todas as parcelas dos meses de suspensão retornavam para `PENDING`. Isso gerava cobranças vencidas indevidas por serviços pausados e distorcia o painel financeiro.

Adicionalmente, ao excluir uma recorrência, era impossível manter o histórico de pagamentos passados sem manter as parcelas futuras pendentes no banco.

## Decisão
Implementamos um modelo robusto de gestão de ciclo de vida para contas recorrentes na aba *Contas -> Recorrentes*:

1. **Novo Status Canônico `SKIPPED`**:
   - Criado o status `BillStatus.SKIPPED` no domínio e persistência (`isSkipped` em `BillInstallmentEntity`).
   - Parcelas compreendidas no intervalo de pausa são marcadas como `SKIPPED` (puladas, não devidas).
   - O indicador `SKIPPED` é estritamente ignorado em somatórios de despesas pendentes, orçamentos, dashboard e cálculos de pontualidade.
   - Cobranças atrasadas anteriores ao início da pausa permanecem íntegras como `PENDING`/`OVERDUE`.

2. **Migração Room v18 -> v19**:
   - Adicionadas as colunas `pausedUntil: Long?` e `endedAt: Long?` na tabela `bills`.
   - Atualizados os esquemas exportados do Room (`19.json`).

3. **Ciclo de Vida Resiliente**:
   - **Pausar**: Permite pausar por 1, 2 ou 3 meses, até data específica ou indefinidamente. As ocorrências no período viram `SKIPPED`. Retomada (manual ou automática ao vencer `pausedUntil`) estende novas parcelas a partir do término da pausa sem reviver dívidas do período de suspensão.
   - **Encerrar**: Permite encerrar a partir de uma data de corte (padrão: próximo vencimento). Mantém o histórico passado e as pendências vencidas antes do corte; remove apenas pendências futuras posteriores ao corte e grava `endedAt` definitivo e `recurrenceEndType = UNTIL_DATE`.
   - **Reativar**: Permite reativar uma conta encerrada, limpando `endedAt` e reativando a extensão automática.
   - **Excluir com Escolha de Histórico**: Diálogo com opção de "Excluir mantendo histórico pago" (soft delete na `BillEntity` e remoção apenas de pendências) ou "Excluir tudo definitivamente".

4. **Migração do BottomSheet e Segmentação em Contas**:
   - O `RecurringDetailBottomSheet` foi migrado para a aba *Recorrentes* de `BillsScreen`.
   - Implementado `RecurringBillsList` com segmentação em abas: **Ativas** | **Pausadas** | **Encerradas**.
   - Diálogos padronizados: `PauseRecurringDialog`, `StopRecurringDialog`, `DeleteRecurringDialog` e `EditRecurringAmountDialog`.

## Consequências
- Fim da ressurreição de contas canceladas e eliminação de dívidas fantasmas pós-pausa.
- Manutenção rigorosa do princípio contábil: pagamentos já realizados são preservados mesmo após exclusão de despesas de custo fixo.
- A tela legada `RecurringInstallmentsScreen` pode agora ser removida com segurança (Prompt 43) sem perda de nenhuma funcionalidade de domínio.
