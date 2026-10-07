# ADR 021: Rigidez Orçamentária 50-30-20 Real com Metas (Poupança) e Classificação Multifaixas

## Status
Aprovado

## Contexto
O card de "Rigidez Orçamentária" anteriormente classificava a saúde do orçamento como "Excelente" unicamente caso `Obrigatório <= 55%` e `Deseja <= 30%`. Esse critério gerava graves falsos positivos:
1. **Falso "Excelente" em Orçamento Sobrecarregado**: Um usuário que gastasse 50% em contas obrigatórias e 50% em necessárias (100% em contas básicas), sem nenhum gasto de estilo de vida (0% Deseja) e sem poupar nada (0% Poupança), recebia o diagnóstico de "Excelente: Distribuição equilibrada", mesmo estando em risco e sem flexibilidade.
2. **Ausência da Perna de Poupança na Regra 50-30-20**: No aplicativo **Platform**, o módulo de Metas (`Goal`) operava isolado de Contas a Pagar (`Bill`). Como não existia natureza de poupança em `ExpenseNature`, a regra 50-30-20 (50% Necessidades, 30% Desejos, 20% Poupança/Investimentos) nunca era avaliada por completo.

## Decisão

### 1. Histórico de Aportes de Metas e Migração Room v11
- Criação da entidade `GoalContributionEntity` e tabela `goal_contributions` (`id`, `goalId`, `amountCents`, `date`) com chave estrangeira em cascata para `goals`.
- Versão do banco Room elevada de `10` para `11` em `PlatformDatabase` com migração `MIGRATION_10_11`.
- A migração faz o backfill inicial de metas existentes com saldo para a nova tabela de aportes.
- Adicionado método reativo `getMonthlyContribution(startDate: Long, endDate: Long): Flow<Long>` em `GoalRepository` e `GoalDao`.
- Preservação da integridade no backup SAF JSON em `BackupDataDto` e `BackupRepositoryImpl`.

### 2. Integração Holística no `GetFinancialDashboardUseCase`
- O caso de uso passa a injetar `GoalRepository`.
- No método `invoke(monthMillis)`, combina reativamente os aportes do mês em metas via `goalRepository.getMonthlyContribution(startOfMonth, endOfMonth)`.
- Base total orçada apurada:
  $$\text{Total Orçado} = \text{Total Contas a Pagar} + \text{Aportes em Metas}$$
- Os percentuais das 4 naturezas (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`) e da Poupança passam a ter como denominador comum o Orçamento Total Integrado, somando exatamente 100%.

### 3. Matriz de Classificação Multifaixas (`BudgetRigidityCalculator`)
A avaliação da rigidez e saúde orçamentária passa a ser processada pelo analisador de domínio `BudgetRigidityCalculator`, categorizando em 8 estados com regras estritas:

| Status | Condição | Rótulo do Badge | Descrição / Orientação |
| :--- | :--- | :--- | :--- |
| `VAZIO` | `totalBudgetCents == 0` | Sem Lançamentos | Cadastre contas a pagar e metas para avaliar a rigidez. |
| `SOBRECARREGADO` | `wantsAmount == 0 && savingsAmount == 0` | Atenção: Sobrecarga Essencial | 100% consumido por despesas básicas. Zero margem para desejos (0%) ou poupança (0%). |
| `ENGESSADO` | `mandatoryPct > 55%` ou `essentialsPct > 75%` | Atenção: Orçamento Engessado | Gastos obrigatórios/essenciais absorvem parcela crítica do orçamento. |
| `ESTILO_DE_VIDA_ELEVADO` | `wantsPct > 35%` | Alerta: Estilo de Vida Elevado | Gastos com estilo de vida/desejos acima do teto recomendado de 30%. |
| `SEM_POUPANCA` | `savingsPct < 5%` (com desejos > 0) | Atenção: Poupança Insuficiente | Menos de 5% em metas (ideal 20%). Tente direcionar excedentes para reservas. |
| `NAO_CLASSIFICADO` | `nonePct > 20%` | Atenção: Gastos Não Classificados | Mais de 20% sem classificação de natureza. |
| `EXCELENTE` | `essentialsPct <= 60%`, `mandatoryPct <= 50%`, `wantsPct in 10%..35%`, `savingsPct >= 15%`, `nonePct <= 10%` | Excelente: Padrão 50-30-20 Equilibrado | Distribuição exemplar no padrão 50-30-20 com margem de segurança robusta. |
| `EQUILIBRADO` | Gastos sob controle e poupança ativa (`savingsPct >= 5%`) | Bom: Orçamento em Equilíbrio | Distribuição saudável com poupança ativa em direção ao ideal 50-30-20. |

### 4. Interface Elegante (`NatureDistributionCard`)
- O card em `StatisticsScreen` exibe explicitamente:
  - Banner semafórico com `badgeLabel` em negrito e diagnóstico contextual.
  - Barras das 4 naturezas de despesas com identificadores visuais.
  - Barra dedicada de **"Poupança (Metas)"** com cor esmeralda `#10B981`, valor monetário formatado e percentual.
  - Divisor e linha de resumo com o **Total Orçado (Despesas + Aportes)**.

## Consequências
- Fiel alinhamento à regra universal 50-30-20 no controle financeiro pessoal.
- Eliminação definitiva de diagnósticos ilusórios de "Excelente" quando não há poupança ou estilo de vida.
- Cobertura por testes unitários cobrindo 100% dos cenários (0% Deseja/Poupança, 50-30-20 real, thresholds de fronteira).
- Totalmente em conformidade com o princípio offline-first e persistência atômica local no Room.
