package com.platform.app.presentation.settings

import android.net.Uri
import com.platform.app.core.mvi.UiAction

sealed interface SettingsUiAction : UiAction {
    data class ToggleBiometric(val enabled: Boolean) : SettingsUiAction
    data class SetThemeMode(val isDarkMode: Boolean?) : SettingsUiAction
    data class ExportBackupToUri(val uri: Uri) : SettingsUiAction
    data class RestoreBackupFromUri(val uri: Uri) : SettingsUiAction
    object ShareBackup : SettingsUiAction
    object Refresh : SettingsUiAction
}
