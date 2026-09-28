package com.platform.app.presentation.settings

import android.content.Context
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.BuildConfig
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.ExportBackupUseCase
import com.platform.app.domain.usecase.RestoreBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val biometricAuthManager: BiometricAuthManager,
    private val financialRepository: FinancialRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    @ApplicationContext private val context: Context
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
        observeData()
    }

    private fun observeData() {
        observePreferences()
        observeEntityCounts()
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

    private fun observeEntityCounts() {
        combine(
            financialRepository.getFinancialAccounts(),
            financialRepository.getPaymentMethods(),
            financialRepository.getCategories()
        ) { accounts, paymentMethods, categories ->
            _uiState.update { current ->
                current.copy(
                    accountsCount = accounts.size,
                    paymentMethodsCount = paymentMethods.size,
                    categoriesCount = categories.size
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.ToggleBiometric -> handleToggleBiometric(action.enabled)
            is SettingsUiAction.SetThemeMode -> handleSetThemeMode(action.isDarkMode)
            is SettingsUiAction.ExportBackupToUri -> handleExportBackupToUri(action.uri)
            is SettingsUiAction.RestoreBackupFromUri -> handleRestoreBackupFromUri(action.uri)
            is SettingsUiAction.ShareBackup -> handleShareBackup()
            is SettingsUiAction.Refresh -> observeData()
        }
    }

    private fun handleExportBackupToUri(uri: android.net.Uri) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val exportResult = exportBackupUseCase()
            exportResult.onSuccess { jsonString ->
                val writeResult = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonString.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    } ?: throw IllegalStateException("Não foi possível acessar o destino do arquivo.")
                }
                writeResult.onSuccess {
                    val now = System.currentTimeMillis()
                    preferencesManager.updateLastOfflineBackupTimestamp(now)
                    _effectChannel.send(SettingsUiEffect.ShowSnackbar("Backup exportado com sucesso!"))
                }.onFailure { err ->
                    _effectChannel.send(SettingsUiEffect.ShowSnackbar("Erro ao salvar arquivo: ${err.localizedMessage}"))
                }
            }.onFailure { err ->
                _effectChannel.send(SettingsUiEffect.ShowSnackbar("Erro ao gerar dados do backup: ${err.localizedMessage}"))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun handleRestoreBackupFromUri(uri: android.net.Uri) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val readResult = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                } ?: throw IllegalStateException("Não foi possível ler o arquivo selecionado.")
            }
            readResult.onSuccess { jsonString ->
                val restoreResult = restoreBackupUseCase(jsonString)
                restoreResult.onSuccess {
                    val now = System.currentTimeMillis()
                    preferencesManager.updateLastOfflineBackupTimestamp(now)
                    _effectChannel.send(SettingsUiEffect.ShowSnackbar("Backup restaurado com sucesso! Dados atualizados."))
                }.onFailure { err ->
                    _effectChannel.send(SettingsUiEffect.ShowSnackbar("Falha ao restaurar: ${err.localizedMessage}"))
                }
            }.onFailure { err ->
                _effectChannel.send(SettingsUiEffect.ShowSnackbar("Erro ao abrir arquivo: ${err.localizedMessage}"))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun handleShareBackup() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val exportResult = exportBackupUseCase()
            exportResult.onSuccess { jsonString ->
                val shareResult = runCatching {
                    val cacheDir = File(context.cacheDir, "backups").apply { mkdirs() }
                    val backupFile = File(cacheDir, "platform_backup_${System.currentTimeMillis()}.json")
                    backupFile.writeText(jsonString, Charsets.UTF_8)
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        backupFile
                    )
                }
                shareResult.onSuccess { shareUri ->
                    val now = System.currentTimeMillis()
                    preferencesManager.updateLastOfflineBackupTimestamp(now)
                    _effectChannel.send(SettingsUiEffect.ShareBackupFile(shareUri))
                }.onFailure { err ->
                    _effectChannel.send(SettingsUiEffect.ShowSnackbar("Erro ao preparar compartilhamento: ${err.localizedMessage}"))
                }
            }.onFailure { err ->
                _effectChannel.send(SettingsUiEffect.ShowSnackbar("Erro ao gerar dados do backup: ${err.localizedMessage}"))
            }
            _uiState.update { it.copy(isLoading = false) }
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
