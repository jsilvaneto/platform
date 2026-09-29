package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

data class MonthlyForecastResult(
    val monthMillis: Long,
    val monthLabel: String,
    val totalForecastCents: Long, // Sum of pending single bills + open/closed invoices in month
    val totalPaidCents: Long,     // Sum of paid bills + paid invoices in month
    val overdueItems: List<PayableItem> = emptyList(),
    val dueTodayItems: List<PayableItem> = emptyList(),
    val next7DaysItems: List<PayableItem> = emptyList(),
    val laterInMonthItems: List<PayableItem> = emptyList(),
    val paidItems: List<PayableItem> = emptyList(),
    val overdueTotalCents: Long = 0L,
    val dueTodayTotalCents: Long = 0L,
    val next7DaysTotalCents: Long = 0L,
    val paidTotalCents: Long = 0L
) {
    val overdueCount: Int get() = overdueItems.size
    val dueTodayCount: Int get() = dueTodayItems.size
    val next7DaysCount: Int get() = next7DaysItems.size
    val paidCount: Int get() = paidItems.size
    val totalItemsCount: Int get() = overdueCount + dueTodayCount + next7DaysCount + laterInMonthItems.size + paidCount
}

class CalculateMonthlyForecastUseCase @Inject constructor(
    private val repository: FinancialRepository
) {
    operator fun invoke(monthMillis: Long, nowMillis: Long = System.currentTimeMillis()): Flow<MonthlyForecastResult> {
        val startOfMonth = DateUtils.getStartOfMonth(monthMillis)
        val endOfMonth = DateUtils.getEndOfMonth(monthMillis)

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
        val sevenDaysAhead = endOfToday + (7L * 24 * 3600 * 1000)

        return combine(
            repository.getInstallmentsForPeriod(startOfMonth, endOfMonth),
            repository.getCreditCards(),
            repository.getInvoicesForPeriod(startOfMonth, endOfMonth)
        ) { installments, cards, invoices ->
            val cardMap = cards.associateBy { it.id }

            // Contas Avulsas (sem vínculo com cartão/fatura) com vencimento no mês
            val standaloneInstallments = installments.filter { it.invoiceId == null }

            var pendingBillsSum = 0L
            var paidBillsSum = 0L

            val overdueList = mutableListOf<PayableItem>()
            val dueTodayList = mutableListOf<PayableItem>()
            val next7DaysList = mutableListOf<PayableItem>()
            val laterList = mutableListOf<PayableItem>()
            val paidList = mutableListOf<PayableItem>()

            for (inst in standaloneInstallments) {
                if (inst.isPaid) {
                    paidBillsSum += inst.amountCents
                    paidList.add(PayableItem.BillPayable(installment = inst, urgency = PayableUrgency.PAID))
                } else {
                    pendingBillsSum += inst.amountCents
                    val urgency = when {
                        inst.dueDate < startOfToday -> PayableUrgency.OVERDUE
                        inst.dueDate in startOfToday..endOfToday -> PayableUrgency.DUE_TODAY
                        inst.dueDate in (endOfToday + 1)..sevenDaysAhead -> PayableUrgency.NEXT_7_DAYS
                        else -> PayableUrgency.LATER
                    }

                    val payable = PayableItem.BillPayable(installment = inst, urgency = urgency)
                    when (urgency) {
                        PayableUrgency.OVERDUE -> overdueList.add(payable)
                        PayableUrgency.DUE_TODAY -> dueTodayList.add(payable)
                        PayableUrgency.NEXT_7_DAYS -> next7DaysList.add(payable)
                        PayableUrgency.LATER -> laterList.add(payable)
                        PayableUrgency.PAID -> paidList.add(payable)
                    }
                }
            }

            // Faturas de Cartões que vencem no mês
            var pendingInvoicesSum = 0L
            var paidInvoicesSum = 0L

            for (inv in invoices) {
                val card = cardMap[inv.creditCardId]
                val cardName = card?.name ?: "Cartão"
                val cardColor = card?.colorHex ?: "#3B82F6"

                if (inv.status == InvoiceStatus.PAGA) {
                    paidInvoicesSum += inv.totalAmountCents
                    paidList.add(PayableItem.InvoicePayable(invoice = inv, cardName = cardName, cardColorHex = cardColor, urgency = PayableUrgency.PAID))
                } else {
                    pendingInvoicesSum += inv.totalAmountCents
                    val urgency = when {
                        inv.dueDate < startOfToday -> PayableUrgency.OVERDUE
                        inv.dueDate in startOfToday..endOfToday -> PayableUrgency.DUE_TODAY
                        inv.dueDate in (endOfToday + 1)..sevenDaysAhead -> PayableUrgency.NEXT_7_DAYS
                        else -> PayableUrgency.LATER
                    }

                    val payable = PayableItem.InvoicePayable(invoice = inv, cardName = cardName, cardColorHex = cardColor, urgency = urgency)
                    when (urgency) {
                        PayableUrgency.OVERDUE -> overdueList.add(payable)
                        PayableUrgency.DUE_TODAY -> dueTodayList.add(payable)
                        PayableUrgency.NEXT_7_DAYS -> next7DaysList.add(payable)
                        PayableUrgency.LATER -> laterList.add(payable)
                        PayableUrgency.PAID -> paidList.add(payable)
                    }
                }
            }

            // Total Previsto = sum(Contas Avulsas Pendentes no Mês) + sum(Faturas Abertas/Fechadas que vencem no Mês)
            val totalForecast = pendingBillsSum + pendingInvoicesSum
            val totalPaid = paidBillsSum + paidInvoicesSum

            MonthlyForecastResult(
                monthMillis = monthMillis,
                monthLabel = DateUtils.formatMonthYear(monthMillis),
                totalForecastCents = totalForecast,
                totalPaidCents = totalPaid,
                overdueItems = overdueList.sortedBy { it.dueDate },
                dueTodayItems = dueTodayList.sortedBy { it.dueDate },
                next7DaysItems = next7DaysList.sortedBy { it.dueDate },
                laterInMonthItems = laterList.sortedBy { it.dueDate },
                paidItems = paidList.sortedByDescending { it.dueDate },
                overdueTotalCents = overdueList.sumOf { it.amountCents },
                dueTodayTotalCents = dueTodayList.sumOf { it.amountCents },
                next7DaysTotalCents = next7DaysList.sumOf { it.amountCents },
                paidTotalCents = totalPaid
            )
        }
    }

    /**
     * Função pura de cálculo do Total Previsto no Mês:
     * Total Previsto = sum(Contas Avulsas Pendentes no Mês) + sum(Faturas Abertas/Fechadas que vencem no Mês)
     */
    fun calculateTotalForecast(
        pendingBillsCents: Long,
        openOrClosedInvoicesCents: Long
    ): Long {
        return pendingBillsCents + openOrClosedInvoicesCents
    }

    /**
     * Função pura que agrega contas e faturas para um determinado mês.
     */
    fun aggregateMonthForecast(
        pendingStandaloneBills: List<Long>,
        openOrClosedInvoices: List<Long>
    ): Long {
        return pendingStandaloneBills.sum() + openOrClosedInvoices.sum()
    }
}
