package com.platform.app.presentation.creditcards

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CardDependencies
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus

enum class InvoiceFilter {
    ALL, OPEN, CLOSED, PAID
}

data class CreditCardsUiState(
    val cardsWithSummary: List<CreditCardWithInvoiceSummary> = emptyList(),
    val selectedCardId: String? = null,
    val selectedReferenceMonth: String? = null,
    val invoicesForSelectedCard: List<CreditCardInvoice> = emptyList(),
    val currentInvoice: CreditCardInvoice? = null,
    val selectedInvoiceForDetails: CreditCardInvoice? = null,
    val installmentsForSelectedInvoice: List<BillInstallment> = emptyList(),
    val invoiceFilter: InvoiceFilter = InvoiceFilter.ALL,
    val pendingDeleteDependencies: CardDependencies? = null,
    val cardToDeleteId: String? = null,
    val cardToEdit: CreditCard? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedCardSummary: CreditCardWithInvoiceSummary?
        get() = cardsWithSummary.find { it.card.id == selectedCardId } ?: cardsWithSummary.firstOrNull()

    val filteredInvoices: List<CreditCardInvoice>
        get() = when (invoiceFilter) {
            InvoiceFilter.ALL -> invoicesForSelectedCard
            InvoiceFilter.OPEN -> invoicesForSelectedCard.filter { it.status == InvoiceStatus.ABERTA }
            InvoiceFilter.CLOSED -> invoicesForSelectedCard.filter { it.status == InvoiceStatus.FECHADA }
            InvoiceFilter.PAID -> invoicesForSelectedCard.filter { it.status == InvoiceStatus.PAGA }
        }
}
