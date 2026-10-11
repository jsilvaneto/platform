package com.platform.app.domain.model

import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val totalLimitCents: Long,
    val closingDay: Int,
    val dueDay: Int,
    val colorHex: String = "#3B82F6",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

enum class InvoiceStatus { ABERTA, FECHADA, PAGA }

data class CreditCardInvoice(
    val id: String = UUID.randomUUID().toString(),
    val creditCardId: String,
    val referenceMonth: String, // "YYYY-MM"
    val closingDate: LocalDate,
    val dueDate: LocalDate,
    val status: InvoiceStatus = InvoiceStatus.ABERTA,
    val totalAmountCents: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    val closingDateMillis: Long get() = closingDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val dueDateMillis: Long get() = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    constructor(
        id: String = UUID.randomUUID().toString(),
        creditCardId: String,
        referenceMonth: String,
        closingDate: Long = 0L,
        dueDate: Long = 0L,
        status: InvoiceStatus = InvoiceStatus.ABERTA,
        totalAmountCents: Long = 0L,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis(),
        deletedAt: Long? = null,
        closingDateMillis: Long = closingDate,
        dueDateMillis: Long = dueDate
    ) : this(
        id = id,
        creditCardId = creditCardId,
        referenceMonth = referenceMonth,
        closingDate = resolveLocalDate(if (closingDate != 0L) closingDate else closingDateMillis),
        dueDate = resolveLocalDate(if (dueDate != 0L) dueDate else dueDateMillis),
        status = status,
        totalAmountCents = totalAmountCents,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

data class CreditCardWithInvoiceSummary(
    val card: CreditCard,
    val currentInvoice: CreditCardInvoice?,
    val usedLimitCents: Long = 0L
) {
    val availableLimitCents: Long get() = (card.totalLimitCents - usedLimitCents).coerceAtLeast(0L)
}
