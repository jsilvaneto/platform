package com.platform.app.presentation.settings

import com.platform.app.core.mvi.UiAction

sealed interface SettingsUiAction : UiAction {
    data class ToggleBiometric(val enabled: Boolean) : SettingsUiAction
    data class SetThemeMode(val isDarkMode: Boolean?) : SettingsUiAction
    object Refresh : SettingsUiAction
}
