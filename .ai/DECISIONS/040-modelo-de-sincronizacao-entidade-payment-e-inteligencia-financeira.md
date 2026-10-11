# ADR 040: Modelo de Sincronização Outbox, Entidade Payment, Inteligência Financeira e Acabamento Visual

- **Status**: Aceito
- **Data**: 2026-10-10
- **Contexto**: Prompts 48 a 54 (Persistência de liquidações, outbox offline-first, parser de boletos, simulador multi-cartão, quitação acelerada e higiene visual)

## Contexto e Problema

Com a evolução da plataforma e a preparação para a sincronização bidirecional com o backend (Fase 11), identificaram-se necessidades arquiteturais críticas:
1. **Acoplamento de Pagamento**: A quitação era representada apenas por flags e datas estáticas na própria parcela (`paidAt`, `status`), inviabilizando pagamentos parciais, histórico de amortizações e registro de juros, multas ou descontos concedidos.
2. **Modelo de Sincronização Frágil**: Deleções físicas (`DELETE FROM`) impediam a propagação de exclusões para o servidor e outros dispositivos. Faltava uma fila de outbox transacional para garantir entrega at-least-once offline-first.
3. **Lacuna em Automações de Pagamento**: O usuário necessitava digitar manualmente valores e vencimentos de boletos bancários e concessionárias, sem validação matemática dos dígitos verificadores ou suporte ao rollover BACEN.
4. **Falta de Inteligência de Compra e Quitação**: O usuário não possuía visibilidade do impacto no fluxo de caixa ao escolher entre múltiplos cartões (float financeiro), nem um plano estruturado para amortização acelerada de dívidas acumuladas.
5. **Inconsistências Visuais Residuais**: Presença de componentes `Surface` crus fora do Design System, números oscilando em largura (jitter visual em valores tabulares) e ausência de teste automatizado de contraste WCAG.

## Decisões Tomadas

1. **Entidade de Pagamento Append-Only (`payments`) e Room Migration 17->18**:
   - Criada a tabela `payments` (`PaymentEntity` e modelo de domínio `Payment`) com chave estrangeira `ON DELETE CASCADE` para `bill_installments(id)`.
   - Propriedades com representação exata em centavos: `amountCents`, `interestCents`, `fineCents`, `discountCents` e `totalPaidWithChargesCents`.
   - Executada a `MIGRATION_17_18` com backfill automático de parcelas quitadas legadas (`status = 'PAID' OR paidAt IS NOT NULL`) para registros em `payments`.
   - `PaymentRepository` e `PaymentRepositoryImpl` implementados com despacho assíncrono em `Dispatchers.IO`.

2. **Arquitetura de Sincronização Outbox e Soft-Delete**:
   - Criada a tabela `sync_outbox` com status (`PENDING`, `SYNCED`, `FAILED`), contagem de retentativas e payload JSON de cada mutação.
   - Criada a tabela `sync_state` para gerenciar chaves e timestamps da sincronização.
   - Adicionada coluna `deletedAt: Long?` em `bills` e `bill_installments`, garantindo soft-deletes seguros para replicação idempotente.

3. **Validador e Parser de Boletos Brasileiros (`BoletoParser`)**:
   - Validação completa de títulos bancários de 47 dígitos (módulo 10 por blocos e montagem do código de barras de 44 posições).
   - Validação de concessionárias e convênios de 48 dígitos (iniciados em 8, com módulos 10 e 11).
   - Extração do fator de vencimento com suporte ao rollover do Banco Central a partir de 22/02/2025.
   - Extração automática de valores em centavos (`amountCents`).

4. **Simulador Multi-Cartão 'Posso comprar?' (`SimulatePurchaseAffordabilityUseCase`)**:
   - Avaliação simultânea de todos os cartões cadastrados dado um valor de compra.
   - Determinação do melhor dia de compra (após a data de corte/fechamento), calculando o float real em dias até o vencimento da fatura.
   - Recomendação determinística do cartão que concede o maior prazo de pagamento e possui limite disponível.

5. **Plano de Quitação Acelerada de Dívidas (`CalculateDebtPayoffPlanUseCase`)**:
   - Implementação das estratégias **Bola de Neve** (*Snowball*, ordenando pelo menor saldo devedor para vitórias rápidas) e **Avalanche** (*Avalanche*, priorizando maiores compromissos e taxas).
   - Simulação progressiva mês a mês com alocação do pool extra e efeito cascata das parcelas liberadas.

6. **Design System, Tokens e Tipografia Tabular**:
   - Eliminação de 100% dos usos literais de `Surface(` fora de `components/`. Criada e ativada a task Gradle `:app:checkRawSurfaces` no `preBuild`.
   - Introdução de `PlatformDepth` (`N0` a `N3`) com modificador `Modifier.platformDepth`.
   - Criação de `PlatformMoneyText` e extensão `.withTabularNumbers()` ativando `fontFeatureSettings = "tnum"` para alinhamento numérico exato.
   - Implementação de `ThemeContrastTest` verificando matematicamente conformidade com WCAG 2.1 AA (contraste >= 4.5:1 para Claro, Escuro e AMOLED).

## Consequências

- **Positivas**:
  - Plataforma 100% preparada para a Fase 11 de sincronização em nuvem e exportações semânticas.
  - Rastreabilidade contábil completa de acréscimos e descontos no ato da quitação.
  - Zero risco de perda de dados por exclusões precipitadas devido ao soft-delete.
  - Redução drástica do atrito de cadastro via leitura automática de boletos.
  - Design executivo refinado, com conformidade de contraste acessível e sem descontinuidades tipográficas.
