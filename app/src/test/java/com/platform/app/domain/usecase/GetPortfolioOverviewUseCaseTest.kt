package com.platform.app.domain.usecase

import app.cash.turbine.test
import com.platform.app.domain.model.AttentionCounts
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.CardUsage
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.domain.model.Goal
import com.platform.app.domain.model.OpenInstallmentsSummary
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.repository.GoalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetPortfolioOverviewUseCaseTest {

    private lateinit var financialRepository: FinancialRepository
    private lateinit var getCardUsageUseCase: GetCardUsageUseCase
    private lateinit var goalRepository: GoalRepository
    private lateinit var useCase: GetPortfolioOverviewUseCase

    private val referenceNow = 1759276800000L // 01/10/2025 aprox

    @Before
    fun setUp() {
        financialRepository = mockk(relaxed = true)
        getCardUsageUseCase = mockk(relaxed = true)
        goalRepository = mockk(relaxed = true)
        useCase = GetPortfolioOverviewUseCase(
            financialRepository = financialRepository,
            getCardUsageUseCase = getCardUsageUseCase,
            goalRepository = goalRepository
        )
    }

    @Test
    fun `overview correctly aggregates attention, open installments, tiles, 12-month series and freeing up commitments`() = runTest {
        // Mock Attention
        every { financialRepository.getAttentionCounts(any(), any()) } returns flowOf(
            AttentionCounts(overdueCount = 2, dueTodayCount = 1)
        )

        // Mock Open Installments
        val lastDueDate = referenceNow + 180L * 24 * 3600 * 1000
        every { financialRepository.getOpenInstallmentsOverview() } returns flowOf(
            OpenInstallmentsSummary(
                totalAmountCents = 450000L,
                count = 6,
                lastDueDate = lastDueDate
            )
        )

        // Mock Fixed Cost
        every { financialRepository.getActiveMonthlyRecurringCost() } returns flowOf(35000L)

        // Mock Cards
        val sampleCard = CreditCard(
            id = "c1",
            name = "Nubank",
            totalLimitCents = 100000L,
            closingDay = 5,
            dueDay = 12
        )
        val usage = CardUsage(
            card = sampleCard,
            totalLimitCents = 100000L,
            usedLimitCents = 40000L,
            availableLimitCents = 60000L,
            usagePercentage = 0.40f,
            currentInvoice = null,
            currentInvoiceAmountCents = 0L,
            isLimitExceeded = false
        )
        every { getCardUsageUseCase() } returns flowOf(listOf(usage))

        // Mock Goals
        val goal = Goal(
            id = "g1",
            name = "Reserva",
            targetAmountCents = 1000000L,
            currentAmountCents = 800000L
        )
        every { goalRepository.getGoals() } returns flowOf(listOf(goal))

        // Mock Installments for 12 months
        val futureInstallment1 = BillInstallment(
            id = "i1",
            billId = "b1",
            billTitle = "Notebook",
            amountCents = 50000L,
            dueDate = referenceNow + 35L * 24 * 3600 * 1000,
            status = BillStatus.PENDING
        )
        val futureInstallment2 = BillInstallment(
            id = "i2",
            billId = "b2",
            billTitle = "Curso",
            amountCents = 20000L,
            dueDate = referenceNow + 65L * 24 * 3600 * 1000,
            status = BillStatus.PENDING
        )
        every { financialRepository.getInstallmentsForPeriod(any(), any()) } returns flowOf(
            listOf(futureInstallment1, futureInstallment2)
        )

        // Mock Freeing Up Commitments
        val freeingUp = listOf(
            FreeingUpCommitment(
                billId = "b1",
                title = "Notebook",
                endDate = referenceNow + 120L * 24 * 3600 * 1000,
                monthlyAmountCents = 50000L
            )
        )
        every { financialRepository.getFreeingUpCommitments() } returns flowOf(freeingUp)

        useCase(referenceNow).test {
            val overview = awaitItem()

            // Atenção
            assertEquals(2, overview.overdueCount)
            assertEquals(1, overview.dueTodayCount)
            assertEquals(3, overview.attentionCount)
            assertTrue(overview.hasAttention)

            // Hero Parcelado
            assertEquals(450000L, overview.openInstallmentsAmountCents)
            assertEquals(6, overview.openInstallmentsCount)
            assertEquals(lastDueDate, overview.lastInstallmentDueDate)
            assertTrue(overview.hasDebts)

            // Tiles
            assertEquals(35000L, overview.fixedCostMonthlyCents)
            assertEquals(40, overview.cardsUsagePercentage)
            assertEquals(40000L, overview.cardsTotalUsedCents)
            assertEquals(100000L, overview.cardsTotalLimitCents)
            assertEquals(80, overview.goalsReservePercentage)

            // Série de 12 meses
            assertEquals(12, overview.monthlySeries.size)
            val pastMonths = overview.monthlySeries.filter { it.isPast }
            val currentMonth = overview.monthlySeries.filter { it.isCurrent }
            val futureMonths = overview.monthlySeries.filter { !it.isPast && !it.isCurrent }
            assertEquals(2, pastMonths.size)
            assertEquals(1, currentMonth.size)
            assertEquals(9, futureMonths.size)

            // Quando fica livre
            assertEquals(1, overview.freeingUpCommitments.size)
            assertEquals("Notebook", overview.freeingUpCommitments[0].title)

            assertFalse(overview.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty portfolio returns empty state and no debt flags`() = runTest {
        every { financialRepository.getAttentionCounts(any(), any()) } returns flowOf(AttentionCounts(0, 0))
        every { financialRepository.getOpenInstallmentsOverview() } returns flowOf(OpenInstallmentsSummary(0L, 0, null))
        every { financialRepository.getActiveMonthlyRecurringCost() } returns flowOf(0L)
        every { getCardUsageUseCase() } returns flowOf(emptyList())
        every { goalRepository.getGoals() } returns flowOf(emptyList())
        every { financialRepository.getInstallmentsForPeriod(any(), any()) } returns flowOf(emptyList())
        every { financialRepository.getFreeingUpCommitments() } returns flowOf(emptyList())

        useCase(referenceNow).test {
            val overview = awaitItem()
            assertFalse(overview.hasAttention)
            assertFalse(overview.hasDebts)
            assertTrue(overview.isEmpty)
            assertNull(overview.cardsUsagePercentage)
            assertNull(overview.goalsReservePercentage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
