package com.platform.app.domain.model

data class BillInstallment(
    val id: String,
    val billId: String,
    val billTitle: String,
    val categoryId: String?,
    val categoryName: String = "Geral",
    val categoryColorHex: String = "#64748B",
    val installmentNumber: Int,
    val totalInstallments: Int,
    val amountCents: Long,
    val dueDate: Long,
    val paidAt: Long? = null,
    val status: BillStatus = BillStatus.PENDING,
    val type: BillType = BillType.SINGLE
) {
    val isPaid: Boolean get() = paidAt != null || status == BillStatus.PAID
}
