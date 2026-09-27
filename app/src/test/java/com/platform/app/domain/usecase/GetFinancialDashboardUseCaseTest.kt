package com.platform.app.domain.usecase

import app.cash.turbine.test
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetFinancialDashboardUseCaseTest {

    private lateinit var repository: FinancialRepository
    private lateinit var useCase: GetFinancialDashboardUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetFinancialDashboardUseCase(repository)
    }

    @Test
    fun `should calculate dashboard metrics and future projections accurately`() = runTest {
        val now = System.currentTimeMillis()
        val currentMonthEpoch = DateUtils.getStartOfMonth(now)
        val endOfCurrentMonth = DateUtils.getEndOfMonth(now)

        val paidInst = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Internet Fibra",
            categoryId = "cat-1",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 15000L, // R$ 150,00
            dueDate = currentMonthEpoch + 1000L,
            paidAt = currentMonthEpoch + 1000L,
            categoryName = "Moradia",
            categoryColorHex = "#4CAF50"
        )

        val pendingInst = BillInstallment(
            id = "inst-2",
            billId = "bill-2",
            billTitle = "Energia Elétrica",
            categoryId = "cat-1",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 25000L, // R$ 250,00
            dueDate = endOfCurrentMonth - 1000L, // final do mês atual (garantido pendente e no mês atual)
            paidAt = null,
            categoryName = "Moradia",
            categoryColorHex = "#4CAF50"
        )

        val nextMonthEpoch = DateUtils.addMonths(currentMonthEpoch, 1)
        val futureStart = DateUtils.getStartOfMonth(nextMonthEpoch)
        val futureInst = BillInstallment(
            id = "inst-3",
            billId = "bill-3",
            billTitle = "Seguro Carro 2/10",
            categoryId = "cat-2",
            installmentNumber = 2,
            totalInstallments = 10,
            amountCents = 30000L, // R$ 300,00
            dueDate = futureStart + (2L * 86400000L), // dia 3 do próximo mês
            paidAt = null,
            categoryName = "Transporte",
            categoryColorHex = "#2196F3"
        )

        val monthInstallments = listOf(paidInst, pendingInst)
        val allInstallments = listOf(paidInst, pendingInst, futureInst)

        every { repository.getInstallmentsForPeriod(any(), any()) } returns flowOf(monthInstallments)
        every { repository.getAllInstallments() } returns flowOf(allInstallments)

        useCase(currentMonthEpoch).test {
            val metrics = awaitItem()

            // Month totals
            assertEquals(40000L, metrics.totalDueMonthCents) // 150 + 250
            assertEquals(15000L, metrics.totalPaidMonthCents) // 150
            assertEquals(25000L, metrics.totalPendingMonthCents) // 250
            assertEquals(0L, metrics.totalOverdueMonthCents)

            // Future projections (6 months)
            assertEquals(6, metrics.futureMonthsProjections.size)
            val monthPlusOne = metrics.futureMonthsProjections.first()
            assertEquals(30000L, monthPlusOne.totalCommittedCents)
            assertEquals(1, monthPlusOne.installmentsCount)

            // Total future committed: pendingInst (250) + futureInst (300) = 550
            assertEquals(55000L, metrics.totalCommittedFutureCents)

            // Category distribution
            assertEquals(1, metrics.categoryDistribution.size)
            assertEquals("Moradia", metrics.categoryDistribution.first().categoryName)
            assertEquals(40000L, metrics.categoryDistribution.first().amountCents)
            assertEquals(100f, metrics.categoryDistribution.first().percentage, 0.01f)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
