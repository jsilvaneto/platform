package com.platform.app.core.notification

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.repository.FinancialRepository
import java.util.Calendar

data class NotificationItems(
    val overdueBills: List<BillInstallment> = emptyList(),
    val overdueInvoices: List<CreditCardInvoice> = emptyList(),
    val todayBills: List<BillInstallment> = emptyList(),
    val todayInvoices: List<CreditCardInvoice> = emptyList(),
    val tomorrowBills: List<BillInstallment> = emptyList(),
    val tomorrowInvoices: List<CreditCardInvoice> = emptyList()
) {
    val totalCount: Int
        get() = overdueBills.size + overdueInvoices.size +
            todayBills.size + todayInvoices.size +
            tomorrowBills.size + tomorrowInvoices.size

    val totalAmountCents: Long
        get() = overdueBills.sumOf { it.amountCents } +
            overdueInvoices.sumOf { it.totalAmountCents } +
            todayBills.sumOf { it.amountCents } +
            todayInvoices.sumOf { it.totalAmountCents } +
            tomorrowBills.sumOf { it.amountCents } +
            tomorrowInvoices.sumOf { it.totalAmountCents }
}

object DueReminderCalculator {

    suspend fun calculateItems(
        repository: FinancialRepository,
        nowMillis: Long,
        notifyTomorrow: Boolean = true,
        notifyOverdue: Boolean = true
    ): NotificationItems {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis

        val startOfTomorrow = endOfToday + 1
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfTomorrow = cal.timeInMillis

        val todayBills = repository.getPendingInstallmentsInRange(startOfToday, endOfToday)
        val todayInvoices = repository.getPendingInvoicesInRange(startOfToday, endOfToday)

        val tomorrowBills = if (notifyTomorrow) {
            repository.getPendingInstallmentsInRange(startOfTomorrow, endOfTomorrow)
        } else {
            emptyList()
        }

        val tomorrowInvoices = if (notifyTomorrow) {
            repository.getPendingInvoicesInRange(startOfTomorrow, endOfTomorrow)
        } else {
            emptyList()
        }

        val overdueBills = if (notifyOverdue) {
            repository.getOverduePendingInstallments(startOfToday)
        } else {
            emptyList()
        }

        val overdueInvoices = if (notifyOverdue) {
            repository.getOverdueInvoices(startOfToday)
        } else {
            emptyList()
        }

        return NotificationItems(
            overdueBills = overdueBills,
            overdueInvoices = overdueInvoices,
            todayBills = todayBills,
            todayInvoices = todayInvoices,
            tomorrowBills = tomorrowBills,
            tomorrowInvoices = tomorrowInvoices
        )
    }
}
