package com.platform.app.data.repository

import com.platform.app.data.local.dao.BudgetDao
import com.platform.app.data.local.entity.BudgetEntity
import com.platform.app.domain.model.Budget
import com.platform.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgets(): Flow<List<Budget>> {
        return budgetDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveBudget(budget: Budget) {
        budgetDao.insert(BudgetEntity.fromDomain(budget))
    }

    override suspend fun deleteBudget(budgetId: String) {
        budgetDao.deleteById(budgetId)
    }
}
