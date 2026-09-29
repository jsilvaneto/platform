package com.platform.app.presentation.creditcards

import com.platform.app.domain.model.CreditCard

sealed interface CreditCardsUiAction {
    data class SelectCard(val cardId: String) : CreditCardsUiAction
    data class SelectMonth(val referenceMonth: String) : CreditCardsUiAction
    data class SaveCard(val card: CreditCard) : CreditCardsUiAction
    data class DeleteCard(val cardId: String) : CreditCardsUiAction
    data class PayInvoice(val invoiceId: String) : CreditCardsUiAction
}
