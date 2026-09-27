package com.platform.app.data.repository

import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.entity.GoalEntity
import com.platform.app.domain.model.Goal
import com.platform.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getGoals(): Flow<List<Goal>> {
        return goalDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveGoal(goal: Goal) {
        goalDao.insert(GoalEntity.fromDomain(goal))
    }

    override suspend fun deleteGoal(goalId: String) {
        goalDao.deleteById(goalId)
    }

    override suspend fun addContribution(goalId: String, amountCents: Long) {
        goalDao.addContribution(goalId, amountCents)
    }
}
