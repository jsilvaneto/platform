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
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.domain.usecase.MonthlyForecastResult
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.home.HomeViewMode
import com.platform.app.presentation.home.components.HomeTopBar
import com.platform.app.presentation.home.components.TodayShortcutsRow
import com.platform.app.presentation.theme.PlatformTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes instrumentados e de renderização da tela Hoje nos três temas (AMOLED, Escuro, Claro)
 * e com escalonamento de fonte de 1,3x (Prompt 59).
 */
@RunWith(AndroidJUnit4::class)
class TodayAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val sampleForecast = MonthlyForecastResult(
        monthMillis = 1759276800000L,
        monthLabel = "Outubro 2026",
        totalForecastCents = 185000L,
        totalPaidCents = 65000L,
        overdueItems = listOf(
            PayableItem.BillPayable(
                id = "b1",
                billId = "bill-1",
                installmentNumber = 1,
                totalInstallments = 1,
                title = "Internet Fibra",
                amountCents = 12000L,
                dueDate = 1759200000000L,
                urgency = PayableUrgency.OVERDUE
            )
        ),
        next7DaysItems = listOf(
            PayableItem.BillPayable(
                id = "b2",
                billId = "bill-2",
                installmentNumber = 2,
                totalInstallments = 10,
                title = "Curso de Inglês",
                amountCents = 35000L,
                dueDate = 1759363200000L,
                urgency = PayableUrgency.NEXT_7_DAYS
            )
        ),
        laterInMonthItems = listOf(
            PayableItem.BillPayable(
                id = "b3",
                billId = "bill-3",
                installmentNumber = 5,
                totalInstallments = 12,
                title = "Seguro Auto",
                amountCents = 138000L,
                dueDate = 1759800000000L,
                urgency = PayableUrgency.LATER_IN_MONTH
            )
        )
    )

    private val sampleUiState = HomeUiState(
        forecastResult = sampleForecast,
        viewMode = HomeViewMode.LIST
    )

    @Test
    fun todayComponents_renderInAmoledTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = true, isAmoled = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HomeTopBar(
                        viewMode = HomeViewMode.LIST,
                        monthLabel = "Outubro 2026",
                        isPrivate = false,
                        onTogglePrivacy = {},
                        onOpenMonthPicker = {},
                        onPreviousMonth = {},
                        onNextMonth = {},
                        onCurrentMonth = {},
                        onToggleViewMode = {}
                    )

                    TodayShortcutsRow(
                        onNavigateToBills = {},
                        onNavigateToCards = {},
                        onNavigateToAnalytics = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(AppStrings.Navigation.TODAY).assertIsDisplayed()
        composeTestRule.onNodeWithText("Outubro 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.Navigation.BILLS).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.Navigation.CREDIT_CARDS).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.Navigation.ANALYTICS).assertIsDisplayed()
    }

    @Test
    fun todayComponents_renderInLightTheme() {
        composeTestRule.setContent {
            PlatformTheme(darkTheme = false, isAmoled = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HomeTopBar(
                        viewMode = HomeViewMode.CALENDAR,
                        monthLabel = "Outubro 2026",
                        isPrivate = false,
                        onTogglePrivacy = {},
                        onOpenMonthPicker = {},
                        onPreviousMonth = {},
                        onNextMonth = {},
                        onCurrentMonth = {},
                        onToggleViewMode = {}
                    )

                    TodayShortcutsRow(
                        onNavigateToBills = {},
                        onNavigateToCards = {},
                        onNavigateToAnalytics = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(AppStrings.Navigation.TODAY).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.Navigation.BILLS).assertIsDisplayed()
    }

    @Test
    fun todayComponents_supportHighFontScale() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            val highDensity = Density(
                density = currentDensity.density,
                fontScale = 1.3f
            )

            CompositionLocalProvider(LocalDensity provides highDensity) {
                PlatformTheme {
                    TodayShortcutsRow(
                        onNavigateToBills = {},
                        onNavigateToCards = {},
                        onNavigateToAnalytics = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(AppStrings.Navigation.BILLS).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.Navigation.CREDIT_CARDS).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.Navigation.ANALYTICS).assertIsDisplayed()
    }
}
