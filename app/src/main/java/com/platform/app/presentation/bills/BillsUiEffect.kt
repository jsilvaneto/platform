package com.platform.app.presentation.bills

import com.platform.app.core.mvi.UiEffect

sealed interface BillsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : BillsUiEffect
}
