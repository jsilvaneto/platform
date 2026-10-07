package com.platform.app.domain.usecase

import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.repository.FinancialRepository
import javax.inject.Inject

class CreateBillUseCase @Inject constructor(
    private val repository: FinancialRepository,
    private val calculateInstallmentsUseCase: CalculateInstallmentsUseCase
) {
    suspend operator fun invoke(
        bill: Bill,
        firstDueDate: Long,
        creditCard: CreditCard? = null,
        isFirstInstallmentPaid: Boolean = false,
        actualPaymentDate: Long? = null
    ): List<BillInstallment> {
        val baseInstallments = calculateInstallmentsUseCase(bill, firstDueDate)

        val finalInstallments = baseInstallments.mapIndexed { index, installment ->
            val isPaid = isFirstInstallmentPaid && (bill.type == BillType.SINGLE || index == 0)
            val paymentTimestamp = if (isPaid) (actualPaymentDate ?: System.currentTimeMillis()) else null
            val status = if (isPaid) BillStatus.PAID else BillStatus.PENDING

            if (creditCard != null) {
                // Para RECURRING, apenas a ocorrência do ciclo inicial é atrelada imediatamente à fatura.
                // As ocorrências futuras mantêm a projeção temporal sem pré-criar faturas vazias antecipadas no banco.
                if (bill.type == BillType.RECURRING && index > 0) {
                    installment.copy(
                        invoiceId = null,
                        status = status,
                        paidAt = paymentTimestamp,
                        actualPaymentDate = paymentTimestamp
                    )
                } else {
                    val refMonth = CreditCardCalculator.determineInvoiceReferenceMonth(
                        purchaseTimestamp = installment.dueDate,
                        closingDay = creditCard.closingDay
                    )
                    val invoice = repository.getOrCreateInvoiceForMonth(creditCard.id, refMonth)

                    installment.copy(
                        invoiceId = invoice.id,
                        dueDate = invoice.dueDate,
                        status = status,
                        paidAt = paymentTimestamp,
                        actualPaymentDate = paymentTimestamp
                    )
                }
            } else {
                installment.copy(
                    status = status,
                    paidAt = paymentTimestamp,
                    actualPaymentDate = paymentTimestamp
                )
            }
        }

        val updatedBill = bill.copy(
            invoiceId = if (creditCard != null) finalInstallments.firstOrNull()?.invoiceId else bill.invoiceId,
            totalInstallments = if (bill.type == BillType.RECURRING && finalInstallments.isNotEmpty()) {
                finalInstallments.size
            } else {
                bill.totalInstallments
            }
        )

        repository.saveBillWithInstallments(updatedBill, finalInstallments)
        return finalInstallments
    }
}
