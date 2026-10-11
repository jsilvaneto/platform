package com.platform.app.domain.model

import java.time.LocalDate
import java.time.ZoneId

data class BillInstallment(
    val id: String,
    val billId: String,
    val billTitle: String,
    val categoryId: String? = null,
    val categoryName: String = "Geral",
    val categoryColorHex: String = "#64748B",
    val categoryIconName: String = "category",
    val nature: ExpenseNature = ExpenseNature.NECESSARIO,
    val itemId: String? = null,
    val itemName: String? = null,
    val invoiceId: String? = null,
    val contactId: String? = null,
    val contactName: String? = null,
    val financialAccountId: String? = null,
    val financialAccountName: String? = null,
    val paymentMethodId: String? = null,
    val paymentMethodName: String? = null,
    val installmentNumber: Int = 1,
    val totalInstallments: Int = 1,
    val amountCents: Long,
    val dueDate: LocalDate,
    val paidAt: Long? = null,
    val actualPaymentDate: Long? = null,
    val status: BillStatus = BillStatus.PENDING,
    val type: BillType = BillType.SINGLE,
    val createdAt: Long = System.currentTimeMillis(),
    val creditCardId: String? = null,
    val deletedAt: Long? = null,
    val updatedAt: Long = createdAt
) {
    val dueDateMillis: Long get() = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val isPaid: Boolean get() = paidAt != null || status == BillStatus.PAID
    val isPaused: Boolean get() = status == BillStatus.PAUSED
    val isSkipped: Boolean get() = status == BillStatus.SKIPPED

    constructor(
        id: String,
        billId: String,
        billTitle: String,
        categoryId: String? = null,
        categoryName: String = "Geral",
        categoryColorHex: String = "#64748B",
        categoryIconName: String = "category",
        nature: ExpenseNature = ExpenseNature.NECESSARIO,
        itemId: String? = null,
        itemName: String? = null,
        invoiceId: String? = null,
        contactId: String? = null,
        contactName: String? = null,
        financialAccountId: String? = null,
        financialAccountName: String? = null,
        paymentMethodId: String? = null,
        paymentMethodName: String? = null,
        installmentNumber: Int = 1,
        totalInstallments: Int = 1,
        amountCents: Long,
        dueDate: Long = 0L,
        paidAt: Long? = null,
        actualPaymentDate: Long? = null,
        status: BillStatus = BillStatus.PENDING,
        type: BillType = BillType.SINGLE,
        createdAt: Long = if (dueDate != 0L) dueDate else System.currentTimeMillis(),
        creditCardId: String? = null,
        deletedAt: Long? = null,
        updatedAt: Long = createdAt,
        dueDateMillis: Long = dueDate
    ) : this(
        id = id,
        billId = billId,
        billTitle = billTitle,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryColorHex = categoryColorHex,
        categoryIconName = categoryIconName,
        nature = nature,
        itemId = itemId,
        itemName = itemName,
        invoiceId = invoiceId,
        contactId = contactId,
        contactName = contactName,
        financialAccountId = financialAccountId,
        financialAccountName = financialAccountName,
        paymentMethodId = paymentMethodId,
        paymentMethodName = paymentMethodName,
        installmentNumber = installmentNumber,
        totalInstallments = totalInstallments,
        amountCents = amountCents,
        dueDate = resolveLocalDate(if (dueDate != 0L) dueDate else dueDateMillis),
        paidAt = paidAt,
        actualPaymentDate = actualPaymentDate,
        status = status,
        type = type,
        createdAt = createdAt,
        creditCardId = creditCardId,
        deletedAt = deletedAt,
        updatedAt = updatedAt
    )
}

internal fun resolveLocalDate(epoch: Long): LocalDate {
    return if (epoch > 1000000) com.platform.app.core.util.DateUtils.millisToLocalDate(epoch) else LocalDate.ofEpochDay(epoch)
}

