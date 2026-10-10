package com.platform.app.core.notification

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DueReminderCalculatorTest {

    private val repository = mockk<FinancialRepository>()

    private fun createSampleInstallment(id: String, amountCents: Long, dueDate: Long): BillInstallment {
        return BillInstallment(
            id = id,
            billId = "bill-$id",
            billTitle = "Sample Bill $id",
            amountCents = amountCents,
            dueDate = dueDate
        )
    }

    private fun createSampleInvoice(id: String, totalCents: Long, dueDate: Long): CreditCardInvoice {
        return CreditCardInvoice(
            id = id,
            creditCardId = "card-1",
            referenceMonth = "2026-10",
            closingDate = dueDate - 864000000L,
            dueDate = dueDate,
            status = InvoiceStatus.FECHADA,
            totalAmountCents = totalCents
        )
    }

    @Test
    fun calculateItems_returnsEmpty_whenNoItemsFound() = runTest {
        coEvery { repository.getPendingInstallmentsInRange(any(), any()) } returns emptyList()
        coEvery { repository.getPendingInvoicesInRange(any(), any()) } returns emptyList()
        coEvery { repository.getOverduePendingInstallments(any()) } returns emptyList()
        coEvery { repository.getOverdueInvoices(any()) } returns emptyList()

        val items = DueReminderCalculator.calculateItems(
            repository = repository,
            nowMillis = System.currentTimeMillis()
        )

        assertEquals(0, items.totalCount)
        assertEquals(0L, items.totalAmountCents)
        assertTrue(items.todayBills.isEmpty())
        assertTrue(items.todayInvoices.isEmpty())
        assertTrue(items.tomorrowBills.isEmpty())
        assertTrue(items.overdueBills.isEmpty())
    }

    @Test
    fun calculateItems_includesTodayAndComputesTotals() = runTest {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        val todayBill = createSampleInstallment("b1", 15000L, now)
        val todayInvoice = createSampleInvoice("inv1", 45000L, now)

        coEvery { repository.getPendingInstallmentsInRange(any(), any()) } returnsMany listOf(
            listOf(todayBill), // today
            emptyList()        // tomorrow
        )
        coEvery { repository.getPendingInvoicesInRange(any(), any()) } returnsMany listOf(
            listOf(todayInvoice), // today
            emptyList()           // tomorrow
        )
        coEvery { repository.getOverduePendingInstallments(any()) } returns emptyList()
        coEvery { repository.getOverdueInvoices(any()) } returns emptyList()

        val items = DueReminderCalculator.calculateItems(
            repository = repository,
            nowMillis = now,
            notifyTomorrow = true,
            notifyOverdue = true
        )

        assertEquals(2, items.totalCount)
        assertEquals(60000L, items.totalAmountCents)
        assertEquals(1, items.todayBills.size)
        assertEquals(1, items.todayInvoices.size)
    }

    @Test
    fun calculateItems_respectsNotifyTomorrowAndNotifyOverdueFlags() = runTest {
        val now = Calendar.getInstance().timeInMillis

        val todayBill = createSampleInstallment("b1", 10000L, now)
        val overdueBill = createSampleInstallment("b0", 5000L, now - 86400000L)

        coEvery { repository.getPendingInstallmentsInRange(any(), any()) } returns listOf(todayBill)
        coEvery { repository.getPendingInvoicesInRange(any(), any()) } returns emptyList()
        coEvery { repository.getOverduePendingInstallments(any()) } returns listOf(overdueBill)
        coEvery { repository.getOverdueInvoices(any()) } returns emptyList()

        // When notifyTomorrow and notifyOverdue are FALSE
        val itemsDisabled = DueReminderCalculator.calculateItems(
            repository = repository,
            nowMillis = now,
            notifyTomorrow = false,
            notifyOverdue = false
        )

        assertEquals(1, itemsDisabled.totalCount)
        assertEquals(10000L, itemsDisabled.totalAmountCents)
        assertTrue(itemsDisabled.tomorrowBills.isEmpty())
        assertTrue(itemsDisabled.overdueBills.isEmpty())
    }
}
