package com.platform.app.presentation.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import com.platform.app.presentation.theme.PlatformShapes
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.settings.components.AppearanceBottomSheetContent
import com.platform.app.presentation.settings.components.CreateBackupPasswordDialog
import com.platform.app.presentation.settings.components.ReleaseNotesDialog
import com.platform.app.presentation.settings.components.RestoreConfirmDialog
import com.platform.app.presentation.settings.components.RestorePasswordDialog
import com.platform.app.presentation.settings.components.SectionCard
import com.platform.app.presentation.settings.components.SettingActionCard
import com.platform.app.presentation.settings.components.SettingsAboutSection
import com.platform.app.presentation.settings.components.SettingsBackupSection
import com.platform.app.presentation.settings.components.SettingsSecuritySection
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricAuthManager: BiometricAuthManager,
    onOpenDrawer: () -> Unit,
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToExpenseItems: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showReleaseNotesDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showAppearanceSheet by remember { mutableStateOf(false) }

    var showCreateBackupDialog by remember { mutableStateOf(false) }
    var isSharingAfterBackup by remember { mutableStateOf(false) }
    var pendingBackupPassword by remember { mutableStateOf("") }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingBackupPassword.isNotBlank()) {
            viewModel.onAction(SettingsUiAction.ExportBackupToUri(uri, pendingBackupPassword))
        }
        pendingBackupPassword = ""
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestorePasswordDialog = true
        }
    }

    val lastBackupFormatted = remember(uiState.lastBackupTimestamp) {
        if (uiState.lastBackupTimestamp > 0L) {
            val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
            sdf.format(Date(uiState.lastBackupTimestamp))
        } else {
            null
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is SettingsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is SettingsUiEffect.ShareBackupFile -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Backup"))
                }
                is SettingsUiEffect.RequestBiometricAuthForToggle -> {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        biometricAuthManager.promptAuthentication(
                            activity = activity,
                            title = "Confirmação de Segurança",
                            subtitle = if (effect.targetState)
                                "Autentique para ativar a proteção por biometria/senha"
                            else
                                "Autentique para desativar a proteção",
                            onSuccess = {
                                viewModel.confirmBiometricToggle(effect.targetState)
                            },
                            onError = { err ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Falha na autenticação: $err")
                                }
                            }
                        )
                    } else {
                        viewModel.confirmBiometricToggle(effect.targetState)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PlatformAppBar(
                title = "Configurações",
                onOpenDrawer = onOpenDrawer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Grupo 1: Cadastros
            SectionCard(
                title = "Cadastros",
                icon = Icons.Default.Category
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingActionCard(
                        title = "Contas",
                        subtitle = "Bancos, carteiras e contas de referência",
                        icon = Icons.Default.AccountBalance,
                        onClick = onNavigateToAccounts
                    )

                    SettingActionCard(
                        title = "Formas de Pagamento",
                        subtitle = "Cartão, PIX, dinheiro e métodos de quitação",
                        icon = Icons.Default.CreditCard,
                        onClick = onNavigateToPaymentMethods
                    )

                    SettingActionCard(
                        title = "Categorias",
                        subtitle = "Classificação de despesas e natureza do gasto",
                        icon = Icons.Default.Category,
                        onClick = onNavigateToCategories
                    )

                    SettingActionCard(
                        title = "Itens de Despesa",
                        subtitle = "Subitens e produtos organizados por categoria",
                        icon = Icons.Default.ShoppingBag,
                        onClick = onNavigateToExpenseItems
                    )
                }
            }

            // Grupo 2: Aparência
            SettingActionCard(
                title = "Aparência",
                subtitle = "Tema do sistema, modo escuro e ícones do aplicativo",
                icon = Icons.Default.Palette,
                onClick = { showAppearanceSheet = true }
            )

            // Seção de Segurança
            SettingsSecuritySection(
                isBiometricSupported = uiState.isBiometricSupported,
                isBiometricEnabled = uiState.isBiometricEnabled,
                onToggleBiometric = { viewModel.onAction(SettingsUiAction.ToggleBiometric(it)) }
            )

            // Seção de Armazenamento & Backup
            SettingsBackupSection(
                lastBackupFormatted = lastBackupFormatted,
                isLoading = uiState.isLoading,
                onBackupClick = {
                    isSharingAfterBackup = false
                    showCreateBackupDialog = true
                },
                onRestoreClick = { showRestoreConfirmDialog = true },
                onShareClick = {
                    isSharingAfterBackup = true
                    showCreateBackupDialog = true
                }
            )

            // Seção "Sobre o Aplicativo"
            SettingsAboutSection(
                appVersionName = uiState.appVersionName,
                onShowReleaseNotes = { showReleaseNotesDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showReleaseNotesDialog) {
            ReleaseNotesDialog(
                versionName = uiState.appVersionName,
                onDismiss = { showReleaseNotesDialog = false }
            )
        }

        if (showAppearanceSheet) {
            val appearanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showAppearanceSheet = false },
                sheetState = appearanceSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                shape = PlatformShapes.bottomSheet
            ) {
                AppearanceBottomSheetContent(
                    uiState = uiState,
                    onSetThemeMode = { viewModel.onAction(SettingsUiAction.SetThemeMode(it)) },
                    onSetAmoledMode = { viewModel.onAction(SettingsUiAction.SetAmoledMode(it)) },
                    onSetAppIcon = { viewModel.onAction(SettingsUiAction.SetAppIcon(it)) },
                    onClose = { showAppearanceSheet = false }
                )
            }
        }

        if (showRestoreConfirmDialog) {
            RestoreConfirmDialog(
                onConfirm = {
                    showRestoreConfirmDialog = false
                    openDocumentLauncher.launch(arrayOf("application/json", "*/*"))
                },
                onDismiss = { showRestoreConfirmDialog = false }
            )
        }

        if (showCreateBackupDialog) {
            CreateBackupPasswordDialog(
                isSharing = isSharingAfterBackup,
                onDismiss = { showCreateBackupDialog = false },
                onConfirm = { password ->
                    showCreateBackupDialog = false
                    if (isSharingAfterBackup) {
                        viewModel.onAction(SettingsUiAction.ShareBackup(password))
                    } else {
                        pendingBackupPassword = password
                        val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        createDocumentLauncher.launch("platform_backup_$timeStampStr.json")
                    }
                }
            )
        }

        if (showRestorePasswordDialog) {
            RestorePasswordDialog(
                onDismiss = {
                    showRestorePasswordDialog = false
                    pendingRestoreUri = null
                },
                onConfirm = { password ->
                    showRestorePasswordDialog = false
                    pendingRestoreUri?.let { uri ->
                        viewModel.onAction(SettingsUiAction.RestoreBackupFromUri(uri, password))
                    }
                    pendingRestoreUri = null
                }
            )
        }
    }
}
