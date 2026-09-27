package com.platform.app.presentation.home

import com.platform.app.core.mvi.UiEffect

sealed interface HomeUiEffect : UiEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : HomeUiEffect
    data class NavigateToDetails(val itemId: String) : HomeUiEffect
}
