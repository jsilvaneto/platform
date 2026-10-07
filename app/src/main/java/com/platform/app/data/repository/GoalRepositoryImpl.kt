package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.entity.GoalContributionEntity
import com.platform.app.data.local.entity.GoalEntity
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
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getGoals(): Flow<List<Goal>> {
        return goalDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getMonthlyContribution(startDate: Long, endDate: Long): Flow<Long> {
        return goalDao.getMonthlyContributionSum(startDate, endDate)
    }

    override fun getContributionsForPeriod(startDate: Long, endDate: Long): Flow<List<GoalContribution>> {
        return goalDao.getContributionsForPeriod(startDate, endDate).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveGoal(goal: Goal) {
        database.withTransaction {
            val existing = goalDao.getById(goal.id)
            goalDao.insert(GoalEntity.fromDomain(goal))
            if (existing == null && goal.currentAmountCents > 0L) {
                goalDao.insertContribution(
                    GoalContributionEntity(
                        id = UUID.randomUUID().toString(),
                        goalId = goal.id,
                        amountCents = goal.currentAmountCents,
                        date = goal.createdAt
                    )
                )
            }
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        goalDao.deleteById(goalId)
    }

    override suspend fun addContribution(goalId: String, amountCents: Long, date: Long) {
        database.withTransaction {
            goalDao.addContribution(goalId, amountCents)
            goalDao.insertContribution(
                GoalContributionEntity(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    amountCents = amountCents,
                    date = date
                )
            )
        }
    }
}
