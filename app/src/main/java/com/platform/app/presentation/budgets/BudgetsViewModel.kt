package com.platform.app.presentation.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Budget
import com.platform.app.domain.repository.BudgetRepository
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<BudgetsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<BudgetsUiEffect> = _effectChannel.receiveAsFlow()

    init {
        loadBudgetsAndExpenses()
    }

    fun onAction(action: BudgetsUiAction) {
        when (action) {
            is BudgetsUiAction.SaveBudget -> handleSaveBudget(action.budget)
            is BudgetsUiAction.DeleteBudget -> handleDeleteBudget(action.budgetId)
            is BudgetsUiAction.Refresh -> loadBudgetsAndExpenses()
        }
    }

    private fun loadBudgetsAndExpenses() {
        _uiState.update { it.copy(isLoading = true) }

        val now = System.currentTimeMillis()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)

        combine(
            budgetRepository.getBudgets(),
            financialRepository.getInstallmentsForPeriod(startOfMonth, endOfMonth),
            financialRepository.getCategories()
        ) { budgets, installments, categories ->
            val budgetsWithSpend = budgets.map { budget ->
                val spent = installments
                    .filter { inst ->
                        if (budget.categoryId != null) {
                            inst.categoryId == budget.categoryId
                        } else {
                            true
                        }
                    }
                    .sumOf { it.amountCents }

                val progress = if (budget.limitAmountCents > 0L) {
                    (spent.toFloat() / budget.limitAmountCents.toFloat()).coerceIn(0f, 2f)
                } else 0f

                BudgetWithSpend(
                    budget = budget,
                    spentCents = spent,
                    progress = progress,
                    isExceeded = spent > budget.limitAmountCents
                )
            }

            val totalLimit = budgets.sumOf { it.limitAmountCents }
            val totalSpent = budgetsWithSpend.sumOf { it.spentCents }
            val overallProgress = if (totalLimit > 0L) (totalSpent.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f) else 0f

            _uiState.update {
                it.copy(
                    budgets = budgetsWithSpend,
                    categories = categories,
                    totalLimitCents = totalLimit,
                    totalSpentCents = totalSpent,
                    overallProgress = overallProgress,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }.catch { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Erro ao carregar orçamentos."
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun handleSaveBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.saveBudget(budget)
                _effectChannel.send(BudgetsUiEffect.ShowSnackbar("Orçamento para '${budget.categoryName}' salvo!"))
                _effectChannel.send(BudgetsUiEffect.BudgetSaved)
            } catch (e: Exception) {
                _effectChannel.send(BudgetsUiEffect.ShowSnackbar("Erro ao salvar orçamento: ${e.message}"))
            }
        }
    }

    private fun handleDeleteBudget(budgetId: String) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budgetId)
                _effectChannel.send(BudgetsUiEffect.ShowSnackbar("Orçamento excluído."))
            } catch (e: Exception) {
                _effectChannel.send(BudgetsUiEffect.ShowSnackbar("Erro ao excluir orçamento: ${e.message}"))
            }
        }
    }
}
