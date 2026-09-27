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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

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
        val endOfMonthEpoch = DateUtils.getEndOfMonth(now)

        val paidInst = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Internet Fibra",
            amountCents = 15000L, // R$ 150,00
            dueDate = currentMonthEpoch + 86400000L,
            paidAt = currentMonthEpoch + 86400000L,
            categoryName = "Moradia",
            categoryColorHex = "#4CAF50"
        )

        val pendingInst = BillInstallment(
            id = "inst-2",
            billId = "bill-2",
            billTitle = "Energia Elétrica",
            amountCents = 25000L, // R$ 250,00
            dueDate = now + (2L * 86400000L), // daqui a 2 dias (dentro dos 7 dias)
            paidAt = null,
            categoryName = "Moradia",
            categoryColorHex = "#4CAF50"
        )

        val nextMonthEpoch = DateUtils.addMonths(currentMonthEpoch, 1)
        val futureInst = BillInstallment(
            id = "inst-3",
            billId = "bill-3",
            billTitle = "Seguro Carro 2/10",
            amountCents = 30000L, // R$ 300,00
            dueDate = nextMonthEpoch + 86400000L,
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

            // Upcoming week (pendingInst is in next 2 days)
            assertEquals(1, metrics.upcomingWeekInstallments.size)
            assertEquals("inst-2", metrics.upcomingWeekInstallments.first().id)

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
