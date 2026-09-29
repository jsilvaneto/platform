package com.platform.app.presentation.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.AccountSpend
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.CompletingInstallmentSummary
import com.platform.app.domain.model.ContactSpend
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.domain.model.NatureSpend
import com.platform.app.domain.model.PastMonthHistory
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformAvatar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformEmptyState
import com.platform.app.presentation.components.PlatformPrivacyToggle
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.dashboard.DashboardUiAction
import com.platform.app.presentation.dashboard.DashboardUiState
import com.platform.app.presentation.dashboard.StatisticsTab
import com.platform.app.presentation.theme.BrandPrimary
import com.platform.app.presentation.theme.ErrorRed
import com.platform.app.presentation.theme.ErrorRedContainer
import com.platform.app.presentation.theme.InfoCyan
import com.platform.app.presentation.theme.InfoCyanContainer
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.SuccessGreenContainer
import com.platform.app.presentation.theme.WarningAmber
import com.platform.app.presentation.theme.WarningAmberContainer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Estatísticas",
                subtitle = "Inteligência & Tomada de Decisão",
                onOpenDrawer = onOpenDrawer,
                actions = {
                    PlatformPrivacyToggle(
                        isPrivate = uiState.isPrivacyMode,
                        onToggle = { onAction(DashboardUiAction.TogglePrivacyMode) }
                    )
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
            // Seletor de Período / Navegador Temporal
            MonthNavigationHeader(
                selectedMonthMillis = uiState.selectedMonthMillis,
                onPreviousMonth = { onAction(DashboardUiAction.PreviousMonth) },
                onNextMonth = { onAction(DashboardUiAction.NextMonth) },
                onCurrentMonth = { onAction(DashboardUiAction.CurrentMonth) }
            )

            // Abas Segmentadas: Passado, Presente e Futuro
            val tabs = listOf(
                SegmentedTabItem(title = StatisticsTab.PAST.title),
                SegmentedTabItem(title = StatisticsTab.PRESENT.title),
                SegmentedTabItem(title = StatisticsTab.FUTURE.title)
            )

            PlatformSegmentedTabs(
                items = tabs,
                selectedIndex = uiState.selectedTab.ordinal,
                onTabSelected = { index ->
                    onAction(DashboardUiAction.SelectTab(StatisticsTab.entries[index]))
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (uiState.isLoading && uiState.metrics == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                val metrics = uiState.metrics
                if (metrics == null) {
                    EmptyStatisticsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (uiState.selectedTab) {
                            StatisticsTab.PAST -> {
                                item {
                                    PastHeroCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item {
                                    PreviousMonthComparisonCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item {
                                    PastHistoryChartCard(
                                        pastHistory = metrics.pastMonthsHistory,
                                        isPrivate = uiState.isPrivacyMode,
                                        onSelectMonth = { onAction(DashboardUiAction.SelectMonth(it)) }
                                    )
                                }
                                item {
                                    PastExtremesCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                if (metrics.topContactsSpend.isNotEmpty()) {
                                    item {
                                        TopContactsCard(
                                            contacts = metrics.topContactsSpend,
                                            isPrivate = uiState.isPrivacyMode
                                        )
                                    }
                                }
                            }

                            StatisticsTab.PRESENT -> {
                                item {
                                    PresentHeroCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item {
                                    NatureDistributionCard(
                                        natureDistribution = metrics.natureDistribution,
                                        totalDueCents = metrics.totalDueMonthCents,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item {
                                    PaymentMethodDistributionCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                if (metrics.categoryDistribution.isNotEmpty()) {
                                    item {
                                        CategoryDistributionCard(
                                            categories = metrics.categoryDistribution,
                                            isPrivate = uiState.isPrivacyMode
                                        )
                                    }
                                }
                                if (metrics.accountsDistribution.isNotEmpty()) {
                                    item {
                                        AccountsDistributionCard(
                                            accounts = metrics.accountsDistribution,
                                            isPrivate = uiState.isPrivacyMode
                                        )
                                    }
                                }
                            }

                            StatisticsTab.FUTURE -> {
                                item {
                                    FutureHeroCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item {
                                    DeescalationCurveCard(
                                        projections = metrics.futureMonthsProjections,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                if (metrics.nextCompletingInstallments.isNotEmpty()) {
                                    item {
                                        CompletingInstallmentsCard(
                                            completingList = metrics.nextCompletingInstallments,
                                            totalFreedFlowCents = metrics.projectedFreedMonthlyFlowCents,
                                            isPrivate = uiState.isPrivacyMode
                                        )
                                    }
                                }
                                item {
                                    DecisionAdvisorCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// NAVEGAÇÃO TEMPORAL
// -------------------------------------------------------------------------------------------------

@Composable
fun MonthNavigationHeader(
    selectedMonthMillis: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthStart = DateUtils.getStartOfMonth(System.currentTimeMillis())
    val isCurrent = DateUtils.getStartOfMonth(selectedMonthMillis) == currentMonthStart

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Mês anterior",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = DateUtils.formatMonthYear(selectedMonthMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isCurrent) {
                    TextButton(
                        onClick = onCurrentMonth,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Atual",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Próximo mês",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SEÇÃO 1: ABA PASSADO (HISTÓRICO & TENDÊNCIAS)
// -------------------------------------------------------------------------------------------------

@Composable
fun PastHeroCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.History,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Histórico & Desempenho",
                subtitle = "Visão consolidada do comportamento financeiro passado"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiBox(
                    label = "Média Mensal",
                    value = formatValueOrPrivate(metrics.historicalMonthlyAverageCents, isPrivate),
                    sublabel = "Custo base apurado",
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Total Liquidado",
                    value = formatValueOrPrivate(metrics.totalHistoricalPaidCents, isPrivate),
                    sublabel = "Histórico pago",
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Pontualidade",
                    value = "${metrics.onTimePaymentRate}%",
                    sublabel = "Pagas no prazo",
                    valueColor = if (metrics.onTimePaymentRate >= 90) SuccessGreen else WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PreviousMonthComparisonCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val diffCents = metrics.totalDueMonthCents - metrics.previousMonthDueCents
    val hasPrevData = metrics.previousMonthDueCents > 0L
    val isHigher = diffCents > 0L
    val pctDiff = if (hasPrevData) {
        (diffCents.toFloat() / metrics.previousMonthDueCents.toFloat()) * 100f
    } else 0f

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comparativo com Mês Anterior",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (hasPrevData) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isHigher) ErrorRedContainer else SuccessGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isHigher) ErrorRed else SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%+.1f%%", pctDiff),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isHigher) ErrorRed else SuccessGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Mês Anterior",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatValueOrPrivate(metrics.previousMonthDueCents, isPrivate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Variação Absoluta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (!hasPrevData) "Sem histórico anterior"
                        else if (isHigher) "+${formatValueOrPrivate(diffCents, isPrivate)}"
                        else "-${formatValueOrPrivate(-diffCents, isPrivate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!hasPrevData) MaterialTheme.colorScheme.onSurfaceVariant else if (isHigher) ErrorRed else SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
fun PastHistoryChartCard(
    pastHistory: List<PastMonthHistory>,
    isPrivate: Boolean,
    onSelectMonth: (Long) -> Unit
) {
    if (pastHistory.isEmpty()) return

    val maxDueCents = pastHistory.maxOfOrNull { it.totalDueCents }?.coerceAtLeast(1L) ?: 1L

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Evolução dos Últimos 6 Meses",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Desembolsos devidos e taxa de liquidação de cada período",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pastHistory.forEach { item ->
                    val progress = (item.totalDueCents.toFloat() / maxDueCents.toFloat()).coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectMonth(item.monthMillis) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.monthLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatValueOrPrivate(item.totalDueCents, isPrivate),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (item.paidRate >= 95) SuccessGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${item.paidRate}% pago",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.paidRate >= 95) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = progress,
                            height = 6.dp,
                            progressColor = if (item.paidRate == 100) SuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastExtremesCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    if (metrics.highestSpendMonthCents <= 0L && metrics.lowestSpendMonthCents <= 0L) return

    PlatformCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = ErrorRedContainer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Mês Mais Pesado",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = metrics.highestSpendMonthLabel.ifEmpty { "N/A" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatValueOrPrivate(metrics.highestSpendMonthCents, isPrivate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = SuccessGreenContainer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Mês Mais Econômico",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = metrics.lowestSpendMonthLabel.ifEmpty { "N/A" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatValueOrPrivate(metrics.lowestSpendMonthCents, isPrivate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
fun TopContactsCard(
    contacts: List<ContactSpend>,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Maiores Destinatários / Fornecedores",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Onde os maiores volumes financeiros foram direcionados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                contacts.forEach { contact ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            PlatformAvatar(
                                name = contact.contactName,
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = contact.contactName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${formatValueOrPrivate(contact.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", contact.percentage)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SEÇÃO 2: ABA PRESENTE (RAIO-X DO MÊS SELECIONADO)
// -------------------------------------------------------------------------------------------------

@Composable
fun PresentHeroCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val total = metrics.totalDueMonthCents
    val paidRate = if (total > 0) ((metrics.totalPaidMonthCents.toFloat() / total.toFloat()) * 100).toInt() else 0

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    icon = Icons.Default.PieChart,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Execução do Mês",
                    subtitle = metrics.currentMonthLabel
                )

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (paidRate == 100) SuccessGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$paidRate% liquidado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (paidRate == 100) SuccessGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Total Comprometido no Mês",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatValueOrPrivate(total, isPrivate),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            PlatformProgressBar(
                progress = if (total > 0) metrics.totalPaidMonthCents.toFloat() / total.toFloat() else 0f,
                height = 8.dp,
                progressColor = SuccessGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBreakdownBox(
                    label = "Pago",
                    value = formatValueOrPrivate(metrics.totalPaidMonthCents, isPrivate),
                    color = SuccessGreen,
                    containerColor = SuccessGreenContainer,
                    modifier = Modifier.weight(1f)
                )
                StatusBreakdownBox(
                    label = "Pendente",
                    value = formatValueOrPrivate(metrics.totalPendingMonthCents, isPrivate),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )
                if (metrics.totalOverdueMonthCents > 0L) {
                    StatusBreakdownBox(
                        label = "Atrasado",
                        value = formatValueOrPrivate(metrics.totalOverdueMonthCents, isPrivate),
                        color = ErrorRed,
                        containerColor = ErrorRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NatureDistributionCard(
    natureDistribution: List<NatureSpend>,
    totalDueCents: Long,
    isPrivate: Boolean
) {
    val mandatorySpend = natureDistribution.firstOrNull { it.nature == ExpenseNature.OBRIGATORIO }
    val mandatoryPct = mandatorySpend?.percentage ?: 0f
    val wantsSpend = natureDistribution.firstOrNull { it.nature == ExpenseNature.DESEJA }
    val wantsPct = wantsSpend?.percentage ?: 0f

    val isRigidBudget = mandatoryPct > 55f
    val isHighLifestyle = wantsPct > 30f

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rigidez Orçamentária (Regra 50-30-20)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Classificação por essencialidade e grau de flexibilidade dos custos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnóstico de Rigidez
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isRigidBudget -> WarningAmberContainer.copy(alpha = 0.4f)
                    isHighLifestyle -> InfoCyanContainer.copy(alpha = 0.4f)
                    else -> SuccessGreenContainer.copy(alpha = 0.4f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isRigidBudget) Icons.Default.Warning else Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (isRigidBudget) WarningAmber else SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isRigidBudget -> "Atenção: Orçamento engessado (${String.format(Locale.getDefault(), "%.0f", mandatoryPct)}% obrigatório). Margem de ajuste estreita."
                            isHighLifestyle -> "Alerta: Gastos com estilo de vida/desejos estão elevados (${String.format(Locale.getDefault(), "%.0f", wantsPct)}%)."
                            else -> "Excelente: Distribuição equilibrada com margem de segurança orçamentária."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                natureDistribution.forEach { item ->
                    val color = try { Color(item.nature.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.nature.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${formatValueOrPrivate(item.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", item.percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = (item.percentage / 100f).coerceIn(0f, 1f),
                            height = 6.dp,
                            progressColor = color,
                            trackColor = color.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodDistributionCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val total = metrics.totalDueMonthCents
    if (total <= 0L) return

    val cardPct = metrics.creditCardPercentage
    val nonCardPct = (100f - cardPct).coerceAtLeast(0f)

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Meio de Liquidação & Crédito",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Exposição a faturas de cartão de crédito vs liquidação à vista",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra Bifurcada Cartão vs Outros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (cardPct > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(cardPct.coerceAtLeast(1f))
                            .background(BrandPrimary)
                    )
                }
                if (nonCardPct > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(nonCardPct.coerceAtLeast(1f))
                            .background(InfoCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Cartão de Crédito",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatValueOrPrivate(metrics.creditCardSpendCents, isPrivate)} (${String.format(Locale.getDefault(), "%.0f", cardPct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = InfoCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Débito / Pix / Conta",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatValueOrPrivate(metrics.nonCardSpendCents, isPrivate)} (${String.format(Locale.getDefault(), "%.0f", nonCardPct)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDistributionCard(
    categories: List<CategorySpend>,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gastos por Categoria",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.forEach { cat ->
                    val color = try { Color(cat.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${formatValueOrPrivate(cat.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", cat.percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = (cat.percentage / 100f).coerceIn(0f, 1f),
                            height = 6.dp,
                            progressColor = color,
                            trackColor = color.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsDistributionCard(
    accounts: List<AccountSpend>,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Concentração por Contas Bancárias",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Instituições que concentram as liquidações do mês",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = acc.accountName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${acc.pendingBillsCount} pagamentos vinculados",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = formatValueOrPrivate(acc.totalAmountCents, isPrivate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// SEÇÃO 3: ABA FUTURO (PREVISIBILIDADE & TOMADA DE DECISÃO)
// -------------------------------------------------------------------------------------------------

@Composable
fun FutureHeroCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Timeline,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Visão Futura & Previsibilidade",
                subtitle = "Compromissos assumidos para os próximos 6 a 12 meses"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiBox(
                    label = "Total Contratado",
                    value = formatValueOrPrivate(metrics.totalCommittedFutureCents, isPrivate),
                    sublabel = "Obrigações futuras",
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Parcelas Futuras",
                    value = "${metrics.futureInstallmentsCount}",
                    sublabel = "Vencimentos",
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Custo Fixo Base",
                    value = formatValueOrPrivate(metrics.fixedMonthlyTotalCents, isPrivate),
                    sublabel = "Assinaturas/mês",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DeescalationCurveCard(
    projections: List<FutureMonthProjection>,
    isPrivate: Boolean
) {
    if (projections.isEmpty()) return

    val maxCommitted = projections.maxOfOrNull { it.totalCommittedCents }?.coerceAtLeast(1L) ?: 1L
    val highestProj = projections.maxByOrNull { it.totalCommittedCents }
    val lowestProj = projections.minByOrNull { it.totalCommittedCents }

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Curva de Desoneração (6 Meses)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Evolução decrescente dos desembolsos com amortização",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (lowestProj != null && lowestProj.totalCommittedCents < (highestProj?.totalCommittedCents ?: 0L)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SuccessGreenContainer
                    ) {
                        Text(
                            text = "Maior folga: ${lowestProj.monthLabel.take(7)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                projections.forEach { proj ->
                    val progress = (proj.totalCommittedCents.toFloat() / maxCommitted.toFloat()).coerceIn(0f, 1f)
                    val isPeak = proj == highestProj && (highestProj?.totalCommittedCents ?: 0L) > 0L
                    val isLowest = proj == lowestProj

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = proj.monthLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isPeak) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pico",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                } else if (isLowest) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Folga",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatValueOrPrivate(proj.totalCommittedCents, isPrivate),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPeak) ErrorRed else if (isLowest) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${proj.installmentsCount}p)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = progress,
                            height = 6.dp,
                            progressColor = if (isPeak) ErrorRed else if (isLowest) SuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletingInstallmentsCard(
    completingList: List<CompletingInstallmentSummary>,
    totalFreedFlowCents: Long,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Desoneração & Término de Parcelas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Compras parceladas que chegam ao fim nos próximos meses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SuccessGreenContainer
                ) {
                    Text(
                        text = "+${formatValueOrPrivate(totalFreedFlowCents, isPrivate)}/mês livre",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                completingList.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Quita em ${item.completionMonthLabel} (${item.finalInstallmentNumber}/${item.totalInstallments})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Libera",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "+${formatValueOrPrivate(item.freedMonthlyAmountCents, isPrivate)}/mês",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DecisionAdvisorCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val avgSpend = metrics.historicalMonthlyAverageCents.coerceAtLeast(1L)
    val nextMonthCommitment = metrics.futureMonthsProjections.firstOrNull()?.totalCommittedCents ?: 0L
    val commitmentRatio = (nextMonthCommitment.toFloat() / avgSpend.toFloat())

    val canAffordNewInstallment = commitmentRatio < 0.75f

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Cockpit de Tomada de Decisão",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recomendações estratégicas antes de assumir novos gastos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (canAffordNewInstallment) SuccessGreenContainer.copy(alpha = 0.4f) else WarningAmberContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (canAffordNewInstallment) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (canAffordNewInstallment) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (canAffordNewInstallment) "Janela Favorável para Investimentos / Despesas"
                            else "Atenção: Margem Futura Comprometida",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (canAffordNewInstallment)
                            "Seu comprometimento projetado para o próximo mês está abaixo de 75% da média histórica. Você tem folga orçamentária para novas despesas ou aportes."
                        else
                            "Os próximos meses já possuem comprometimento próximo ou superior à sua média de gastos. Sugerido aguardar o término de parcelamentos em andamento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENTES AUXILIARES DE UI
// -------------------------------------------------------------------------------------------------

@Composable
fun SectionHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconTint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricKpiBox(
    label: String,
    value: String,
    sublabel: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusBreakdownBox(
    label: String,
    value: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStatisticsState() {
    PlatformEmptyState(
        icon = Icons.Default.Timeline,
        title = "Sem dados estatísticos no período",
        message = "Insira lançamentos para gerar o histórico e as projeções do app."
    )
}
