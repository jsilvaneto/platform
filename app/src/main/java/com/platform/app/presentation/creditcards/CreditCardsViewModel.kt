package com.platform.app.presentation.creditcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CardDependencies
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.platform.app.domain.usecase.GetCreditCardSummariesUseCase
import javax.inject.Inject

@HiltViewModel
class CreditCardsViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val getCreditCardSummariesUseCase: GetCreditCardSummariesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditCardsUiState())
    val uiState: StateFlow<CreditCardsUiState> = _uiState.asStateFlow()

    private var invoiceDetailsJob: Job? = null

    init {
        loadData()
    }

    fun onAction(action: CreditCardsUiAction) {
        when (action) {
            is CreditCardsUiAction.SelectCard -> {
                _uiState.update { it.copy(selectedCardId = action.cardId, selectedInvoiceForDetails = null) }
                loadData()
            }
            is CreditCardsUiAction.SelectMonth -> {
                _uiState.update { it.copy(selectedReferenceMonth = action.referenceMonth) }
            }
            is CreditCardsUiAction.SelectInvoiceForDetails -> {
                _uiState.update { it.copy(selectedInvoiceForDetails = action.invoice) }
                invoiceDetailsJob?.cancel()
                if (action.invoice != null) {
                    invoiceDetailsJob = repository.getInstallmentsForInvoice(action.invoice.id)
                        .combine(repository.getAllInstallments()) { invoiceInsts, _ ->
                            invoiceInsts
                        }
                        .launchIn(viewModelScope)
                    viewModelScope.launch {
                        repository.getInstallmentsForInvoice(action.invoice.id).collect { insts ->
                            _uiState.update { it.copy(installmentsForSelectedInvoice = insts) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(installmentsForSelectedInvoice = emptyList()) }
                }
            }
            is CreditCardsUiAction.SetInvoiceFilter -> {
                _uiState.update { it.copy(invoiceFilter = action.filter) }
            }
            is CreditCardsUiAction.SaveCard -> {
                viewModelScope.launch {
                    repository.saveCreditCard(action.card)
                    _uiState.update { it.copy(cardToEdit = null) }
                }
            }
            is CreditCardsUiAction.OpenEditCard -> {
                _uiState.update { it.copy(cardToEdit = action.card) }
            }
            is CreditCardsUiAction.CloseEditCard -> {
                _uiState.update { it.copy(cardToEdit = null) }
            }
            is CreditCardsUiAction.RequestDeleteCard -> {
                viewModelScope.launch {
                    val deps = repository.getCardDependencies(action.cardId)
                    _uiState.update {
                        it.copy(
                            cardToDeleteId = action.cardId,
                            pendingDeleteDependencies = deps
                        )
                    }
                }
            }
            is CreditCardsUiAction.ConfirmDeleteCard -> {
                val cardId = _uiState.value.cardToDeleteId
                if (cardId != null) {
                    viewModelScope.launch {
                        repository.deleteCreditCard(cardId)
                        val remaining = _uiState.value.cardsWithSummary.filter { it.card.id != cardId }
                        _uiState.update {
                            it.copy(
                                cardToDeleteId = null,
                                pendingDeleteDependencies = null,
                                selectedCardId = remaining.firstOrNull()?.card?.id,
                                selectedInvoiceForDetails = null
                            )
                        }
                    }
                }
            }
            is CreditCardsUiAction.CancelDeleteCard -> {
                _uiState.update { it.copy(cardToDeleteId = null, pendingDeleteDependencies = null) }
            }
            is CreditCardsUiAction.PayInvoice -> {
                viewModelScope.launch {
                    repository.payInvoice(action.invoiceId, action.actualPaymentDate)
                    // Se estiver com os detalhes da fatura aberta, atualiza
                    val currentDetails = _uiState.value.selectedInvoiceForDetails
                    if (currentDetails?.id == action.invoiceId) {
                        _uiState.update {
                            it.copy(
                                selectedInvoiceForDetails = currentDetails.copy(status = InvoiceStatus.PAGA)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            getCreditCardSummariesUseCase(),
            repository.getAllCreditCardInvoices(),
            repository.getAllInstallments()
        ) { summaries, allInvoices, installments ->
            val currentSelected = _uiState.value.selectedCardId ?: summaries.firstOrNull()?.card?.id
            val cardInvoices = if (currentSelected != null) {
                allInvoices.filter { it.creditCardId == currentSelected }
                    .sortedByDescending { it.referenceMonth }
                    .map { invoice ->
                        val invTotal = installments
                            .filter { it.invoiceId == invoice.id }
                            .sumOf { it.amountCents }
                        invoice.copy(totalAmountCents = invTotal)
                    }
            } else emptyList()

            val selectedSummary = summaries.find { it.card.id == currentSelected }
            val currentInvoice = selectedSummary?.currentInvoice
                ?: cardInvoices.firstOrNull { it.status != InvoiceStatus.PAGA }
                ?: cardInvoices.firstOrNull()

            _uiState.update {
                it.copy(
                    cardsWithSummary = summaries,
                    selectedCardId = currentSelected,
                    invoicesForSelectedCard = cardInvoices,
                    currentInvoice = currentInvoice,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }
}
