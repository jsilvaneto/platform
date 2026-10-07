package com.platform.app.domain.repository

import com.platform.app.domain.model.Goal
import com.platform.app.domain.model.GoalContribution
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<List<Goal>>
    fun getMonthlyContribution(startDate: Long, endDate: Long): Flow<Long>
    fun getContributionsForPeriod(startDate: Long, endDate: Long): Flow<List<GoalContribution>>
    fun getContributionsForPeriod(goalId: String, startDate: Long, endDate: Long): Flow<List<GoalContribution>>
    suspend fun saveGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)
    suspend fun addContribution(goalId: String, amountCents: Long, date: Long = System.currentTimeMillis())
}
