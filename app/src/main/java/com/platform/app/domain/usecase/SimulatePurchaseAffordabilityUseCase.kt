package com.platform.app.domain.usecase

import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class CardAffordabilitySimulation(
    val cardId: String,
    val cardName: String,
    val cardColorHex: String,
    val totalLimitCents: Long,
    val usedLimitCents: Long,
    val availableLimitCents: Long,
    val canAfford: Boolean,
    val daysUntilDue: Int,
    val nextClosingDate: Long,
    val nextDueDate: Long,
    val isRecommended: Boolean,
    val recommendationReason: String? = null
)

data class PurchaseAffordabilityResult(
    val purchaseAmountCents: Long,
    val options: List<CardAffordabilitySimulation>,
    val recommendedCardId: String?,
    val hasAffordableCard: Boolean
)

/**
 * Caso de uso do simulador multi-cartão "Posso comprar?" (Prompt 50).
 * Avalia capacidade de limite e recomenda o cartão ideal baseado no prazo de pagamento (float).
 */
class SimulatePurchaseAffordabilityUseCase @Inject constructor(
    private val getCreditCardSummariesUseCase: GetCreditCardSummariesUseCase
) {

    fun simulate(
        cardSummaries: List<CreditCardWithInvoiceSummary>,
        purchaseAmountCents: Long,
        currentTimestamp: Long = System.currentTimeMillis()
    ): PurchaseAffordabilityResult {
        if (cardSummaries.isEmpty()) {
            return PurchaseAffordabilityResult(
                purchaseAmountCents = purchaseAmountCents,
                options = emptyList(),
                recommendedCardId = null,
                hasAffordableCard = false
            )
        }

        val cal = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        val rawOptions = cardSummaries.map { summary ->
            val card = summary.card
            val available = CreditCardCalculator.calculateAvailableLimit(card.totalLimitCents, summary.usedLimitCents)
            val canAfford = available >= purchaseAmountCents

            // Se o dia atual for após o fechamento, a compra cai na fatura do mês seguinte (melhor dia de compra)
            val invoiceMonthOffset = if (currentDay > card.closingDay) 1 else 0

            val targetCal = Calendar.getInstance().apply {
                timeInMillis = currentTimestamp
                add(Calendar.MONTH, invoiceMonthOffset)
            }
            val targetYear = targetCal.get(Calendar.YEAR)
            val targetMonth = targetCal.get(Calendar.MONTH)

            val nextClosing = CreditCardCalculator.calculateClosingDate(targetYear, targetMonth, card.closingDay)
            val nextDue = CreditCardCalculator.calculateDueDate(targetYear, targetMonth, card.closingDay, card.dueDay)

            val diffMillis = (nextDue - currentTimestamp).coerceAtLeast(0L)
            val daysUntilDue = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()

            CardAffordabilitySimulation(
                cardId = card.id,
                cardName = card.name,
                cardColorHex = card.colorHex,
                totalLimitCents = card.totalLimitCents,
                usedLimitCents = summary.usedLimitCents,
                availableLimitCents = available,
                canAfford = canAfford,
                daysUntilDue = daysUntilDue,
                nextClosingDate = nextClosing,
                nextDueDate = nextDue,
                isRecommended = false,
                recommendationReason = null
            )
        }

        val affordableOptions = rawOptions.filter { it.canAfford }
        val bestOption = affordableOptions.maxWithOrNull(
            compareBy<CardAffordabilitySimulation> { it.daysUntilDue }
                .thenBy { it.availableLimitCents }
        )

        val finalizedOptions = rawOptions.map { opt ->
            if (bestOption != null && opt.cardId == bestOption.cardId) {
                opt.copy(
                    isRecommended = true,
                    recommendationReason = "Maior prazo de pagamento (${opt.daysUntilDue} dias) com limite disponível suficiente."
                )
            } else if (!opt.canAfford) {
                val missingCents = purchaseAmountCents - opt.availableLimitCents
                opt.copy(
                    recommendationReason = "Limite insuficiente (faltam R$ ${String.format(java.util.Locale.US, "%.2f", missingCents / 100.0)})."
                )
            } else {
                opt.copy(
                    recommendationReason = "Limite disponível, porém menor prazo de fatura (${opt.daysUntilDue} dias)."
                )
            }
        }

        return PurchaseAffordabilityResult(
            purchaseAmountCents = purchaseAmountCents,
            options = finalizedOptions,
            recommendedCardId = bestOption?.cardId,
            hasAffordableCard = bestOption != null
        )
    }
}
