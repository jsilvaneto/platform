package com.platform.app.presentation.settings

import com.platform.app.core.mvi.UiState

data class SettingsUiState(
    val isBiometricSupported: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isDarkMode: Boolean? = null,
    val appVersionName: String = "1.0.0",
    val appVersionCode: Int = 1,
    val lastBackupTimestamp: Long = 0L,
    val isLoading: Boolean = false
) : UiState
