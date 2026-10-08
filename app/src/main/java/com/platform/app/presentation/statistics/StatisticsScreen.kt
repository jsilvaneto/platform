package com.platform.app.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
