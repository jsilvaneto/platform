package com.platform.app.presentation.settings

import com.platform.app.core.mvi.UiEffect

sealed interface SettingsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : SettingsUiEffect
    data class RequestBiometricAuthForToggle(val targetState: Boolean) : SettingsUiEffect
}
