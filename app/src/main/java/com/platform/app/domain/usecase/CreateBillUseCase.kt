package com.platform.app.domain.usecase

import com.platform.app.domain.model.Bill
import com.platform.app.domain.repository.FinancialRepository
import javax.inject.Inject

class CreateBillUseCase @Inject constructor(
    private val repository: FinancialRepository,
    private val calculateInstallmentsUseCase: CalculateInstallmentsUseCase
) {
    suspend operator fun invoke(bill: Bill, firstDueDate: Long) {
        val installments = calculateInstallmentsUseCase(bill, firstDueDate)
        repository.saveBillWithInstallments(bill, installments)
    }
}
