package com.platform.app.domain.usecase

import com.platform.app.domain.repository.FinancialRepository
import javax.inject.Inject

class ToggleInstallmentPaymentUseCase @Inject constructor(
    private val repository: FinancialRepository
) {
    suspend operator fun invoke(installmentId: String, currentPaidStatus: Boolean) {
        val newPaidStatus = !currentPaidStatus
        val paidTimestamp = if (newPaidStatus) System.currentTimeMillis() else null
        repository.toggleInstallmentPayment(installmentId, newPaidStatus, paidTimestamp)
    }
}
