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
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.MonthlyBarData
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.domain.model.PortfolioOverview
import com.platform.app.presentation.bills.BillsScreen
import com.platform.app.presentation.bills.BillsUiState
import com.platform.app.presentation.bills.NewExpenseContent
import com.platform.app.presentation.bills.NewExpenseUiState
import com.platform.app.presentation.creditcards.CreditCardsScreen
import com.platform.app.presentation.creditcards.CreditCardsUiState
import com.platform.app.presentation.dashboard.DashboardUiState
import com.platform.app.presentation.dashboard.StatisticsPeriod
import com.platform.app.presentation.dashboard.StatisticsTab
import com.platform.app.presentation.home.HomeScreen
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.home.HomeViewMode
import com.platform.app.presentation.navigation.DrawerContent
import com.platform.app.presentation.navigation.DrawerWidth
import com.platform.app.presentation.navigation.Screen
import com.platform.app.presentation.overview.OverviewContent
import com.platform.app.presentation.statistics.StatisticsScreen
import com.platform.app.presentation.theme.PlatformTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

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
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/snapshots/images/${screenName}_${theme.suffix}_font1.3.png"
        )
    }

    companion object {
        private const val FIXED_BASE_TIME = 1792000000000L
        private val FIXED_DATE = LocalDate.of(2026, 10, 15)
    }

    // Fixtures
    private val mockOverview = PortfolioOverview(
        overdueCount = 2,
        dueTodayCount = 1,
        openInstallmentsAmountCents = 384000L,
        openInstallmentsCount = 14,
        lastInstallmentDueDate = FIXED_BASE_TIME + 86400000L * 150,
        fixedCostMonthlyCents = 175000L,
        cardsUsagePercentage = 42,
        cardsTotalUsedCents = 145000L,
        cardsTotalLimitCents = 350000L,
        goalsReservePercentage = 65,
        monthlySeries = listOf(
            MonthlyBarData(1L, "Jan", 210000L, isPast = true, isCurrent = false, isPeak = false, isLowestFuture = false),
            MonthlyBarData(2L, "Fev", 195000L, isPast = true, isCurrent = false, isPeak = false, isLowestFuture = false),
            MonthlyBarData(3L, "Mar", 230000L, isPast = false, isCurrent = true, isPeak = false, isLowestFuture = false),
            MonthlyBarData(4L, "Abr", 280000L, isPast = false, isCurrent = false, isPeak = true, isLowestFuture = false),
            MonthlyBarData(5L, "Mai", 150000L, isPast = false, isCurrent = false, isPeak = false, isLowestFuture = true),
            MonthlyBarData(6L, "Jun", 160000L, isPast = false, isCurrent = false, isPeak = false, isLowestFuture = false)
        ),
        freeingUpCommitments = listOf(
            FreeingUpCommitment("1", "Financiamento Carro", FIXED_BASE_TIME + 86400000L * 60, 45000L),
            FreeingUpCommitment("2", "Curso de Inglês", FIXED_BASE_TIME + 86400000L * 90, 25000L)
        )
    )

    private val mockInstallment1 = BillInstallment(
        id = "inst-1",
        billId = "bill-1",
        billTitle = "Aluguel Apartamento",
        amountCents = 180000L,
        dueDate = FIXED_DATE.plusDays(2),
        categoryName = "Moradia",
        categoryColorHex = "#3B82F6",
        nature = ExpenseNature.NECESSARIO,
        status = BillStatus.PENDING,
        installmentNumber = 4,
        totalInstallments = 12
    )

    private val mockInstallment2 = BillInstallment(
        id = "inst-2",
        billId = "bill-2",
        billTitle = "Energia Elétrica",
        amountCents = 24500L,
        dueDate = FIXED_DATE.minusDays(1),
        categoryName = "Utilidades",
        categoryColorHex = "#F59E0B",
        nature = ExpenseNature.NECESSARIO,
        status = BillStatus.PENDING,
        installmentNumber = 1,
        totalInstallments = 1
    )

    private val mockInstallment3 = BillInstallment(
        id = "inst-3",
        billId = "bill-3",
        billTitle = "Internet Fibra",
        amountCents = 11990L,
        dueDate = FIXED_DATE.minusDays(5),
        categoryName = "Serviços",
        categoryColorHex = "#10B981",
        nature = ExpenseNature.NECESSARIO,
        status = BillStatus.PAID,
        paidAt = FIXED_BASE_TIME - 86400000L * 5,
        installmentNumber = 1,
        totalInstallments = 1
    )

    private val mockInstallments = listOf(mockInstallment1, mockInstallment2, mockInstallment3)

    private val mockHomeState = HomeUiState(
        viewMode = HomeViewMode.LIST,
        selectedMonthMillis = FIXED_BASE_TIME,
        dashboardMetrics = FinancialDashboardMetrics(
            monthMillis = FIXED_BASE_TIME,
            totalDueMonthCents = 245000L,
            totalPaidMonthCents = 120000L,
            totalPendingMonthCents = 95000L,
            totalOverdueMonthCents = 30000L
        ),
        globalOverdueItems = listOf(PayableItem.BillPayable(mockInstallment2, PayableUrgency.OVERDUE)),
        globalOverdueTotalCents = 24500L,
        next30DaysItems = listOf(PayableItem.BillPayable(mockInstallment1, PayableUrgency.NEXT_7_DAYS)),
        next30DaysTotalCents = 180000L
    )

    private val mockBillsState = BillsUiState(
        selectedYear = 2026,
        installments = mockInstallments,
        filteredInstallments = mockInstallments,
        totalPeriodCents = 216490L,
        pendingPeriodCents = 180000L,
        overduePeriodCents = 24500L,
        paidPeriodCents = 11990L
    )

    private val mockCard = CreditCard(
        id = "card-1",
        name = "Nubank Ultravioleta",
        totalLimitCents = 1500000L,
        closingDay = 25,
        dueDay = 5,
        colorHex = "#820AD1"
    )

    private val mockInvoice = CreditCardInvoice(
        id = "inv-1",
        creditCardId = "card-1",
        referenceMonth = "2026-10",
        closingDate = FIXED_DATE.plusDays(10),
        dueDate = FIXED_DATE.plusDays(20),
        status = InvoiceStatus.ABERTA,
        totalAmountCents = 452000L
    )

    private val mockCreditCardSummary = CreditCardWithInvoiceSummary(
        card = mockCard,
        currentInvoice = mockInvoice,
        usedLimitCents = 452000L
    )

    private val mockCreditCardsState = CreditCardsUiState(
        cardsWithSummary = listOf(mockCreditCardSummary),
        selectedCardId = mockCard.id,
        invoicesForSelectedCard = listOf(mockInvoice),
        currentInvoice = mockInvoice,
        installmentsForSelectedInvoice = mockInstallments
    )

    private val mockStatisticsState = DashboardUiState(
        selectedTab = StatisticsTab.PRESENT,
        selectedPeriod = StatisticsPeriod.MONTH,
        metrics = FinancialDashboardMetrics(
            monthMillis = FIXED_BASE_TIME,
            currentMonthLabel = "Outubro 2026",
            totalDueMonthCents = 320000L,
            totalPaidMonthCents = 180000L,
            totalPendingMonthCents = 140000L,
            onTimePaymentRate = 92,
            savingsCents = 45000L,
            savingsPercentage = 14.5f
        )
    )

    private val mockNewExpenseState = NewExpenseUiState(
        description = "Supermercado Semanal",
        amountCents = 34580L,
        dueDate = FIXED_BASE_TIME + 86400000L * 2,
        expenseType = BillType.SINGLE
    )

    // 1. Visão Geral
    @Test
    fun overview_amoled() = capture(ThemeVariant.AMOLED, "overview_screen") {
        OverviewContent(overview = mockOverview, onOpenDrawer = {})
    }

    @Test
    fun overview_dark() = capture(ThemeVariant.DARK, "overview_screen") {
        OverviewContent(overview = mockOverview, onOpenDrawer = {})
    }

    @Test
    fun overview_light() = capture(ThemeVariant.LIGHT, "overview_screen") {
        OverviewContent(overview = mockOverview, onOpenDrawer = {})
    }

    // 2. Hoje (Home)
    @Test
    fun home_amoled() = capture(ThemeVariant.AMOLED, "home_screen") {
        HomeScreen(uiState = mockHomeState, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun home_dark() = capture(ThemeVariant.DARK, "home_screen") {
        HomeScreen(uiState = mockHomeState, onAction = {}, onNavigateToNewExpense = {})
    }

    @Test
    fun home_light() = capture(ThemeVariant.LIGHT, "home_screen") {
        HomeScreen(uiState = mockHomeState, onAction = {}, onNavigateToNewExpense = {})
    }

    // 3. Contas (Bills)
    @Test
    fun bills_amoled() = capture(ThemeVariant.AMOLED, "bills_screen") {
        BillsScreen(uiState = mockBillsState, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun bills_dark() = capture(ThemeVariant.DARK, "bills_screen") {
        BillsScreen(uiState = mockBillsState, uiEffect = emptyFlow(), onAction = {})
    }

    @Test
    fun bills_light() = capture(ThemeVariant.LIGHT, "bills_screen") {
        BillsScreen(uiState = mockBillsState, uiEffect = emptyFlow(), onAction = {})
    }

    // 4. Cartões (CreditCards)
    @Test
    fun credit_cards_amoled() = capture(ThemeVariant.AMOLED, "credit_cards_screen") {
        CreditCardsScreen(uiState = mockCreditCardsState, onAction = {})
    }

    @Test
    fun credit_cards_dark() = capture(ThemeVariant.DARK, "credit_cards_screen") {
        CreditCardsScreen(uiState = mockCreditCardsState, onAction = {})
    }

    @Test
    fun credit_cards_light() = capture(ThemeVariant.LIGHT, "credit_cards_screen") {
        CreditCardsScreen(uiState = mockCreditCardsState, onAction = {})
    }

    // 5. Análises (Statistics)
    @Test
    fun statistics_amoled() = capture(ThemeVariant.AMOLED, "statistics_screen") {
        StatisticsScreen(uiState = mockStatisticsState, onAction = {})
    }

    @Test
    fun statistics_dark() = capture(ThemeVariant.DARK, "statistics_screen") {
        StatisticsScreen(uiState = mockStatisticsState, onAction = {})
    }

    @Test
    fun statistics_light() = capture(ThemeVariant.LIGHT, "statistics_screen") {
        StatisticsScreen(uiState = mockStatisticsState, onAction = {})
    }

    // 6. Lançamento Rápido (NewExpense)
    @Test
    fun new_expense_amoled() = capture(ThemeVariant.AMOLED, "new_expense_screen") {
        NewExpenseContent(uiState = mockNewExpenseState, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun new_expense_dark() = capture(ThemeVariant.DARK, "new_expense_screen") {
        NewExpenseContent(uiState = mockNewExpenseState, onNavigateBack = {}, onSaveClick = {})
    }

    @Test
    fun new_expense_light() = capture(ThemeVariant.LIGHT, "new_expense_screen") {
        NewExpenseContent(uiState = mockNewExpenseState, onNavigateBack = {}, onSaveClick = {})
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
                lastBackupTimestamp = FIXED_BASE_TIME - 86400000L,
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
                lastBackupTimestamp = FIXED_BASE_TIME - 86400000L,
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
                lastBackupTimestamp = FIXED_BASE_TIME - 86400000L,
                onNavigate = {},
                onCloseDrawer = {}
            )
        }
    }
}
