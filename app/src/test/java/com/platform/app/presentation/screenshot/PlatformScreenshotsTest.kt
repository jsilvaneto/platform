package com.platform.app.presentation.screenshot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.platform.app.presentation.bills.BillsScreen
import com.platform.app.presentation.bills.NewExpenseContent
import com.platform.app.presentation.creditcards.CreditCardsScreen
import com.platform.app.presentation.home.HomeScreen
import com.platform.app.presentation.navigation.DrawerContent
import com.platform.app.presentation.navigation.DrawerWidth
import com.platform.app.presentation.navigation.Screen
import com.platform.app.presentation.overview.OverviewContent
import com.platform.app.presentation.statistics.StatisticsScreen
import com.platform.app.presentation.theme.PlatformTheme
import com.platform.app.test.audit.LayoutAudit
import com.platform.app.test.fixtures.ScreenFixtures
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

enum class ThemeVariant(val isDark: Boolean, val isAmoled: Boolean, val suffix: String) {
    AMOLED(isDark = true, isAmoled = true, suffix = "amoled"),
    DARK(isDark = true, isAmoled = false, suffix = "dark"),
    LIGHT(isDark = false, isAmoled = false, suffix = "light")
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel6)
class PlatformScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun capture(
        theme: ThemeVariant,
        screenName: String,
        content: @Composable () -> Unit
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = 1.3f
                )
            ) {
                PlatformTheme(
                    darkTheme = theme.isDark,
                    isAmoled = theme.isAmoled
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        content()
                    }
                }
            }
        }
        LayoutAudit.assertNoLayoutDefects(
            rule = composeTestRule,
            screenName = screenName,
            themeName = theme.suffix,
            fontScale = 1.3f
        )
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/snapshots/images/${screenName}_${theme.suffix}_font1.3.png"
        )
    }

    // 1. Visão Geral
    @Test
    fun overview_amoled() = capture(ThemeVariant.AMOLED, "overview_screen") {
        OverviewContent(overview = ScreenFixtures.overviewStandard, onOpenDrawer = {})
    }

    @Test
    fun overview_dark() = capture(ThemeVariant.DARK, "overview_screen") {
        OverviewContent(overview = ScreenFixtures.overviewStandard, onOpenDrawer = {})
    }

    @Test
    fun overview_light() = capture(ThemeVariant.LIGHT, "overview_screen") {
        OverviewContent(overview = ScreenFixtures.overviewStandard, onOpenDrawer = {})
    }

    // 2. Hoje (Home)
    @Test
    fun home_amoled() = capture(ThemeVariant.AMOLED, "home_screen") {
        HomeScreen(uiState = ScreenFixtures.homeStandard, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun home_dark() = capture(ThemeVariant.DARK, "home_screen") {
        HomeScreen(uiState = ScreenFixtures.homeStandard, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun home_light() = capture(ThemeVariant.LIGHT, "home_screen") {
        HomeScreen(uiState = ScreenFixtures.homeStandard, onAction = {}, onNavigateToNewExpense = {})
    }

    // 3. Contas (Bills)
    @Test
    fun bills_amoled() = capture(ThemeVariant.AMOLED, "bills_screen") {
        BillsScreen(uiState = ScreenFixtures.billsStandard, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun bills_dark() = capture(ThemeVariant.DARK, "bills_screen") {
        BillsScreen(uiState = ScreenFixtures.billsStandard, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun bills_light() = capture(ThemeVariant.LIGHT, "bills_screen") {
        BillsScreen(uiState = ScreenFixtures.billsStandard, uiEffect = emptyFlow(), onAction = {})
    }

    // 4. Cartões (CreditCards)
    @Test
    fun credit_cards_amoled() = capture(ThemeVariant.AMOLED, "credit_cards_screen") {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsStandard, onAction = {})
    }

    @Test
    fun credit_cards_dark() = capture(ThemeVariant.DARK, "credit_cards_screen") {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsStandard, onAction = {})
    }

    @Test
    fun credit_cards_light() = capture(ThemeVariant.LIGHT, "credit_cards_screen") {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsStandard, onAction = {})
    }

    // 5. Análises (Statistics)
    @Test
    fun statistics_amoled() = capture(ThemeVariant.AMOLED, "statistics_screen") {
        StatisticsScreen(uiState = ScreenFixtures.statisticsStandard, onAction = {})
    }

    @Test
    fun statistics_dark() = capture(ThemeVariant.DARK, "statistics_screen") {
        StatisticsScreen(uiState = ScreenFixtures.statisticsStandard, onAction = {})
    }

    @Test
    fun statistics_light() = capture(ThemeVariant.LIGHT, "statistics_screen") {
        StatisticsScreen(uiState = ScreenFixtures.statisticsStandard, onAction = {})
    }

    // 6. Lançamento Rápido (NewExpense)
    @Test
    fun new_expense_amoled() = capture(ThemeVariant.AMOLED, "new_expense_screen") {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseStandard, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun new_expense_dark() = capture(ThemeVariant.DARK, "new_expense_screen") {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseStandard, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun new_expense_light() = capture(ThemeVariant.LIGHT, "new_expense_screen") {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseStandard, onNavigateBack = {}, onSaveClick = {})
    }

    // 7. Menu Lateral (Drawer)
    @Test
    fun drawer_amoled() = capture(ThemeVariant.AMOLED, "drawer_menu") {
        ModalDrawerSheet(
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(DrawerWidth)
        ) {
            DrawerContent(
                currentRoute = Screen.Overview.route,
                monthRemainingCents = 145000L,
                overdueCount = 2,
                lastBackupTimestamp = ScreenFixtures.FIXED_BASE_TIME - 86400000L,
                onNavigate = {},
                onCloseDrawer = {}
            )
        }
    }

    @Test
    fun drawer_dark() = capture(ThemeVariant.DARK, "drawer_menu") {
        ModalDrawerSheet(
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(DrawerWidth)
        ) {
            DrawerContent(
                currentRoute = Screen.Overview.route,
                monthRemainingCents = 145000L,
                overdueCount = 2,
                lastBackupTimestamp = ScreenFixtures.FIXED_BASE_TIME - 86400000L,
                onNavigate = {},
                onCloseDrawer = {}
            )
        }
    }

    @Test
    fun drawer_light() = capture(ThemeVariant.LIGHT, "drawer_menu") {
        ModalDrawerSheet(
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(DrawerWidth)
        ) {
            DrawerContent(
                currentRoute = Screen.Overview.route,
                monthRemainingCents = 145000L,
                overdueCount = 2,
                lastBackupTimestamp = ScreenFixtures.FIXED_BASE_TIME - 86400000L,
                onNavigate = {},
                onCloseDrawer = {}
            )
        }
    }
}
