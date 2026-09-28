package com.platform.app.presentation.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.AccountSpend
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.ErrorRed
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBalanceVisible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Início",
                subtitle = "Painel de Inteligência Financeira",
                onOpenDrawer = onOpenDrawer,
                actions = {
                    IconButton(onClick = { isBalanceVisible = !isBalanceVisible }) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Ocultar valores",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading && uiState.metrics == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val metrics = uiState.metrics
                if (metrics == null || (metrics.totalDueMonthCents == 0L && metrics.upcomingInstallments.isEmpty() && metrics.futureMonthsProjections.all { it.totalCommittedCents == 0L } && metrics.fixedMonthlyTotalCents == 0L)) {
                    EmptyDashboardState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Hero Card: Panorama Geral do Mês Vigente
                        item {
                            CurrentMonthHeroCard(
                                metrics = metrics,
                                isBalanceVisible = isBalanceVisible
                            )
                        }

                        // 2. Grid de KPIs Globais (Estilo Wallet)
                        item {
                            GlobalMacroKpisGrid(
                                metrics = metrics,
                                isBalanceVisible = isBalanceVisible
                            )
                        }

                        // 3. Vencimentos dos Próximos 7 Dias (Ação Rápida de Pagamento)
                        if (metrics.upcomingWeekInstallments.isNotEmpty()) {
                            item {
                                UpcomingWeekSection(
                                    installments = metrics.upcomingWeekInstallments,
                                    onTogglePayment = { inst ->
                                        onAction(
                                            DashboardUiAction.TogglePayment(
                                                installmentId = inst.id,
                                                currentPaid = inst.isPaid
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        // 4. Alternador de Visão de Inteligência
                        item {
                            DashboardViewModeToggle(
                                currentMode = uiState.viewMode,
                                onSelectMode = { onAction(DashboardUiAction.ChangeViewMode(it)) }
                            )
                        }

                        // 5. Conteúdo Dinâmico por Aba
                        if (uiState.viewMode == DashboardViewMode.OVERVIEW) {
                            if (metrics.categoryDistribution.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Distribuição por Categorias",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                items(metrics.categoryDistribution, key = { it.categoryName }) { catSpend ->
                                    CategorySpendRow(
                                        catSpend = catSpend,
                                        isBalanceVisible = isBalanceVisible
                                    )
                                }
                            }

                            if (metrics.accountsDistribution.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Comprometimento por Conta de Referência",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                items(metrics.accountsDistribution, key = { it.accountName }) { accSpend ->
                                    AccountSpendRow(
                                        accSpend = accSpend,
                                        isBalanceVisible = isBalanceVisible
                                    )
                                }
                            }
                        } else {
                            item {
                                PredictiveReliefCard(
                                    futureProjections = metrics.futureMonthsProjections,
                                    totalCommittedFutureCents = metrics.totalCommittedFutureCents,
                                    isBalanceVisible = isBalanceVisible
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentMonthHeroCard(
    metrics: FinancialDashboardMetrics,
    isBalanceVisible: Boolean
) {
    val monthTitle = if (metrics.currentMonthLabel.isNotBlank()) {
        "Mês Vigente • ${metrics.currentMonthLabel}"
    } else {
        "Mês Vigente"
    }

    val paidProgress = if (metrics.totalDueMonthCents > 0L) {
        (metrics.totalPaidMonthCents.toFloat() / metrics.totalDueMonthCents.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val percentPaid = (paidProgress * 100).toInt()

    PlatformCard(
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Contas a pagar do período",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PlatformStatusChip(
                    text = "$percentPaid% Quitado",
                    type = if (percentPaid == 100) StatusChipType.SUCCESS else StatusChipType.INFO
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total Previsto no Mês",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isBalanceVisible)
                    CurrencyUtils.formatCentsToCurrency(metrics.totalDueMonthCents)
                else
                    "R$ ••••••",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { paidProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = SuccessGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SuccessGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Já Pago",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isBalanceVisible)
                                CurrencyUtils.formatCentsToCurrency(metrics.totalPaidMonthCents)
                            else "R$ •••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(WarningAmber, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Restante Pendente",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isBalanceVisible)
                                CurrencyUtils.formatCentsToCurrency(metrics.totalPendingMonthCents)
                            else "R$ •••••",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalMacroKpisGrid(
    metrics: FinancialDashboardMetrics,
    isBalanceVisible: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Custo Fixo Recorrente
            PlatformCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custo Fixo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBalanceVisible)
                            CurrencyUtils.formatCentsToCurrency(metrics.fixedMonthlyTotalCents)
                        else
                            "R$ •••••",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metrics.activeRecurringCount} contratos/mês",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Card 2: Saldo Devedor Parcelado
            PlatformCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo Parcelado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBalanceVisible)
                            CurrencyUtils.formatCentsToCurrency(metrics.totalInstallmentsRemainingCents)
                        else
                            "R$ •••••",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metrics.activeInstallmentsCount} compras ativas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Urgência Próximos 7 Dias
            PlatformCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Próximos 7 Dias",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (metrics.upcomingWeekInstallments.isNotEmpty()) ErrorRed else SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metrics.upcomingWeekInstallments.size} a vencer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (metrics.upcomingWeekInstallments.isNotEmpty()) ErrorRed else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val upcomingWeekSum = metrics.upcomingWeekInstallments.sumOf { it.amountCents }
                    Text(
                        text = if (isBalanceVisible) CurrencyUtils.formatCentsToCurrency(upcomingWeekSum) else "R$ •••••",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Card 4: Pontualidade
            PlatformCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pontualidade",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${metrics.onTimePaymentRate}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Histórico no prazo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardViewModeToggle(
    currentMode: DashboardViewMode,
    onSelectMode: (DashboardViewMode) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectMode(DashboardViewMode.OVERVIEW) },
                shape = RoundedCornerShape(9.dp),
                color = if (currentMode == DashboardViewMode.OVERVIEW) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (currentMode == DashboardViewMode.OVERVIEW) 1.dp else 0.dp
            ) {
                Text(
                    text = "Diagnóstico Geral",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (currentMode == DashboardViewMode.OVERVIEW) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentMode == DashboardViewMode.OVERVIEW) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectMode(DashboardViewMode.PREDICTIONS) },
                shape = RoundedCornerShape(9.dp),
                color = if (currentMode == DashboardViewMode.PREDICTIONS) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (currentMode == DashboardViewMode.PREDICTIONS) 1.dp else 0.dp
            ) {
                Text(
                    text = "Projeção Futura (12m)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (currentMode == DashboardViewMode.PREDICTIONS) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentMode == DashboardViewMode.PREDICTIONS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun PredictiveReliefCard(
    futureProjections: List<FutureMonthProjection>,
    totalCommittedFutureCents: Long,
    isBalanceVisible: Boolean
) {
    PlatformCard(
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Curva de Desoneração",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Projeção de alívio nos gastos mensais",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = SuccessGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val maxAmount = futureProjections.maxOfOrNull { it.totalCommittedCents } ?: 1L
            val safeMax = if (maxAmount > 0L) maxAmount else 1L

            futureProjections.forEach { proj ->
                val ratio = (proj.totalCommittedCents.toFloat() / safeMax.toFloat()).coerceIn(0f, 1f)
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = proj.monthLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBalanceVisible)
                                CurrencyUtils.formatCentsToCurrency(proj.totalCommittedCents)
                            else "R$ •••••",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AccountSpendRow(
    accSpend: AccountSpend,
    isBalanceVisible: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accSpend.accountName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${accSpend.bankName} • ${accSpend.pendingBillsCount} contas vinculadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (isBalanceVisible)
                    CurrencyUtils.formatCentsToCurrency(accSpend.totalAmountCents)
                else "R$ •••••",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun UpcomingWeekSection(
    installments: List<BillInstallment>,
    onTogglePayment: (BillInstallment) -> Unit
) {
    PlatformCard(
        shape = RoundedCornerShape(16.dp),
        containerColor = WarningAmber.copy(alpha = 0.08f),
        borderColor = WarningAmber.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Vencimentos desta Semana",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = WarningAmber
                    )
                    Text(
                        text = "${installments.size} contas nos próximos 7 dias",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = WarningAmber.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            installments.forEach { inst ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onTogglePayment(inst) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (inst.isPaid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Pagar",
                                tint = if (inst.isPaid) SuccessGreen else MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inst.billTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vence em ${DateUtils.formatDate(inst.dueDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(inst.amountCents),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UpcomingInstallmentCard(
    installment: BillInstallment,
    isBalanceVisible: Boolean
) {
    PlatformCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = try {
                Color(android.graphics.Color.parseColor(installment.categoryColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = installment.billTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${installment.categoryName} • Vence em ${DateUtils.formatDate(installment.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = if (isBalanceVisible)
                    CurrencyUtils.formatCentsToCurrency(installment.amountCents)
                else
                    "R$ •••",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategorySpendRow(
    catSpend: CategorySpend,
    isBalanceVisible: Boolean
) {
    val barColor = try {
        Color(android.graphics.Color.parseColor(catSpend.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    PlatformCard(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = catSpend.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBalanceVisible)
                        "${CurrencyUtils.formatCentsToCurrency(catSpend.amountCents)} (${String.format("%.1f", catSpend.percentage)}%)"
                    else
                        "R$ ••• (${String.format("%.1f", catSpend.percentage)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (catSpend.percentage / 100f).coerceIn(0f, 1f) },
                color = barColor,
                trackColor = barColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}

@Composable
fun EmptyDashboardState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📊", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhum dado registrado para este mês",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Adicione contas ou parcelamentos em 'Registros' para visualizar seu panorama financeiro.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
