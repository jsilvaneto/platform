package com.platform.app.presentation.contacts

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.Contact
import com.platform.app.domain.usecase.ContactDetails

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedContactDetails: ContactDetails? = null,
    val isDetailsLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
