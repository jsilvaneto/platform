package com.platform.app.presentation.creditcards

import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary

data class CreditCardsUiState(
    val cardsWithSummary: List<CreditCardWithInvoiceSummary> = emptyList(),
    val selectedCardId: String? = null,
    val selectedReferenceMonth: String = "",
    val invoicesForSelectedCard: List<CreditCardInvoice> = emptyList(),
    val currentInvoice: CreditCardInvoice? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
