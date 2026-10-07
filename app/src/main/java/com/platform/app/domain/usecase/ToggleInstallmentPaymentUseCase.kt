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
}
