package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.GoalContributionDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.GoalContributionEntity
import com.platform.app.data.local.entity.GoalEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
import com.platform.app.domain.model.Goal
import com.platform.app.domain.model.GoalContribution
import com.platform.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val database: PlatformDatabase,
    private val goalDao: GoalDao,
    private val goalContributionDao: GoalContributionDao,
    private val syncDao: SyncDao? = null
) : GoalRepository {

    override fun getGoals(): Flow<List<Goal>> {
        return goalDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getMonthlyContribution(startDate: Long, endDate: Long): Flow<Long> {
        return goalContributionDao.sumForPeriod(startDate, endDate)
    }

    override fun getContributionsForPeriod(startDate: Long, endDate: Long): Flow<List<GoalContribution>> {
        return goalContributionDao.getForPeriod(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override fun getContributionsForPeriod(goalId: String, startDate: Long, endDate: Long): Flow<List<GoalContribution>> {
        return goalContributionDao.getByGoalForPeriod(goalId, startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveGoal(goal: Goal) {
        database.withTransaction {
            val existing = goalDao.getById(goal.id)
            goalDao.insert(GoalEntity.fromDomain(goal))
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "GOAL",
                    entityId = goal.id,
                    operation = if (existing == null) "INSERT" else "UPDATE",
                    payload = """{"id":"${goal.id}","name":"${goal.name}","targetAmountCents":${goal.targetAmountCents}}"""
                )
            )

            if (existing == null && goal.currentAmountCents > 0L) {
                val contributionId = UUID.randomUUID().toString()
                goalContributionDao.insert(
                    GoalContributionEntity(
                        id = contributionId,
                        goalId = goal.id,
                        amountCents = goal.currentAmountCents,
                        date = goal.createdAt
                    )
                )
                val totalCents = goalContributionDao.sumByGoal(goal.id)
                goalDao.updateCurrentAmount(goal.id, totalCents)

                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "GOAL_CONTRIBUTION",
                        entityId = contributionId,
                        operation = "INSERT",
                        payload = """{"id":"$contributionId","goalId":"${goal.id}","amountCents":${goal.currentAmountCents}}"""
                    )
                )
            }
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        database.withTransaction {
            goalContributionDao.deleteByGoalId(goalId)
            goalDao.deleteById(goalId)
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "GOAL",
                    entityId = goalId,
                    operation = "DELETE",
                    payload = """{"id":"$goalId"}"""
                )
            )
        }
    }

    override suspend fun addContribution(goalId: String, amountCents: Long, date: Long) {
        database.withTransaction {
            val contributionId = UUID.randomUUID().toString()
            goalContributionDao.insert(
                GoalContributionEntity(
                    id = contributionId,
                    goalId = goalId,
                    amountCents = amountCents,
                    date = date
                )
            )
            val totalCents = goalContributionDao.sumByGoal(goalId)
            goalDao.updateCurrentAmount(goalId, totalCents)

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "GOAL_CONTRIBUTION",
                    entityId = contributionId,
                    operation = "INSERT",
                    payload = """{"id":"$contributionId","goalId":"$goalId","amountCents":$amountCents}"""
                )
            )
        }
    }
}
