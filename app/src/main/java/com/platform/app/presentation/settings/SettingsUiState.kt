package com.platform.app.presentation.settings

import com.platform.app.core.mvi.UiState

data class SettingsUiState(
    val isBiometricSupported: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 0,
    val hideContentInRecents: Boolean = true,
    val isDarkMode: Boolean? = null,
    val isAmoledMode: Boolean = false,
    val appIcon: String = "classic",
    val appVersionName: String = "1.0.0",
    val appVersionCode: Int = 1,
    val lastBackupTimestamp: Long = 0L,
    val accountsCount: Int = 0,
    val paymentMethodsCount: Int = 0,
    val categoriesCount: Int = 0,
    val isLoading: Boolean = false
) : UiState
