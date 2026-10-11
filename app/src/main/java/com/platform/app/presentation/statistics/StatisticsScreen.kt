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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.components.PlatformPrivacyToggle
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.dashboard.DashboardUiAction
import com.platform.app.presentation.dashboard.DashboardUiState
import com.platform.app.presentation.dashboard.StatisticsPeriod
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
import com.platform.app.presentation.statistics.components.PayoffPlanBottomSheet
import com.platform.app.presentation.statistics.components.PaymentMethodDistributionCard
import com.platform.app.presentation.statistics.components.PlanningSection
import com.platform.app.presentation.statistics.components.PresentHeroCard
import com.platform.app.presentation.statistics.components.PreviousMonthComparisonCard
import com.platform.app.presentation.statistics.components.TopContactsCard
import com.platform.app.presentation.theme.PlatformShapes
import java.util.Locale

/**
 * Tela de Análises reconstruída (Prompt 62):
 * - Abas Passado | Presente | Futuro (segmentadas).
 * - Seletor de período (Mês, Trimestre, Ano, Tudo) em chips neutros.
 * - 1 frase de insight em 18sp.
 * - Card 'Equilíbrio do orçamento' com barras horizontais neutras.
 * - Card 'Pagamentos nos próximos meses' com barras neutras.
 * - Máximo de 4 cards por aba e botão 'Ver mais análises'.
 * - Seção 'Planejar' com Orçamentos, Metas e Plano de quitação.
 * - Sem cores de status fora de status.
 */
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
    var showPayoffPlan by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = AppStrings.Navigation.ANALYTICS,
                subtitle = "Inteligência Financeira",
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
            // 1. Abas Segmentadas: Passado | Presente | Futuro
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

            // 2. Seletor de Período em Chips Neutros (Mês, Trimestre, Ano, Tudo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticsPeriod.entries.forEach { period ->
                    PlatformNeutralChip(
                        text = period.title,
                        selected = uiState.selectedPeriod == period,
                        onClick = { onAction(DashboardUiAction.SelectPeriod(period)) }
                    )
                }
            }

            // Navegação de mês quando em período mensal
            if (uiState.selectedPeriod == StatisticsPeriod.MONTH) {
                MonthNavigationHeader(
                    selectedMonthMillis = uiState.selectedMonthMillis,
                    onPreviousMonth = { onAction(DashboardUiAction.PreviousMonth) },
                    onNextMonth = { onAction(DashboardUiAction.NextMonth) },
                    onCurrentMonth = { onAction(DashboardUiAction.CurrentMonth) }
                )
            }

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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 3. 1 Frase de Insight em 18sp
                        item {
                            val insightText = when (uiState.selectedTab) {
                                StatisticsTab.PAST -> if (metrics.savingsCents > 0) {
                                    "Você economizou ${formatValueOrPrivate(metrics.savingsCents, uiState.isPrivacyMode)} em relação ao mês anterior."
                                } else {
                                    "Histórico e taxa de pontualidade consolidada dos meses passados."
                                }
                                StatisticsTab.PRESENT -> if (metrics.savingsPercentage >= 20f) {
                                    "Excelente: você poupou ${String.format(Locale.US, "%.0f", metrics.savingsPercentage)}% das suas receitas este mês."
                                } else {
                                    "Equilíbrio orçamentário: acompanhe como você gasta e divida despesas essenciais."
                                }
                                StatisticsTab.FUTURE -> if (metrics.projectedFreedMonthlyFlowCents > 0) {
                                    "Fluxo liberado de ${formatValueOrPrivate(metrics.projectedFreedMonthlyFlowCents, uiState.isPrivacyMode)} nos próximos meses com a quitação de parcelas."
                                } else {
                                    "Projeção dos seus compromissos futuros para manter suas finanças em dia."
                                }
                            }

                            Text(
                                text = insightText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // 4. Cards por Aba (Máximo de 4 por padrão + 'Ver mais análises')
                        when (uiState.selectedTab) {
                            StatisticsTab.PAST -> {
                                item { PastHeroCard(metrics = metrics, isPrivate = uiState.isPrivacyMode) }
                                item { PreviousMonthComparisonCard(metrics = metrics, isPrivate = uiState.isPrivacyMode) }
                                item {
                                    PastHistoryChartCard(
                                        pastHistory = metrics.pastMonthsHistory,
                                        isPrivate = uiState.isPrivacyMode,
                                        onSelectMonth = { onAction(DashboardUiAction.SelectMonth(it)) }
                                    )
                                }
                                item { PastExtremesCard(metrics = metrics, isPrivate = uiState.isPrivacyMode) }

                                // Extras da aba Passado
                                if (metrics.topContactsSpend.isNotEmpty()) {
                                    item {
                                        ViewMoreAnalyticsButton(
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
                                item { PresentHeroCard(metrics = metrics, isPrivate = uiState.isPrivacyMode) }
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
                                item { PaymentMethodDistributionCard(metrics = metrics, isPrivate = uiState.isPrivacyMode) }
                                if (metrics.categoryDistribution.isNotEmpty()) {
                                    item { CategoryDistributionCard(categories = metrics.categoryDistribution, isPrivate = uiState.isPrivacyMode) }
                                }

                                // Extras da aba Presente
                                if (metrics.accountsDistribution.isNotEmpty()) {
                                    item {
                                        ViewMoreAnalyticsButton(
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
                                item { FutureHeroCard(metrics = metrics, isPrivate = uiState.isPrivacyMode) }
                                item {
                                    DeescalationCurveCard(
                                        projections = metrics.futureMonthsProjections,
                                        isPrivate = uiState.isPrivacyMode
                                    )
                                }
                                item { DecisionAdvisorCard(metrics = metrics) }
                                if (metrics.nextCompletingInstallments.isNotEmpty()) {
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

                        // 5. Seção "Planejar" (Orçamentos, Metas e Plano de quitação)
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            PlanningSection(
                                onNavigateToBudgets = onNavigateToBudgets,
                                onNavigateToGoals = onNavigateToGoals,
                                onOpenPayoffPlan = { showPayoffPlan = true }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // Modal do Plano de Quitação
        if (showPayoffPlan) {
            PayoffPlanBottomSheet(
                debts = emptyList(),
                onDismiss = { showPayoffPlan = false }
            )
        }
    }
}

@Composable
private fun ViewMoreAnalyticsButton(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onToggle,
        shape = PlatformShapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = if (expanded) "Ver menos" else "Ver mais análises",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}
