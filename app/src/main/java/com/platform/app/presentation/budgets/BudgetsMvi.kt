package com.platform.app.presentation.budgets

import com.platform.app.core.mvi.UiAction
import com.platform.app.core.mvi.UiEffect
import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.Budget
import com.platform.app.domain.model.Category

data class BudgetWithSpend(
    val budget: Budget,
    val spentCents: Long,
    val progress: Float,
    val isExceeded: Boolean
)

data class BudgetsUiState(
    val budgets: List<BudgetWithSpend> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalLimitCents: Long = 0L,
    val totalSpentCents: Long = 0L,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface BudgetsUiAction : UiAction {
    data class SaveBudget(val budget: Budget) : BudgetsUiAction
    data class DeleteBudget(val budgetId: String) : BudgetsUiAction
    object Refresh : BudgetsUiAction
}

sealed interface BudgetsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : BudgetsUiEffect
    object BudgetSaved : BudgetsUiEffect
}
