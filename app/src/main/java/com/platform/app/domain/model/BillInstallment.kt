package com.platform.app.domain.model

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
    val dueDate: Long,
    val paidAt: Long? = null,
    val status: BillStatus = BillStatus.PENDING,
    val type: BillType = BillType.SINGLE
) {
    val isPaid: Boolean get() = paidAt != null || status == BillStatus.PAID
}
