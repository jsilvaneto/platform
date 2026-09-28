package com.platform.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.platform.app.core.security.BiometricAuthManager
import com.platform.app.presentation.components.PlatformAppBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    biometricAuthManager: BiometricAuthManager,
    onOpenDrawer: () -> Unit,
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showReleaseNotesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is SettingsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Menu individual: Contas
            SettingActionCard(
                title = "Contas",
                icon = Icons.Default.AccountBalance,
                onClick = onNavigateToAccounts
            )

            // Menu individual: Formas de Pagamento
            SettingActionCard(
                title = "Formas de Pagamento",
                icon = Icons.Default.CreditCard,
                onClick = onNavigateToPaymentMethods
            )

            // Menu individual: Categorias
            SettingActionCard(
                title = "Categorias",
                icon = Icons.Default.Category,
                onClick = onNavigateToCategories
            )

            // Seção de Segurança
            SectionCard(
                title = "Segurança & Privacidade",
                icon = Icons.Default.Security
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloqueio do Aplicativo",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.isBiometricSupported)
                                "Exigir biometria ou senha do celular ao abrir"
                            else
                                "Biometria/senha não disponível no aparelho",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.onAction(SettingsUiAction.ToggleBiometric(it)) },
                        enabled = uiState.isBiometricSupported,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Seção de Tema / Aparência
            SectionCard(
                title = "Aparência",
                icon = Icons.Default.DarkMode
            ) {
                Text(
                    text = "Tema do Aplicativo",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = uiState.isDarkMode == null,
                        onClick = { viewModel.onAction(SettingsUiAction.SetThemeMode(null)) },
                        label = { Text("Automático", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.isDarkMode == false,
                        onClick = { viewModel.onAction(SettingsUiAction.SetThemeMode(false)) },
                        label = { Text("Claro", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.isDarkMode == true,
                        onClick = { viewModel.onAction(SettingsUiAction.SetThemeMode(true)) },
                        label = { Text("Escuro", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Seção de Armazenamento Local
            SectionCard(
                title = "Armazenamento",
                icon = Icons.Default.Storage
            ) {
                Text(
                    text = "Banco de Dados Local Room SQLite",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Todos os dados financeiros, parcelas e contatos são processados e armazenados com privacidade em seu dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Seção "Sobre o Aplicativo"
            SectionCard(
                title = "Sobre o Aplicativo",
                icon = Icons.Default.Info
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReleaseNotesDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Platform",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "v${uiState.appVersionName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Gestão Financeira & Planejamento",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    TextButton(
                        onClick = { showReleaseNotesDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Novidades",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showReleaseNotesDialog) {
            ReleaseNotesDialog(
                versionName = uiState.appVersionName,
                onDismiss = { showReleaseNotesDialog = false }
            )
        }
    }
}

@Composable
fun ReleaseNotesDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🚀", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Melhorias & Correções",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Versão $versionName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "O que há de novo nesta versão:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ReleaseNoteItem(
                    emoji = "📊",
                    title = "Dashboard Aprimorado (Início)",
                    description = "Novos cards inteligentes para visualização clara do passado, compromissos do mês e projeção futura de despesas."
                )

                ReleaseNoteItem(
                    emoji = "💳",
                    title = "Recorrentes e Parcelados",
                    description = "Novo menu lateral dedicado ao gerenciamento de assinaturas fixas e compras parceladas em andamento."
                )

                ReleaseNoteItem(
                    emoji = "🎯",
                    title = "Metas e Orçamentos",
                    description = "Crie objetivos financeiros com progresso visual e estabeleça tetos mensais de gastos por categoria."
                )

                ReleaseNoteItem(
                    emoji = "📍",
                    title = "Cadastro de Contatos Inteligente",
                    description = "Busca automática de endereço a partir do CEP, novos campos de número e complemento, e edição rápida."
                )

                ReleaseNoteItem(
                    emoji = "🏛️",
                    title = "Gestão Financeira Direta",
                    description = "Telas dedicadas e exclusivas para Contas, Formas de Pagamento e Categorias, sem menus misturados."
                )

                ReleaseNoteItem(
                    emoji = "✨",
                    title = "Menu Lateral Minimalista",
                    description = "Interface enxuta, proporções elegantes, remoção de subtítulos e navegação ágil."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Entendido")
            }
        }
    )
}

@Composable
fun ReleaseNoteItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

