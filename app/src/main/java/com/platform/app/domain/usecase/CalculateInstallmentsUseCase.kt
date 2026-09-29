package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
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
                for (i in 1..12) {
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
                            totalInstallments = 12,
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
}
