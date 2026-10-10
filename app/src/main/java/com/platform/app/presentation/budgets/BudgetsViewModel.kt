package com.platform.app.presentation.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Budget
import com.platform.app.domain.repository.BudgetRepository
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.GetBudgetProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val financialRepository: FinancialRepository,
    private val getBudgetProgressUseCase: GetBudgetProgressUseCase
) : ViewModel() {

    private val selectedMonthMillis = MutableStateFlow(System.currentTimeMillis())

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<BudgetsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<BudgetsUiEffect> = _effectChannel.receiveAsFlow()

    init {
        observeBudgetsAndExpenses()
    }

    fun onAction(action: BudgetsUiAction) {
        when (action) {
            is BudgetsUiAction.SaveBudget -> handleSaveBudget(action.budget)
            is BudgetsUiAction.DeleteBudget -> handleDeleteBudget(action.budgetId)
            is BudgetsUiAction.ChangeMonth -> selectedMonthMillis.value = action.targetMonthMillis
            is BudgetsUiAction.PreviousMonth -> {
                selectedMonthMillis.value = DateUtils.addMonths(selectedMonthMillis.value, -1)
            }
            is BudgetsUiAction.NextMonth -> {
                selectedMonthMillis.value = DateUtils.addMonths(selectedMonthMillis.value, 1)
            }
            is BudgetsUiAction.Refresh -> observeBudgetsAndExpenses()
        }
    }

    private fun observeBudgetsAndExpenses() {
        _uiState.update { it.copy(isLoading = true) }

        selectedMonthMillis
            .flatMapLatest { monthMillis ->
                combine(
                    getBudgetProgressUseCase(monthMillis),
                    financialRepository.getCategories()
                ) { overview, categories ->
                    val uiBudgets = overview.budgets.map { item ->
                        BudgetWithSpend(
                            budget = item.budget,
                            spentCents = item.spentCents,
                            paidCents = item.paidCents,
                            pendingCents = item.pendingCents,
                            progress = item.progress,
                            isExceeded = item.isExceeded
                        )
                    }

                    _uiState.update { current ->
                        current.copy(
                            budgets = uiBudgets,
                            categories = categories,
                            selectedMonthMillis = monthMillis,
                            totalLimitCents = overview.totalLimitCents,
                            totalSpentCents = overview.totalSpentCents,
                            totalPaidCents = overview.totalPaidCents,
                            totalPendingCents = overview.totalPendingCents,
                            overallProgress = overview.overallProgress,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar orçamentos."
                    )
                }
            }
            .launchIn(viewModelScope)
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
