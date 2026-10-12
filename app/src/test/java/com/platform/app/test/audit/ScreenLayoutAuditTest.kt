package com.platform.app.test.audit

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
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.platform.app.presentation.bills.BillsScreen
import com.platform.app.presentation.bills.NewExpenseContent
import com.platform.app.presentation.creditcards.CreditCardsScreen
import com.platform.app.presentation.home.HomeScreen
import com.platform.app.presentation.navigation.DrawerContent
import com.platform.app.presentation.navigation.DrawerWidth
import com.platform.app.presentation.navigation.Screen
import com.platform.app.presentation.overview.OverviewContent
import com.platform.app.presentation.screenshot.ThemeVariant
import com.platform.app.presentation.statistics.StatisticsScreen
import com.platform.app.presentation.theme.PlatformTheme
import com.platform.app.test.fixtures.ScreenFixtures
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Suite de Auditoria Automática de Layout (Prompt 80).
 * Avalia telas em todas as fontes (1.0x, 1.3x, 2.0x), temas (AMOLED, Dark, Light)
 * e estados realistas (valores grandes, nomes longos, vazio, carregando e erro).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel6)
class ScreenLayoutAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun audit(
        screenName: String,
        theme: ThemeVariant = ThemeVariant.DARK,
        fontScale: Float = 1.0f,
        content: @Composable () -> Unit
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = fontScale
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
            fontScale = fontScale
        )
    }

    // --- VISÃO GERAL ---
    @Test
    fun audit_overview_font1_0() = audit("Overview (1.0x)", fontScale = 1.0f) {
        OverviewContent(overview = ScreenFixtures.overviewStandard, onOpenDrawer = {})
    }

    @Test
    fun audit_overview_font1_3() = audit("Overview (1.3x)", fontScale = 1.3f) {
        OverviewContent(overview = ScreenFixtures.overviewStandard, onOpenDrawer = {})
    }

    @Test
    fun audit_overview_font2_0_large_values() = audit("Overview (2.0x Large Values)", fontScale = 2.0f) {
        OverviewContent(overview = ScreenFixtures.overviewLargeValues, onOpenDrawer = {})
    }

    @Test
    fun audit_overview_long_names() = audit("Overview (Long Names)", fontScale = 1.3f) {
        OverviewContent(overview = ScreenFixtures.overviewLongNames, onOpenDrawer = {})
    }

    @Test
    fun audit_overview_empty() = audit("Overview (Empty)", fontScale = 1.0f) {
        OverviewContent(overview = ScreenFixtures.overviewEmpty, onOpenDrawer = {})
    }

    // --- HOJE (HOME) ---
    @Test
    fun audit_home_font1_0() = audit("Home (1.0x)", fontScale = 1.0f) {
        HomeScreen(uiState = ScreenFixtures.homeStandard, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun audit_home_font1_3() = audit("Home (1.3x)", fontScale = 1.3f) {
        HomeScreen(uiState = ScreenFixtures.homeStandard, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun audit_home_font2_0() = audit("Home (2.0x)", fontScale = 2.0f) {
        HomeScreen(uiState = ScreenFixtures.homeStandard, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun audit_home_empty() = audit("Home (Empty)", fontScale = 1.0f) {
        HomeScreen(uiState = ScreenFixtures.homeEmpty, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun audit_home_error() = audit("Home (Error)", fontScale = 1.0f) {
        HomeScreen(uiState = ScreenFixtures.homeError, onAction = {}, onNavigateToNewExpense = {})
    }

    // --- CONTAS (BILLS) ---
    @Test
    fun audit_bills_font1_0() = audit("Bills (1.0x)", fontScale = 1.0f) {
        BillsScreen(uiState = ScreenFixtures.billsStandard, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun audit_bills_font1_3() = audit("Bills (1.3x)", fontScale = 1.3f) {
        BillsScreen(uiState = ScreenFixtures.billsStandard, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun audit_bills_font2_0() = audit("Bills (2.0x)", fontScale = 2.0f) {
        BillsScreen(uiState = ScreenFixtures.billsStandard, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun audit_bills_empty() = audit("Bills (Empty)", fontScale = 1.0f) {
        BillsScreen(uiState = ScreenFixtures.billsEmpty, uiEffect = emptyFlow(), onAction = {})
    }

    // --- CARTÕES (CREDIT CARDS) ---
    @Test
    fun audit_credit_cards_font1_0() = audit("CreditCards (1.0x)", fontScale = 1.0f) {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsStandard, onAction = {})
    }

    @Test
    fun audit_credit_cards_font1_3() = audit("CreditCards (1.3x)", fontScale = 1.3f) {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsStandard, onAction = {})
    }

    @Test
    fun audit_credit_cards_font2_0() = audit("CreditCards (2.0x)", fontScale = 2.0f) {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsStandard, onAction = {})
    }

    @Test
    fun audit_credit_cards_empty() = audit("CreditCards (Empty)", fontScale = 1.0f) {
        CreditCardsScreen(uiState = ScreenFixtures.creditCardsEmpty, onAction = {})
    }

    // --- ANÁLISES (STATISTICS) ---
    @Test
    fun audit_statistics_font1_0() = audit("Statistics (1.0x)", fontScale = 1.0f) {
        StatisticsScreen(uiState = ScreenFixtures.statisticsStandard, onAction = {})
    }

    @Test
    fun audit_statistics_font1_3() = audit("Statistics (1.3x)", fontScale = 1.3f) {
        StatisticsScreen(uiState = ScreenFixtures.statisticsStandard, onAction = {})
    }

    @Test
    fun audit_statistics_font2_0() = audit("Statistics (2.0x)", fontScale = 2.0f) {
        StatisticsScreen(uiState = ScreenFixtures.statisticsStandard, onAction = {})
    }

    @Test
    fun audit_statistics_empty() = audit("Statistics (Empty)", fontScale = 1.0f) {
        StatisticsScreen(uiState = ScreenFixtures.statisticsEmpty, onAction = {})
    }

    // --- LANÇAMENTO RÁPIDO (NEW EXPENSE) ---
    @Test
    fun audit_new_expense_font1_0() = audit("NewExpense (1.0x)", fontScale = 1.0f) {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseStandard, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun audit_new_expense_font1_3() = audit("NewExpense (1.3x)", fontScale = 1.3f) {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseStandard, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun audit_new_expense_font2_0_large() = audit("NewExpense (2.0x Large Values)", fontScale = 2.0f) {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseLargeValues, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun audit_new_expense_long_names() = audit("NewExpense (Long Names)", fontScale = 1.3f) {
        NewExpenseContent(uiState = ScreenFixtures.newExpenseLongNames, onNavigateBack = {}, onSaveClick = {})
    }

    // --- MENU LATERAL (DRAWER) ---
    @Test
    fun audit_drawer_font1_0() = audit("Drawer (1.0x)", fontScale = 1.0f) {
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
    fun audit_drawer_font1_3() = audit("Drawer (1.3x)", fontScale = 1.3f) {
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
    fun audit_drawer_font2_0() = audit("Drawer (2.0x)", fontScale = 2.0f) {
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
