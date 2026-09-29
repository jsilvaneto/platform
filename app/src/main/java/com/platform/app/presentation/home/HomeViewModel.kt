package com.platform.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CalculateMonthlyForecastUseCase
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
class HomeViewModel @Inject constructor(
    private val calculateMonthlyForecastUseCase: CalculateMonthlyForecastUseCase,
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCategoriesIfEmpty()
            repository.seedInitialCreditCardsIfEmpty()
            repository.seedInitialExpenseItemsIfEmpty()
        }
        loadForecast(_uiState.value.selectedMonthMillis)
    }

    fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.PreviousMonth -> {
                val newMonth = DateUtils.addMonths(_uiState.value.selectedMonthMillis, -1)
                loadForecast(newMonth)
            }
            is HomeUiAction.NextMonth -> {
                val newMonth = DateUtils.addMonths(_uiState.value.selectedMonthMillis, 1)
                loadForecast(newMonth)
            }
            is HomeUiAction.CurrentMonth -> {
                loadForecast(System.currentTimeMillis())
            }
            is HomeUiAction.PayBill -> {
                viewModelScope.launch {
                    repository.toggleInstallmentPayment(
                        installmentId = action.installmentId,
                        isPaid = true,
                        paidTimestamp = System.currentTimeMillis()
                    )
                }
            }
            is HomeUiAction.PayInvoice -> {
                viewModelScope.launch {
                    repository.payInvoice(action.invoiceId)
                }
            }
            is HomeUiAction.TogglePaidSection -> {
                _uiState.update { it.copy(isPaidSectionExpanded = action.expanded) }
            }
            is HomeUiAction.Refresh -> {
                loadForecast(_uiState.value.selectedMonthMillis)
            }
        }
    }

    private fun loadForecast(monthMillis: Long) {
        _uiState.update { it.copy(selectedMonthMillis = monthMillis, isLoading = true) }

        calculateMonthlyForecastUseCase(monthMillis)
            .onEach { result ->
                _uiState.update {
                    it.copy(
                        forecastResult = result,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao calcular previsibilidade do mês."
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
