package com.platform.app.domain.usecase

import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetDrawerOverviewUseCaseTest {

    private lateinit var financialRepository: FinancialRepository
    private lateinit var calculateMonthlyForecastUseCase: CalculateMonthlyForecastUseCase
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var useCase: GetDrawerOverviewUseCase

    @Before
    fun setUp() {
        financialRepository = mockk()
        calculateMonthlyForecastUseCase = mockk()
        preferencesManager = mockk()
        useCase = GetDrawerOverviewUseCase(
            financialRepository = financialRepository,
            calculateMonthlyForecastUseCase = calculateMonthlyForecastUseCase,
            preferencesManager = preferencesManager
        )
    }

    @Test
    fun `combines forecast remaining, overdue bills and invoices, backup and initial screen`() = runTest {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        }.timeInMillis

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val tomorrow = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        val forecastResult = MonthlyForecastResult(
            monthMillis = now,
            monthLabel = "Outubro 2026",
            totalForecastCents = 154000L,
            totalPaidCents = 50000L
        )

        val overdueBill = BillInstallment(
            id = "inst-1",
            billId = "bill-1",
            billTitle = "Aluguel",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 10000L,
            dueDate = yesterday,
            status = BillStatus.PENDING
        )
        val futureBill = BillInstallment(
            id = "inst-2",
            billId = "bill-2",
            billTitle = "Internet",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 12000L,
            dueDate = tomorrow,
            status = BillStatus.PENDING
        )
        val paidBill = BillInstallment(
            id = "inst-3",
            billId = "bill-3",
            billTitle = "Luz",
            installmentNumber = 1,
            totalInstallments = 1,
            amountCents = 8000L,
            dueDate = yesterday,
            paidAt = yesterday,
            status = BillStatus.PAID
        )

        val overdueInvoice = CreditCardInvoice(
            id = "inv-1",
            creditCardId = "card-1",
            referenceMonth = "2026-09",
            closingDate = yesterday,
            dueDate = yesterday,
            totalAmountCents = 50000L,
            status = InvoiceStatus.FECHADA
        )
        val paidInvoice = CreditCardInvoice(
            id = "inv-2",
            creditCardId = "card-1",
            referenceMonth = "2026-08",
            closingDate = yesterday,
            dueDate = yesterday,
            totalAmountCents = 40000L,
            status = InvoiceStatus.PAGA
        )

        every { calculateMonthlyForecastUseCase(now, now) } returns flowOf(forecastResult)
        every { financialRepository.getAllInstallments() } returns flowOf(listOf(overdueBill, futureBill, paidBill))
        every { financialRepository.getAllCreditCardInvoices() } returns flowOf(listOf(overdueInvoice, paidInvoice))
        every { preferencesManager.lastOfflineBackupTimestamp } returns flowOf(1728640000000L)
        every { preferencesManager.initialScreen } returns flowOf("overview")

        val result = useCase(now).first()

        assertEquals(154000L, result.remainingMonthCents)
        assertEquals(2, result.overdueCount) // 1 bill + 1 invoice
        assertEquals(1728640000000L, result.lastBackupTimestamp)
        assertEquals("overview", result.initialScreen)
    }

    @Test
    fun `when no overdue items, overdueCount is 0`() = runTest {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        }.timeInMillis

        val forecastResult = MonthlyForecastResult(
            monthMillis = now,
            monthLabel = "Outubro 2026",
            totalForecastCents = 0L,
            totalPaidCents = 100000L
        )

        every { calculateMonthlyForecastUseCase(now, now) } returns flowOf(forecastResult)
        every { financialRepository.getAllInstallments() } returns flowOf(emptyList())
        every { financialRepository.getAllCreditCardInvoices() } returns flowOf(emptyList())
        every { preferencesManager.lastOfflineBackupTimestamp } returns flowOf(0L)
        every { preferencesManager.initialScreen } returns flowOf("today")

        val result = useCase(now).first()

        assertEquals(0L, result.remainingMonthCents)
        assertEquals(0, result.overdueCount)
        assertEquals(0L, result.lastBackupTimestamp)
        assertEquals("today", result.initialScreen)
    }
}
