package com.platform.app.domain.repository

import com.platform.app.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<List<Goal>>
    suspend fun saveGoal(goal: Goal)
    suspend fun deleteGoal(goalId: String)
    suspend fun addContribution(goalId: String, amountCents: Long)
}
