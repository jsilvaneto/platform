package com.platform.app.domain.model

/**
 * Modelo de domínio para pagamentos e liquidações (Prompt 49).
 * Permite modelagem append-only de pagamentos parciais, juros, multas e descontos,
 * desacoplando a quitação do valor estático da parcela.
 */
data class Payment(
    val id: String,
    val billInstallmentId: String,
    val amountCents: Long,
    val paymentDate: Long,
    val paymentMethodId: String? = null,
    val financialAccountId: String? = null,
    val interestCents: Long = 0L,
    val fineCents: Long = 0L,
    val discountCents: Long = 0L,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    /**
     * Valor líquido efetivamente desembolsado no pagamento, incluindo juros e multas e deduzindo descontos.
     */
    val totalPaidWithChargesCents: Long
        get() = amountCents + interestCents + fineCents - discountCents
}
