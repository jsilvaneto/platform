package com.platform.app.presentation.management

import com.platform.app.core.mvi.UiEffect

sealed interface ManagementUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : ManagementUiEffect
    object ItemSaved : ManagementUiEffect
}
