package com.platform.app.domain.usecase

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.MonthSummary
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso central para recuperação e cálculo agregado do resumo financeiro mensal.
 *
 * Fornece tanto consulta reativa baseada em SQL agregada no Room quanto função de cálculo puro
 * sobre listas em memória (para filtros locais e testes unitários), garantindo paridade numérica estrita.
 */
class GetMonthSummaryUseCase @Inject constructor(
    private val repository: FinancialRepository
) {

    operator fun invoke(
        startMillis: Long,
        endMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Flow<MonthSummary> {
        return repository.getMonthSummary(startMillis, endMillis, nowMillis)
    }

    /**
     * Cálculo puro em memória para listas filtradas na camada de apresentação.
     */
    fun calculate(
        installments: List<BillInstallment>,
        nowMillis: Long = System.currentTimeMillis()
    ): MonthSummary {
        return MonthSummary.fromInstallments(installments, nowMillis)
    }
}
