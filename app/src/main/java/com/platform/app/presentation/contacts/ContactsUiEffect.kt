package com.platform.app.presentation.contacts

import com.platform.app.core.mvi.UiEffect

sealed interface ContactsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : ContactsUiEffect
    object ContactSaved : ContactsUiEffect
}
