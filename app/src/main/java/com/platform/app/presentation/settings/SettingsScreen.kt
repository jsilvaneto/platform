package com.platform.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
                subtitle = "Estrutura financeira, segurança e tema",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Seção de Estrutura Financeira (Contas, Formas de Pagamento e Categorias)
            SectionCard(
                title = "Estrutura Financeira",
                icon = Icons.Default.AccountBalance
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val accountsBadge = if (uiState.accountsCount > 0) {
                        "${uiState.accountsCount} ${if (uiState.accountsCount == 1) "conta" else "contas"}"
                    } else null

                    SettingsNavigationTile(
                        title = "Contas Bancárias & Carteiras",
                        subtitle = "Bancos, saldos iniciais e carteiras de dinheiro",
                        icon = Icons.Default.AccountBalance,
                        badgeText = accountsBadge,
                        onClick = onNavigateToAccounts
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    val methodsBadge = if (uiState.paymentMethodsCount > 0) {
                        "${uiState.paymentMethodsCount} ${if (uiState.paymentMethodsCount == 1) "forma" else "formas"}"
                    } else null

                    SettingsNavigationTile(
                        title = "Formas de Pagamento",
                        subtitle = "Cartões de crédito, débito, Pix e boletos",
                        icon = Icons.Default.CreditCard,
                        badgeText = methodsBadge,
                        onClick = onNavigateToPaymentMethods
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    val categoriesBadge = if (uiState.categoriesCount > 0) {
                        "${uiState.categoriesCount} ${if (uiState.categoriesCount == 1) "categoria" else "categorias"}"
                    } else null

                    SettingsNavigationTile(
                        title = "Categorias & Subcategorias",
                        subtitle = "Classificação de receitas, despesas e tags",
                        icon = Icons.Default.Category,
                        badgeText = categoriesBadge,
                        onClick = onNavigateToCategories
                    )
                }
            }

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
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloqueio do Aplicativo",
                            style = MaterialTheme.typography.titleMedium,
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.isDarkMode == null,
                        onClick = { viewModel.onAction(SettingsUiAction.SetThemeMode(null)) },
                        label = { Text("Automático") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.isDarkMode == false,
                        onClick = { viewModel.onAction(SettingsUiAction.SetThemeMode(false)) },
                        label = { Text("Claro") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.isDarkMode == true,
                        onClick = { viewModel.onAction(SettingsUiAction.SetThemeMode(true)) },
                        label = { Text("Escuro") },
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showReleaseNotesDialog = true },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Platform",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Gestão Financeira & Planejamento",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Versão ${uiState.appVersionName} (Build ${uiState.appVersionCode})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showReleaseNotesDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver Novidades e Melhorias da Versão")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingsNavigationTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
