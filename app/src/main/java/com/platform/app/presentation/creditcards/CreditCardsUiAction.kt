package com.platform.app.presentation.creditcards

import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice

sealed interface CreditCardsUiAction {
    data class SelectCard(val cardId: String) : CreditCardsUiAction
    data class SelectMonth(val referenceMonth: String) : CreditCardsUiAction
    data class SelectInvoiceForDetails(val invoice: CreditCardInvoice?) : CreditCardsUiAction
    data class SetInvoiceFilter(val filter: InvoiceFilter) : CreditCardsUiAction
    data class SaveCard(val card: CreditCard) : CreditCardsUiAction
    data class OpenEditCard(val card: CreditCard) : CreditCardsUiAction
    data object CloseEditCard : CreditCardsUiAction
    data class RequestDeleteCard(val cardId: String) : CreditCardsUiAction
    data object ConfirmDeleteCard : CreditCardsUiAction
    data object CancelDeleteCard : CreditCardsUiAction
    data class PayInvoice(val invoiceId: String) : CreditCardsUiAction
}
