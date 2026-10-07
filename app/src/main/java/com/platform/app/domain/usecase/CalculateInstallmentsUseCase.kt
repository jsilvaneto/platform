package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import java.util.UUID
import javax.inject.Inject

class CalculateInstallmentsUseCase @Inject constructor() {

    operator fun invoke(bill: Bill, firstDueDate: Long): List<BillInstallment> {
        val installments = mutableListOf<BillInstallment>()

        when (bill.type) {
            BillType.SINGLE -> {
                installments.add(
                    BillInstallment(
                        id = UUID.randomUUID().toString(),
                        billId = bill.id,
                        billTitle = bill.title,
                        categoryId = bill.categoryId,
                        itemId = bill.itemId,
                        invoiceId = bill.invoiceId,
                        contactId = bill.contactId,
                        financialAccountId = bill.financialAccountId,
                        paymentMethodId = bill.paymentMethodId,
                        installmentNumber = 1,
                        totalInstallments = 1,
                        amountCents = bill.totalAmountCents,
                        dueDate = firstDueDate,
                        status = BillStatus.PENDING,
                        type = BillType.SINGLE
                    )
                )
            }

            BillType.INSTALLMENT -> {
                val totalInstallments = bill.totalInstallments.coerceAtLeast(1)
                val baseAmount = bill.totalAmountCents / totalInstallments
                val remainder = bill.totalAmountCents % totalInstallments

                for (i in 1..totalInstallments) {
                    val installmentAmount = if (i == 1) baseAmount + remainder else baseAmount
                    val dueDate = DateUtils.addMonths(firstDueDate, i - 1)

                    installments.add(
                        BillInstallment(
                            id = UUID.randomUUID().toString(),
                            billId = bill.id,
                            billTitle = bill.title,
                            categoryId = bill.categoryId,
                            itemId = bill.itemId,
                            invoiceId = bill.invoiceId,
                            contactId = bill.contactId,
                            financialAccountId = bill.financialAccountId,
                            paymentMethodId = bill.paymentMethodId,
                            installmentNumber = i,
                            totalInstallments = totalInstallments,
                            amountCents = installmentAmount,
                            dueDate = dueDate,
                            status = BillStatus.PENDING,
                            type = BillType.INSTALLMENT
                        )
                    )
                }
            }

            BillType.RECURRING -> {
                val frequency = bill.recurrenceFrequency ?: RecurrenceFrequency.MONTHLY
                val endType = bill.recurrenceEndType ?: RecurrenceEndType.FOREVER
                val targetDueDates = calculateRecurrenceDueDates(
                    firstDueDate = firstDueDate,
                    frequency = frequency,
                    endType = endType,
                    endDate = bill.recurrenceEndDate,
                    occurrencesCount = bill.totalInstallments
                )
                val total = targetDueDates.size

                targetDueDates.forEachIndexed { index, dueDate ->
                    installments.add(
                        BillInstallment(
                            id = UUID.randomUUID().toString(),
                            billId = bill.id,
                            billTitle = bill.title,
                            categoryId = bill.categoryId,
                            itemId = bill.itemId,
                            invoiceId = bill.invoiceId,
                            contactId = bill.contactId,
                            financialAccountId = bill.financialAccountId,
                            paymentMethodId = bill.paymentMethodId,
                            installmentNumber = index + 1,
                            totalInstallments = total,
                            amountCents = bill.totalAmountCents,
                            dueDate = dueDate,
                            status = BillStatus.PENDING,
                            type = BillType.RECURRING
                        )
                    )
                }
            }
        }

        return installments
    }

    fun generateNextRecurringInstallments(
        bill: Bill,
        existingInstallments: List<BillInstallment>,
        occurrencesToAdd: Int? = null
    ): List<BillInstallment> {
        if (bill.type != BillType.RECURRING || existingInstallments.isEmpty()) {
            return emptyList()
        }

        val frequency = bill.recurrenceFrequency ?: RecurrenceFrequency.MONTHLY
        val count = occurrencesToAdd ?: getDefaultWindowOccurrences(frequency)

        val sorted = existingInstallments.sortedBy { it.installmentNumber }
        val firstInstallment = sorted.first()
        val lastInstallment = sorted.last()
        val startNumber = lastInstallment.installmentNumber + 1
        val newTotal = lastInstallment.installmentNumber + count

        val installments = mutableListOf<BillInstallment>()
        for (i in 0 until count) {
            val installmentNumber = startNumber + i
            val stepFromFirst = installmentNumber - firstInstallment.installmentNumber
            val dueDate = DateUtils.addRecurrenceStep(firstInstallment.dueDate, frequency, stepFromFirst)

            installments.add(
                BillInstallment(
                    id = UUID.randomUUID().toString(),
                    billId = bill.id,
                    billTitle = bill.title,
                    categoryId = bill.categoryId,
                    itemId = bill.itemId,
                    invoiceId = bill.invoiceId,
                    contactId = bill.contactId,
                    financialAccountId = bill.financialAccountId,
                    paymentMethodId = bill.paymentMethodId,
                    installmentNumber = installmentNumber,
                    totalInstallments = newTotal,
                    amountCents = bill.totalAmountCents,
                    dueDate = dueDate,
                    status = if (bill.isPaused) BillStatus.PAUSED else BillStatus.PENDING,
                    type = BillType.RECURRING
                )
            )
        }

        return installments
    }

    companion object {
        fun getDefaultWindowOccurrences(frequency: RecurrenceFrequency): Int {
            return when (frequency) {
                RecurrenceFrequency.DAILY -> 30
                RecurrenceFrequency.WEEKLY -> 26
                RecurrenceFrequency.MONTHLY -> 12
                RecurrenceFrequency.YEARLY -> 5
            }
        }

        fun calculateRecurrenceDueDates(
            firstDueDate: Long,
            frequency: RecurrenceFrequency,
            endType: RecurrenceEndType,
            endDate: Long?,
            occurrencesCount: Int
        ): List<Long> {
            val dueDates = mutableListOf<Long>()
            when (endType) {
                RecurrenceEndType.FOREVER -> {
                    val count = getDefaultWindowOccurrences(frequency)
                    for (i in 0 until count) {
                        dueDates.add(DateUtils.addRecurrenceStep(firstDueDate, frequency, i))
                    }
                }
                RecurrenceEndType.BY_OCCURRENCES -> {
                    val count = occurrencesCount.coerceIn(1, 365)
                    for (i in 0 until count) {
                        dueDates.add(DateUtils.addRecurrenceStep(firstDueDate, frequency, i))
                    }
                }
                RecurrenceEndType.UNTIL_DATE -> {
                    val limitDate = endDate ?: DateUtils.addMonths(firstDueDate, 12)
                    val endOfDayLimit = DateUtils.getEndOfDay(limitDate)
                    var index = 0
                    val maxOccurrences = 365
                    while (index < maxOccurrences) {
                        val nextDate = DateUtils.addRecurrenceStep(firstDueDate, frequency, index)
                        if (nextDate <= endOfDayLimit || index == 0) {
                            dueDates.add(nextDate)
                            index++
                        } else {
                            break
                        }
                    }
                }
            }
            return dueDates
        }
    }
}
