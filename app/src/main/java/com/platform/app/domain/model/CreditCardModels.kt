package com.platform.app.domain.model

import java.util.UUID

data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val totalLimitCents: Long,
    val closingDay: Int,
    val dueDay: Int,
    val colorHex: String = "#3B82F6",
    val syncStatus: String = "PENDENTE"
)

enum class InvoiceStatus { ABERTA, FECHADA, PAGA }

data class CreditCardInvoice(
    val id: String = UUID.randomUUID().toString(),
    val creditCardId: String,
    val referenceMonth: String, // "YYYY-MM"
    val closingDate: Long,
    val dueDate: Long,
    val status: InvoiceStatus = InvoiceStatus.ABERTA,
    val totalAmountCents: Long = 0L,
    val syncStatus: String = "PENDENTE"
)

data class CreditCardWithInvoiceSummary(
    val card: CreditCard,
    val currentInvoice: CreditCardInvoice?,
    val usedLimitCents: Long = 0L
) {
    val availableLimitCents: Long get() = (card.totalLimitCents - usedLimitCents).coerceAtLeast(0L)
}
