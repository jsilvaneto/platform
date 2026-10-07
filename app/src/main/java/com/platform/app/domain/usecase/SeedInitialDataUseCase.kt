package com.platform.app.domain.usecase

import com.platform.app.domain.repository.FinancialRepository
import javax.inject.Inject

class SeedInitialDataUseCase @Inject constructor(
    private val financialRepository: FinancialRepository
) {
    suspend operator fun invoke() {
        financialRepository.seedInitialData()
    }
}
