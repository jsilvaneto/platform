package com.platform.app.domain.usecase

import com.platform.app.domain.model.CardUsage
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Caso de uso central para cálculo do consumo e limites de cartões de crédito.
 *
 * Transforma resumos calculados com as regras estritas de faturas em modelos [CardUsage].
 */
class GetCardUsageUseCase @Inject constructor(
    private val getCreditCardSummariesUseCase: GetCreditCardSummariesUseCase
) {

    operator fun invoke(): Flow<List<CardUsage>> {
        return getCreditCardSummariesUseCase().map { list ->
            list.map { CardUsage.fromSummary(it) }
        }
    }

    operator fun invoke(cardId: String): Flow<CardUsage?> {
        return getCreditCardSummariesUseCase().map { list ->
            list.firstOrNull { it.card.id == cardId }?.let { CardUsage.fromSummary(it) }
        }
    }

    fun calculate(summaries: List<CreditCardWithInvoiceSummary>): List<CardUsage> {
        return summaries.map { CardUsage.fromSummary(it) }
    }
}
