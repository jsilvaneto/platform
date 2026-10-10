package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.Budget
import com.platform.app.domain.repository.BudgetRepository
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetBudgetProgressUseCaseTest {

    private lateinit var budgetRepository: BudgetRepository
    private lateinit var financialRepository: FinancialRepository
    private lateinit var useCase: GetBudgetProgressUseCase

    @Before
    fun setUp() {
        budgetRepository = mockk()
        financialRepository = mockk()
        useCase = GetBudgetProgressUseCase(budgetRepository, financialRepository)
    }

    private fun getMonthDate(year: Int, monthZeroIndexed: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroIndexed)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun `when general budget and category budget both exist, totalSpent is not duplicated`() = runTest {
        val octDate = getMonthDate(2026, Calendar.OCTOBER, 10)

        val generalBudget = Budget(
            id = "b-general",
            categoryId = null,
            categoryName = "Geral",
            limitAmountCents = 100000L, // R$ 1.000,00
            colorHex = "#2196F3"
        )
        val foodBudget = Budget(
            id = "b-food",
            categoryId = "cat-food",
            categoryName = "Alimentação",
            limitAmountCents = 40000L, // R$ 400,00
            colorHex = "#4CAF50"
        )

        val foodInstallment = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Supermercado",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 15000L, // R$ 150,00
            dueDate = octDate,
            createdAt = octDate,
            categoryId = "cat-food",
            status = BillStatus.PAID,
            paidAt = octDate
        )

        every { budgetRepository.getBudgets() } returns flowOf(listOf(generalBudget, foodBudget))
        every { financialRepository.getAllInstallments() } returns flowOf(listOf(foodInstallment))

        val overview = useCase(octDate).first()

        // Total limit is defined by the General Budget
        assertEquals(100000L, overview.totalLimitCents)
        // Total spent must be 15000L, NOT duplicated to 30000L
        assertEquals(15000L, overview.totalSpentCents)
        assertEquals(15000L, overview.totalPaidCents)
        assertEquals(0L, overview.totalPendingCents)

        // General budget item
        val generalItem = overview.budgets.find { it.budget.id == "b-general" }
        assertEquals(15000L, generalItem?.spentCents)

        // Food budget item
        val foodItem = overview.budgets.find { it.budget.id == "b-food" }
        assertEquals(15000L, foodItem?.spentCents)
    }

    @Test
    fun `credit card purchases are accounted on purchase date createdAt rather than invoice dueDate`() = runTest {
        val octPurchaseDate = getMonthDate(2026, Calendar.OCTOBER, 25)
        val novInvoiceDueDate = getMonthDate(2026, Calendar.NOVEMBER, 10)

        val cardBudget = Budget(
            id = "b-tech",
            categoryId = "cat-tech",
            categoryName = "Tecnologia",
            limitAmountCents = 50000L,
            colorHex = "#9C27B0"
        )

        val cardInstallment = BillInstallment(
            id = "inst-card-1",
            billId = "bill-card-1",
            billTitle = "Notebook",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 20000L, // R$ 200,00
            dueDate = novInvoiceDueDate, // Invoice dueDate in November
            createdAt = octPurchaseDate, // Purchase made in October
            creditCardId = "card-nubank",
            categoryId = "cat-tech",
            status = BillStatus.PENDING
        )

        every { budgetRepository.getBudgets() } returns flowOf(listOf(cardBudget))
        every { financialRepository.getAllInstallments() } returns flowOf(listOf(cardInstallment))

        // Querying for October: must include the card purchase
        val octOverview = useCase(octPurchaseDate).first()
        assertEquals(20000L, octOverview.totalSpentCents)
        assertEquals(20000L, octOverview.totalPendingCents)
        assertEquals(0L, octOverview.totalPaidCents)

        // Querying for November: must NOT include the October purchase
        val novOverview = useCase(novInvoiceDueDate).first()
        assertEquals(0L, novOverview.totalSpentCents)
    }

    @Test
    fun `installments with PAUSED status are excluded from budget calculation`() = runTest {
        val octDate = getMonthDate(2026, Calendar.OCTOBER, 15)

        val budget = Budget(
            id = "b-sub",
            categoryId = "cat-sub",
            categoryName = "Assinaturas",
            limitAmountCents = 30000L,
            colorHex = "#FF9800"
        )

        val pausedInstallment = BillInstallment(
            id = "inst-paused",
            billId = "bill-paused",
            billTitle = "Serviço Pausado",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 5000L,
            dueDate = octDate,
            createdAt = octDate,
            categoryId = "cat-sub",
            status = BillStatus.PAUSED
        )

        val activeInstallment = BillInstallment(
            id = "inst-active",
            billId = "bill-active",
            billTitle = "Serviço Ativo",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 8000L,
            dueDate = octDate,
            createdAt = octDate,
            categoryId = "cat-sub",
            status = BillStatus.PENDING
        )

        every { budgetRepository.getBudgets() } returns flowOf(listOf(budget))
        every { financialRepository.getAllInstallments() } returns flowOf(listOf(pausedInstallment, activeInstallment))

        val overview = useCase(octDate).first()

        // Only activeInstallment (8000L) should be counted, pausedInstallment (5000L) must be ignored
        assertEquals(8000L, overview.totalSpentCents)
        val budgetItem = overview.budgets.first()
        assertEquals(8000L, budgetItem.spentCents)
    }

    @Test
    fun `separates paid vs pending amounts correctly and flags exceeded budget`() = runTest {
        val octDate = getMonthDate(2026, Calendar.OCTOBER, 5)

        val budget = Budget(
            id = "b-leisure",
            categoryId = "cat-leisure",
            categoryName = "Lazer",
            limitAmountCents = 10000L, // R$ 100,00
            colorHex = "#E91E63"
        )

        val paidInst = BillInstallment(
            id = "inst-paid",
            billId = "bill-l1",
            billTitle = "Cinema",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 7000L, // R$ 70,00 pago
            dueDate = octDate,
            createdAt = octDate,
            categoryId = "cat-leisure",
            status = BillStatus.PAID,
            paidAt = octDate
        )

        val pendingInst = BillInstallment(
            id = "inst-pending",
            billId = "bill-l2",
            billTitle = "Jogos",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 5000L, // R$ 50,00 pendente (total = 120,00 -> exceeded)
            dueDate = octDate,
            createdAt = octDate,
            categoryId = "cat-leisure",
            status = BillStatus.PENDING
        )

        every { budgetRepository.getBudgets() } returns flowOf(listOf(budget))
        every { financialRepository.getAllInstallments() } returns flowOf(listOf(paidInst, pendingInst))

        val overview = useCase(octDate).first()

        assertEquals(10000L, overview.totalLimitCents)
        assertEquals(7000L, overview.totalPaidCents)
        assertEquals(5000L, overview.totalPendingCents)
        assertEquals(12000L, overview.totalSpentCents)

        val item = overview.budgets.first()
        assertEquals(7000L, item.paidCents)
        assertEquals(5000L, item.pendingCents)
        assertEquals(12000L, item.spentCents)
        assertTrue(item.isExceeded)
        assertEquals(1.2f, item.progress, 0.01f)
    }
}
