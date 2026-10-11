package com.platform.app.domain.usecase

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso central para obtenção do saldo devedor em aberto por contato.
 *
 * Exclui parcelas pagas e ocorrências com status PAUSED diretamente na consulta SQL do SQLite.
 */
class GetOpenAmountByContactUseCase @Inject constructor(
    private val repository: FinancialRepository
) {

    operator fun invoke(): Flow<Map<String, Long>> {
        return repository.getOpenAmountByContact()
    }

    /**
     * Cálculo puro em memória para testes e cenários de fallback.
     */
    fun calculate(installments: List<BillInstallment>): Map<String, Long> {
        return installments
            .filter { !it.isPaid && !it.isPaused && !it.isSkipped && it.contactId != null }
            .groupBy { it.contactId!! }
            .mapValues { entry ->
                var sum = 0L
                for (item in entry.value) {
                    sum += item.amountCents
                }
                sum
            }
    }
}
