package com.platform.app.presentation.creditcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditCardsViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditCardsUiState())
    val uiState: StateFlow<CreditCardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCreditCardsIfEmpty()
        }
        loadData()
    }

    fun onAction(action: CreditCardsUiAction) {
        when (action) {
            is CreditCardsUiAction.SelectCard -> {
                _uiState.update { it.copy(selectedCardId = action.cardId) }
                loadData()
            }
            is CreditCardsUiAction.SelectMonth -> {
                _uiState.update { it.copy(selectedReferenceMonth = action.referenceMonth) }
            }
            is CreditCardsUiAction.SaveCard -> {
                viewModelScope.launch {
                    repository.saveCreditCard(action.card)
                }
            }
            is CreditCardsUiAction.DeleteCard -> {
                viewModelScope.launch {
                    repository.deleteCreditCard(action.cardId)
                }
            }
            is CreditCardsUiAction.PayInvoice -> {
                viewModelScope.launch {
                    repository.payInvoice(action.invoiceId)
                }
            }
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            repository.getCreditCards(),
            repository.getAllInstallments()
        ) { cards, installments ->
            val summaries = cards.map { card ->
                val activeInvoiceInsts = installments.filter { it.invoiceId != null && !it.isPaid }
                val usedLimit = activeInvoiceInsts.sumOf { it.amountCents }
                CreditCardWithInvoiceSummary(
                    card = card,
                    currentInvoice = null,
                    usedLimitCents = usedLimit
                )
            }
            val currentSelected = _uiState.value.selectedCardId ?: cards.firstOrNull()?.id
            _uiState.update {
                it.copy(
                    cardsWithSummary = summaries,
                    selectedCardId = currentSelected,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }
}
