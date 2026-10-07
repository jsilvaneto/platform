package com.platform.app.presentation.contacts

import com.platform.app.core.mvi.UiAction
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.ContactType

sealed interface ContactsUiAction : UiAction {
    data class SaveContact(val contact: Contact) : ContactsUiAction
    data class DeleteContact(val contactId: String) : ContactsUiAction
    data class SearchQueryChanged(val query: String) : ContactsUiAction
    data class FilterTypeSelected(val type: ContactType?) : ContactsUiAction
    data class LoadContactDetails(val contactId: String) : ContactsUiAction
    data class ToggleInstallmentPayment(
        val installmentId: String,
        val isPaid: Boolean,
        val actualPaymentDate: Long? = null
    ) : ContactsUiAction
    object Refresh : ContactsUiAction
}
