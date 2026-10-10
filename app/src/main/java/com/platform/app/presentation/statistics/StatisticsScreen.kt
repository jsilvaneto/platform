package com.platform.app.presentation.statistics

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformPrivacyToggle
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.components.formatValueOrPrivate
import androidx.compose.material.icons.filled.Lightbulb
import com.platform.app.presentation.dashboard.DashboardUiAction
import com.platform.app.presentation.dashboard.DashboardUiState
import com.platform.app.presentation.dashboard.StatisticsTab
import com.platform.app.presentation.statistics.components.AccountsDistributionCard
import com.platform.app.presentation.statistics.components.CategoryDistributionCard
import com.platform.app.presentation.statistics.components.CompletingInstallmentsCard
import com.platform.app.presentation.statistics.components.DecisionAdvisorCard
import com.platform.app.presentation.statistics.components.DeescalationCurveCard
import com.platform.app.presentation.statistics.components.EmptyStatisticsState
import com.platform.app.presentation.statistics.components.FutureHeroCard
import com.platform.app.presentation.statistics.components.MonthNavigationHeader
import com.platform.app.presentation.statistics.components.NatureDistributionCard
import com.platform.app.presentation.statistics.components.PastExtremesCard
import com.platform.app.presentation.statistics.components.PastHeroCard
import com.platform.app.presentation.statistics.components.PastHistoryChartCard
import com.platform.app.presentation.statistics.components.PaymentMethodDistributionCard
import com.platform.app.presentation.statistics.components.PresentHeroCard
import com.platform.app.presentation.statistics.components.PreviousMonthComparisonCard
import com.platform.app.presentation.statistics.components.TopContactsCard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.PlatformShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null
) {
    var showMorePast by rememberSaveable { mutableStateOf(false) }
    var showMorePresent by rememberSaveable { mutableStateOf(false) }
    var showMoreFuture by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = AppStrings.Navigation.ANALYTICS,
                subtitle = "Inteligência Financeira",
                onOpenDrawer = null,
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

            // Seção de Planejamento Integrado (Orçamentos e Metas)
            PlanningQuickAccessCard(
                onNavigateToBudgets = onNavigateToBudgets,
                onNavigateToGoals = onNavigateToGoals,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
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
                                    val pastInsight = if (metrics.savingsCents > 0) {
                                        "Economia de ${formatValueOrPrivate(metrics.savingsCents, uiState.isPrivacyMode)} em relação ao mês anterior."
                                    } else {
                                        "Histórico e taxa de pontualidade consolidada dos meses passados."
                                    }
                                    StatisticsInsightBanner(pastInsight)
                                }
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
                                        ShowMoreButton(
                                            expanded = showMorePast,
                                            onToggle = { showMorePast = !showMorePast }
                                        )
                                    }
                                    if (showMorePast) {
                                        item {
                                            TopContactsCard(
                                                contacts = metrics.topContactsSpend,
                                                isPrivate = uiState.isPrivacyMode
                                            )
                                        }
                                    }
                                }
                            }

                            StatisticsTab.PRESENT -> {
                                item {
                                    val presentInsight = if (metrics.savingsPercentage >= 20f) {
                                        "Excelente: você poupou ${String.format(java.util.Locale.US, "%.0f", metrics.savingsPercentage)}% das suas receitas este mês."
                                    } else {
                                        "Equilíbrio orçamentário: acompanhe como você gasta e divida despesas essenciais."
                                    }
                                    StatisticsInsightBanner(presentInsight)
                                }
                                item {
                                    PresentHeroCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item {
                                    NatureDistributionCard(
                                        natureDistribution = metrics.natureDistribution,
                                        savingsCents = metrics.savingsCents,
                                        savingsPercentage = metrics.savingsPercentage,
                                        budgetRigidity = metrics.budgetRigidity,
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
                                        ShowMoreButton(
                                            expanded = showMorePresent,
                                            onToggle = { showMorePresent = !showMorePresent }
                                        )
                                    }
                                    if (showMorePresent) {
                                        item {
                                            AccountsDistributionCard(
                                                accounts = metrics.accountsDistribution,
                                                isPrivate = uiState.isPrivacyMode
                                            )
                                        }
                                    }
                                }
                            }

                            StatisticsTab.FUTURE -> {
                                item {
                                    val futureInsight = if (metrics.projectedFreedMonthlyFlowCents > 0) {
                                        "Fluxo liberado de ${formatValueOrPrivate(metrics.projectedFreedMonthlyFlowCents, uiState.isPrivacyMode)} nos próximos meses com a quitação de parcelas."
                                    } else {
                                        "Projeção dos seus compromissos futuros para manter suas finanças em dia."
                                    }
                                    StatisticsInsightBanner(futureInsight)
                                }
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
                                item {
                                    DecisionAdvisorCard(
                                        metrics = metrics,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                if (metrics.nextCompletingInstallments.isNotEmpty()) {
                                    item {
                                        ShowMoreButton(
                                            expanded = showMoreFuture,
                                            onToggle = { showMoreFuture = !showMoreFuture }
                                        )
                                    }
                                    if (showMoreFuture) {
                                        item {
                                            CompletingInstallmentsCard(
                                                completingList = metrics.nextCompletingInstallments,
                                                totalFreedFlowCents = metrics.projectedFreedMonthlyFlowCents,
                                                isPrivate = uiState.isPrivacyMode
                                            )
                                        }
                                    }
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

@Composable
private fun StatisticsInsightBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = PlatformShapes.medium,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlanningQuickAccessCard(
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            onClick = onNavigateToBudgets,
            shape = PlatformShapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Orçamentos",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            onClick = onNavigateToGoals,
            shape = PlatformShapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Metas",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ShowMoreButton(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = PlatformShapes.pill,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (expanded) AppStrings.Actions.SEE_LESS else AppStrings.Actions.SEE_MORE,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
