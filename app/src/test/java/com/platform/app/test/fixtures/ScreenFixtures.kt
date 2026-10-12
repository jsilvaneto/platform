package com.platform.app.test.fixtures

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
import com.platform.app.presentation.bills.BillsUiState
import com.platform.app.presentation.bills.NewExpenseUiState
import com.platform.app.presentation.creditcards.CreditCardsUiState
import com.platform.app.presentation.dashboard.DashboardUiState
import com.platform.app.presentation.dashboard.StatisticsPeriod
import com.platform.app.presentation.dashboard.StatisticsTab
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.home.HomeViewMode
import java.time.LocalDate

/**
 * Fixtures compartilhadas e realistas para testes de interface, screenshots e auditoria de layout (Prompt 80).
 */
object ScreenFixtures {

    const val FIXED_BASE_TIME = 1792000000000L // 2026-10-15 UTC
    val FIXED_DATE: LocalDate = LocalDate.of(2026, 10, 15)

    // MARK: - Installments Fixtures
    val standardInstallment1 = BillInstallment(
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

    val standardInstallment2 = BillInstallment(
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

    val standardInstallment3 = BillInstallment(
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

    val standardInstallments = listOf(standardInstallment1, standardInstallment2, standardInstallment3)

    // MARK: - Overview (PortfolioOverview)
    val overviewStandard = PortfolioOverview(
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

    val overviewLargeValues = overviewStandard.copy(
        openInstallmentsAmountCents = 987654321L,
        openInstallmentsCount = 96,
        fixedCostMonthlyCents = 85400000L,
        cardsTotalUsedCents = 120500000L,
        cardsTotalLimitCents = 250000000L
    )

    val overviewLongNames = overviewStandard.copy(
        freeingUpCommitments = listOf(
            FreeingUpCommitment("1", "Financiamento Habitacional da Caixa Econômica Federal e Reforma Residencial Completa", FIXED_BASE_TIME + 86400000L * 60, 450000L),
            FreeingUpCommitment("2", "Pós-Graduação Executiva em Engenharia de Software e Inteligência Artificial Avançada", FIXED_BASE_TIME + 86400000L * 90, 250000L)
        )
    )

    val overviewEmpty = PortfolioOverview()

    // MARK: - Home (HomeUiState)
    val homeStandard = HomeUiState(
        viewMode = HomeViewMode.LIST,
        selectedMonthMillis = FIXED_BASE_TIME,
        dashboardMetrics = FinancialDashboardMetrics(
            monthMillis = FIXED_BASE_TIME,
            totalDueMonthCents = 245000L,
            totalPaidMonthCents = 120000L,
            totalPendingMonthCents = 95000L,
            totalOverdueMonthCents = 30000L
        ),
        globalOverdueItems = listOf(PayableItem.BillPayable(standardInstallment2, PayableUrgency.OVERDUE)),
        globalOverdueTotalCents = 24500L,
        next30DaysItems = listOf(PayableItem.BillPayable(standardInstallment1, PayableUrgency.NEXT_7_DAYS)),
        next30DaysTotalCents = 180000L
    )

    val homeEmpty = HomeUiState(
        viewMode = HomeViewMode.LIST,
        selectedMonthMillis = FIXED_BASE_TIME,
        globalOverdueItems = emptyList(),
        next30DaysItems = emptyList()
    )

    val homeLoading = HomeUiState(
        isLoading = true,
        selectedMonthMillis = FIXED_BASE_TIME
    )

    val homeError = HomeUiState(
        errorMessage = "Falha ao sincronizar dados da carteira local",
        selectedMonthMillis = FIXED_BASE_TIME
    )

    // MARK: - Bills (BillsUiState)
    val billsStandard = BillsUiState(
        selectedYear = 2026,
        installments = standardInstallments,
        filteredInstallments = standardInstallments,
        totalPeriodCents = 216490L,
        pendingPeriodCents = 180000L,
        overduePeriodCents = 24500L,
        paidPeriodCents = 11990L
    )

    val billsEmpty = BillsUiState(
        selectedYear = 2026,
        installments = emptyList(),
        filteredInstallments = emptyList()
    )

    val billsLoading = BillsUiState(
        isLoading = true,
        selectedYear = 2026
    )

    val billsError = BillsUiState(
        errorMessage = "Erro ao carregar lançamentos de contas",
        selectedYear = 2026
    )

    // MARK: - Credit Cards (CreditCardsUiState)
    val standardCard = CreditCard(
        id = "card-1",
        name = "Nubank Ultravioleta",
        totalLimitCents = 1500000L,
        closingDay = 25,
        dueDay = 5,
        colorHex = "#820AD1"
    )

    val standardInvoice = CreditCardInvoice(
        id = "inv-1",
        creditCardId = "card-1",
        referenceMonth = "2026-10",
        closingDate = FIXED_DATE.plusDays(10),
        dueDate = FIXED_DATE.plusDays(20),
        status = InvoiceStatus.ABERTA,
        totalAmountCents = 452000L
    )

    val standardCardSummary = CreditCardWithInvoiceSummary(
        card = standardCard,
        currentInvoice = standardInvoice,
        usedLimitCents = 452000L
    )

    val creditCardsStandard = CreditCardsUiState(
        cardsWithSummary = listOf(standardCardSummary),
        selectedCardId = standardCard.id,
        invoicesForSelectedCard = listOf(standardInvoice),
        currentInvoice = standardInvoice,
        installmentsForSelectedInvoice = standardInstallments
    )

    val creditCardsEmpty = CreditCardsUiState(
        cardsWithSummary = emptyList()
    )

    val creditCardsLoading = CreditCardsUiState(
        isLoading = true
    )

    val creditCardsError = CreditCardsUiState(
        errorMessage = "Não foi possível carregar os cartões de crédito"
    )

    // MARK: - Statistics (DashboardUiState)
    val statisticsStandard = DashboardUiState(
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

    val statisticsEmpty = DashboardUiState(
        selectedTab = StatisticsTab.PRESENT,
        selectedPeriod = StatisticsPeriod.MONTH,
        metrics = null
    )

    val statisticsLoading = DashboardUiState(
        isLoading = true,
        selectedTab = StatisticsTab.PRESENT,
        selectedPeriod = StatisticsPeriod.MONTH
    )

    val statisticsError = DashboardUiState(
        errorMessage = "Erro ao consolidar métricas estatísticas",
        selectedTab = StatisticsTab.PRESENT,
        selectedPeriod = StatisticsPeriod.MONTH
    )

    // MARK: - New Expense (NewExpenseUiState)
    val newExpenseStandard = NewExpenseUiState(
        description = "Supermercado Semanal",
        amountCents = 34580L,
        dueDate = FIXED_BASE_TIME + 86400000L * 2,
        expenseType = BillType.SINGLE
    )

    val newExpenseLargeValues = NewExpenseUiState(
        description = "Aquisição de Veículo Elétrico Familiar",
        amountCents = 24999900L,
        dueDate = FIXED_BASE_TIME + 86400000L * 5,
        expenseType = BillType.INSTALLMENT,
        installmentsCount = 48
    )

    val newExpenseLongNames = NewExpenseUiState(
        description = "Supermercado Hipermercado Pão de Açúcar Distribuição Logística e Varejo S/A",
        amountCents = 58940L,
        dueDate = FIXED_BASE_TIME + 86400000L * 3,
        expenseType = BillType.SINGLE
    )
}
