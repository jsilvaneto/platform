package com.platform.app.domain.usecase

import com.platform.app.domain.repository.FinancialRepository
import javax.inject.Inject

class ToggleInstallmentPaymentUseCase @Inject constructor(
    private val repository: FinancialRepository
) {
    suspend operator fun invoke(
        installmentId: String,
        currentPaidStatus: Boolean,
        actualPaymentDate: Long? = null
    ) {
        val newPaidStatus = !currentPaidStatus
        val paidTimestamp = if (newPaidStatus) System.currentTimeMillis() else null
        val effectiveActualDate = if (newPaidStatus) (actualPaymentDate ?: paidTimestamp) else null
        repository.toggleInstallmentPayment(
            installmentId = installmentId,
            isPaid = newPaidStatus,
            paidTimestamp = paidTimestamp,
            actualPaymentDate = effectiveActualDate
        )
    }

    suspend fun recordPayment(
        installmentId: String,
        amountCents: Long,
        paymentDate: Long,
        paymentMethodId: String? = null,
        financialAccountId: String? = null,
        interestCents: Long = 0L,
        fineCents: Long = 0L,
        discountCents: Long = 0L,
        notes: String? = null
    ) {
        repository.recordInstallmentPayment(
            installmentId = installmentId,
            amountCents = amountCents,
            paymentDate = paymentDate,
            paymentMethodId = paymentMethodId,
            financialAccountId = financialAccountId,
            interestCents = interestCents,
            fineCents = fineCents,
            discountCents = discountCents,
            notes = notes
        )
    }
}
