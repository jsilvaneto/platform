package com.platform.app.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.domain.model.MonthlyBarData
import com.platform.app.domain.model.PortfolioOverview
import com.platform.app.presentation.overview.components.Overview12MonthChart
import com.platform.app.presentation.overview.components.OverviewAttentionBanner
import com.platform.app.presentation.overview.components.OverviewFreeingUpList
import com.platform.app.presentation.overview.components.OverviewHeroCard
import com.platform.app.presentation.overview.components.OverviewTilesRow
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverviewAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleOverview = PortfolioOverview(
        overdueCount = 2,
        dueTodayCount = 1,
        openInstallmentsAmountCents = 350000L,
        openInstallmentsCount = 5,
        lastInstallmentDueDate = 1759276800000L,
        fixedCostMonthlyCents = 45000L,
        cardsUsagePercentage = 38,
        cardsTotalUsedCents = 38000L,
        cardsTotalLimitCents = 100000L,
        goalsReservePercentage = 85,
        monthlySeries = listOf(
            MonthlyBarData(1000L, "OUT", 12000L, isPast = true, isCurrent = false, isPeak = false, isLowestFuture = false),
            MonthlyBarData(2000L, "NOV", 15000L, isPast = true, isCurrent = false, isPeak = false, isLowestFuture = false),
            MonthlyBarData(3000L, "DEZ", 25000L, isPast = false, isCurrent = true, isPeak = false, isLowestFuture = false),
            MonthlyBarData(4000L, "JAN", 40000L, isPast = false, isCurrent = false, isPeak = true, isLowestFuture = false),
            MonthlyBarData(5000L, "FEV", 10000L, isPast = false, isCurrent = false, isPeak = false, isLowestFuture = true)
        ),
        freeingUpCommitments = listOf(
            FreeingUpCommitment(
                billId = "b1",
                title = "Smartphone Galaxy",
                endDate = 1759276800000L,
                monthlyAmountCents = 25000L
            )
        )
    )

    @Test
    fun overviewComponents_renderInAmoledTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OverviewAttentionBanner(attentionCount = 3, onNavigateToToday = {})
                    OverviewHeroCard(overview = sampleOverview)
                    OverviewTilesRow(overview = sampleOverview)
                    Overview12MonthChart(monthlySeries = sampleOverview.monthlySeries)
                    OverviewFreeingUpList(commitments = sampleOverview.freeingUpCommitments)
                }
            }
        }

        composeTestRule.onNodeWithText("PARCELADO EM ABERTO").assertIsDisplayed()
        composeTestRule.onNodeWithText("CUSTO FIXO/MÊS").assertIsDisplayed()
        composeTestRule.onNodeWithText("CARTÕES EM USO").assertIsDisplayed()
        composeTestRule.onNodeWithText("RESERVA").assertIsDisplayed()
        composeTestRule.onNodeWithText("PROJEÇÃO EM 12 MESES").assertIsDisplayed()
        composeTestRule.onNodeWithText("QUANDO FICA LIVRE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Smartphone Galaxy").assertIsDisplayed()
    }

    @Test
    fun overviewComponents_renderEmptyState() {
        val emptyOverview = PortfolioOverview()

        composeTestRule.setContent {
            PlatformTheme(darkTheme = false) {
                Column {
                    OverviewHeroCard(overview = emptyOverview)
                    OverviewTilesRow(overview = emptyOverview)
                }
            }
        }

        composeTestRule.onNodeWithText("Nada em aberto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nenhuma compra parcelada em aberto no momento.").assertIsDisplayed()
    }

    @Test
    fun overviewComponents_supportHighFontScale() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            val highDensity = Density(
                density = currentDensity.density,
                fontScale = 1.3f
            )

            CompositionLocalProvider(LocalDensity provides highDensity) {
                PlatformTheme {
                    OverviewHeroCard(overview = sampleOverview)
                }
            }
        }

        composeTestRule.onNodeWithText("PARCELADO EM ABERTO").assertIsDisplayed()
    }
}
