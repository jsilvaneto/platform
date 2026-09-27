package com.platform.app.domain.repository

import com.platform.app.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(): Flow<List<Budget>>
    suspend fun saveBudget(budget: Budget)
    suspend fun deleteBudget(budgetId: String)
}
