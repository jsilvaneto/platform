package com.platform.app.domain.model

/**
 * Representação de alto nível do uso de limite e fatura de um cartão de crédito.
 */
data class CardUsage(
    val card: CreditCard,
    val totalLimitCents: Long,
    val usedLimitCents: Long,
    val availableLimitCents: Long,
    val usagePercentage: Float,
    val currentInvoice: CreditCardInvoice?,
    val currentInvoiceAmountCents: Long,
    val isLimitExceeded: Boolean
) {
    companion object {
        fun fromSummary(summary: CreditCardWithInvoiceSummary): CardUsage {
            val totalLimit = summary.card.totalLimitCents
            val usedLimit = summary.usedLimitCents
            val availableLimit = (totalLimit - usedLimit).coerceAtLeast(0L)
            val usagePercentage = if (totalLimit > 0) {
                (usedLimit.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
            } else 0f
            val invoiceAmount = summary.currentInvoice?.totalAmountCents ?: 0L

            return CardUsage(
                card = summary.card,
                totalLimitCents = totalLimit,
                usedLimitCents = usedLimit,
                availableLimitCents = availableLimit,
                usagePercentage = usagePercentage,
                currentInvoice = summary.currentInvoice,
                currentInvoiceAmountCents = invoiceAmount,
                isLimitExceeded = usedLimit > totalLimit
            )
        }
    }
}
