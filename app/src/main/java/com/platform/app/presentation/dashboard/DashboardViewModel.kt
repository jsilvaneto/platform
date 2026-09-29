package com.platform.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.GetFinancialDashboardUseCase
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardUseCase: GetFinancialDashboardUseCase,
    private val togglePaymentUseCase: ToggleInstallmentPaymentUseCase,
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCategoriesIfEmpty()
        }
        loadMetricsForMonth(_uiState.value.selectedMonthMillis)
    }

    fun onAction(action: DashboardUiAction) {
        when (action) {
            is DashboardUiAction.PreviousMonth -> {
                val newMonth = DateUtils.addMonths(_uiState.value.selectedMonthMillis, -1)
                loadMetricsForMonth(newMonth)
            }
            is DashboardUiAction.NextMonth -> {
                val newMonth = DateUtils.addMonths(_uiState.value.selectedMonthMillis, 1)
                loadMetricsForMonth(newMonth)
            }
            is DashboardUiAction.CurrentMonth -> {
                loadMetricsForMonth(System.currentTimeMillis())
            }
            is DashboardUiAction.SelectMonth -> {
                loadMetricsForMonth(action.monthMillis)
            }
            is DashboardUiAction.SelectTab -> {
                _uiState.update { it.copy(selectedTab = action.tab) }
            }
            is DashboardUiAction.TogglePrivacyMode -> {
                _uiState.update { it.copy(isPrivacyMode = !it.isPrivacyMode) }
            }
            is DashboardUiAction.ChangeViewMode -> {
                _uiState.update { it.copy(viewMode = action.mode) }
            }
            is DashboardUiAction.TogglePayment -> {
                viewModelScope.launch {
                    togglePaymentUseCase(action.installmentId, action.currentPaid)
                }
            }
            is DashboardUiAction.Refresh -> {
                loadMetricsForMonth(_uiState.value.selectedMonthMillis)
            }
        }
    }

    private fun loadMetricsForMonth(monthMillis: Long) {
        _uiState.update { it.copy(selectedMonthMillis = monthMillis, isLoading = true) }

        getDashboardUseCase(monthMillis)
            .onEach { metrics ->
                _uiState.update {
                    it.copy(
                        metrics = metrics,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar métricas do dashboard."
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
