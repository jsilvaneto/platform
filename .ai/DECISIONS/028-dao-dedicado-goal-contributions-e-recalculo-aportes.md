# ADR 028: DAO Dedicado para Goal Contributions, Recálculo Derivado e Histórico Temporal de Metas

## Status
Aprovado

## Contexto
O módulo de metas financeiras (`goals`) gerenciava o progresso das metas financeiras através de um valor escalar acumulado (`currentAmountCents`). Embora a tabela relacional `goal_contributions` tenha sido introduzida na v1.4.2 (v11 do banco), as operações de banco de dados permaneciam acopladas dentro de `GoalDao`, sem um DAO dedicado e sem recálculo automatizado do cache agregado.

Esse cenário apresentava limitações:
1. **Violação do Princípio da Responsabilidade Única (SRP)**: `GoalDao` acumulava responsabilidades sobre as metas e sobre os registros individuais de aportes.
2. **Ausência de Filtro por Meta e Período**: O repositório expunha apenas consultas agregadas globais do mês, sem disponibilizar `getContributionsForPeriod(goalId, start, end)` para extratos individuais de uma meta em um determinado período.
3. **Risco de Dessincronização do Cache**: Incrementos manuais diretos (`+= amountCents`) podiam divergir caso uma contribuição fosse excluída ou alterada.

## Decisão

### 1. Criação do DAO Próprio (`GoalContributionDao`)
Criada a interface dedicada [GoalContributionDao.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/dao/GoalContributionDao.kt), contendo:
- `insert(contribution: GoalContributionEntity)`
- `getByGoalForPeriod(goalId, startDate, endDate): Flow<List<GoalContributionEntity>>`
- `getForPeriod(startDate, endDate): Flow<List<GoalContributionEntity>>`
- `getByGoal(goalId): Flow<List<GoalContributionEntity>>`
- `sumByGoal(goalId): Long`
- `sumForPeriod(startDate, endDate): Flow<Long>`
- `getAll()`, `insertAll()`, `deleteByGoalId(goalId)` e `deleteAll()`

O DAO foi registrado como propriedade abstrata em [PlatformDatabase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/data/local/PlatformDatabase.kt) e provido como Singleton em [AppModule.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/di/AppModule.kt).

### 2. Recálculo Atômico do Cache `currentAmountCents`
O campo `currentAmountCents` na tabela `goals` é mantido como cache derivado de leitura rápida para a UI, sendo recalculado transacionalmente a partir da soma real das contribuições da meta (`sumByGoal`):
```kotlin
override suspend fun addContribution(goalId: String, amountCents: Long, date: Long) {
    database.withTransaction {
        goalContributionDao.insert(
            GoalContributionEntity(
                id = UUID.randomUUID().toString(),
                goalId = goalId,
                amountCents = amountCents,
                date = date
            )
        )
        val totalCents = goalContributionDao.sumByGoal(goalId)
        goalDao.updateCurrentAmount(goalId, totalCents)
    }
}
```

### 3. Exposição de Consulta Granular por Meta
[GoalRepository.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/repository/GoalRepository.kt) agora expõe:
```kotlin
fun getContributionsForPeriod(goalId: String, startDate: Long, endDate: Long): Flow<List<GoalContribution>>
```
Permitindo extrair aportes individuais de uma meta em qualquer janela temporal.

### 4. Compatibilidade de Schema Room
A tabela `goal_contributions` já existia no banco desde a versão 11 com as colunas `id (PK)`, `goalId (FK CASCADE)`, `amountCents` e `date`, além de índices em `goalId` e `date`. Portanto, a extração do DAO não altera o DDL do SQLite, preservando integridade sem necessidade de nova migração de versão do banco.

## Consequências

### Positivas
- Código limpo e desacoplado, com responsabilidades bem definidas entre `GoalDao` e `GoalContributionDao`.
- Garantia matemática de integridade: `currentAmountCents` reflete com 100% de exatidão a soma das contribuições.
- Suporte imediato a relatórios temporais de poupança (regra 50-30-20) e históricos visuais de aportes.
