package com.platform.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.BuildConfig
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isBiometricSupported = biometricAuthManager.canAuthenticate(),
            appVersionName = runCatching { BuildConfig.VERSION_NAME }.getOrDefault("1.0.0"),
            appVersionCode = runCatching { BuildConfig.VERSION_CODE }.getOrDefault(1)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<SettingsUiEffect> = _effectChannel.receiveAsFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        combine(
            preferencesManager.isBiometricEnabled,
            preferencesManager.isDarkMode,
            preferencesManager.lastOfflineBackupTimestamp
        ) { isBioEnabled, isDark, lastBackup ->
            _uiState.update { current ->
                current.copy(
                    isBiometricEnabled = isBioEnabled,
                    isDarkMode = isDark,
                    lastBackupTimestamp = lastBackup,
                    isBiometricSupported = biometricAuthManager.canAuthenticate()
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ToggleBiometric -> handleToggleBiometric(action.enabled)
            is SettingsUiAction.SetThemeMode -> handleSetThemeMode(action.isDarkMode)
            is SettingsUiAction.Refresh -> observePreferences()
        }
    }

    private fun handleToggleBiometric(targetEnabled: Boolean) {
        if (targetEnabled && !_uiState.value.isBiometricSupported) {
            viewModelScope.launch {
                _effectChannel.send(
                    SettingsUiEffect.ShowSnackbar("Este dispositivo não possui biometria ou bloqueio de tela configurado.")
                )
            }
            return
        }

        viewModelScope.launch {
            _effectChannel.send(SettingsUiEffect.RequestBiometricAuthForToggle(targetEnabled))
        }
    }

    fun confirmBiometricToggle(targetEnabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(targetEnabled)
            val msg = if (targetEnabled)
                "Bloqueio por biometria/senha ativado!"
            else
                "Bloqueio desativado."
            _effectChannel.send(SettingsUiEffect.ShowSnackbar(msg))
        }
    }

    private fun handleSetThemeMode(isDarkMode: Boolean?) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(isDarkMode)
        }
    }
}
