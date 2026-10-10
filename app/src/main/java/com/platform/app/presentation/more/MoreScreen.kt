package com.platform.app.presentation.more

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.BuildConfig
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.PlatformShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToContacts: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToExpenseItems: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            PlatformAppBar(
                title = AppStrings.Navigation.MORE,
                subtitle = "Central de Cadastros e Preferências"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Cadastros Financeiros
            MoreSection(title = "Cadastros Financeiros") {
                MoreMenuRow(
                    title = "Contas Bancárias",
                    subtitle = "Bancos, carteiras e contas correntes",
                    icon = Icons.Default.AccountBalance,
                    onClick = onNavigateToAccounts
                )
                MoreMenuRow(
                    title = "Formas de Pagamento",
                    subtitle = "Pix, cartões de crédito/débito e dinheiro",
                    icon = Icons.Default.Payments,
                    onClick = onNavigateToPaymentMethods
                )
                MoreMenuRow(
                    title = "Categorias",
                    subtitle = "Grupos organizacionais de orçamento",
                    icon = Icons.Default.Category,
                    onClick = onNavigateToCategories
                )
                MoreMenuRow(
                    title = "Itens",
                    subtitle = "Itens padronizados e naturezas de gasto",
                    icon = Icons.Default.Checklist,
                    onClick = onNavigateToExpenseItems
                )
            }

            // Seção: Contatos
            MoreSection(title = "Pessoas e Empresas") {
                MoreMenuRow(
                    title = "Contatos",
                    subtitle = "Fornecedores, prestadores e favorecidos",
                    icon = Icons.Default.People,
                    onClick = onNavigateToContacts
                )
            }

            // Seção: Planejamento
            MoreSection(title = "Planejamento Orçamentário") {
                MoreMenuRow(
                    title = "Orçamentos",
                    subtitle = "Teto mensal por categoria",
                    icon = Icons.Default.PieChart,
                    onClick = onNavigateToBudgets
                )
                MoreMenuRow(
                    title = "Metas",
                    subtitle = "Objetivos e reservas financeiras",
                    icon = Icons.Default.Flag,
                    onClick = onNavigateToGoals
                )
            }

            // Seção: Sistema
            MoreSection(title = "Sistema & Dados") {
                MoreMenuRow(
                    title = "Configurações & Backup",
                    subtitle = "Aparência, biometria e exportação protegida",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rodapé com versão
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Platform • Versão ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun MoreSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        PlatformCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun MoreMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = PlatformShapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
