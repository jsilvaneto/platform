package com.platform.app.presentation.settings

import android.net.Uri
import com.platform.app.core.mvi.UiAction

sealed interface SettingsUiAction : UiAction {
    data class ToggleBiometric(val enabled: Boolean) : SettingsUiAction
    data class SetLockTimeoutSeconds(val seconds: Int) : SettingsUiAction
    data class SetHideContentInRecents(val enabled: Boolean) : SettingsUiAction
    data class SetThemeMode(val isDarkMode: Boolean?) : SettingsUiAction
    data class SetAmoledMode(val enabled: Boolean) : SettingsUiAction
    data class SetAppIcon(val iconKey: String) : SettingsUiAction
    data class ExportBackupToUri(val uri: Uri, val password: String) : SettingsUiAction
    data class RestoreBackupFromUri(val uri: Uri, val password: String) : SettingsUiAction
    data class ShareBackup(val password: String) : SettingsUiAction
    object Refresh : SettingsUiAction
}
