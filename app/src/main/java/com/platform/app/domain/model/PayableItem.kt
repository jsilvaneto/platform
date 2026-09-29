package com.platform.app.domain.model

enum class PayableUrgency {
    OVERDUE,     // 🔴 Atrasadas: Vencimento < Hoje e status == PENDENTE
    DUE_TODAY,   // 🟡 Vence Hoje: Vencimento == Hoje e status == PENDENTE
    NEXT_7_DAYS, // ⚪ Próximos 7 Dias: Vencimento nos próximos 7 dias
    LATER,       // Mais adiante no mês
    PAID         // 🟢 Pagas no Mês: status == PAGO
}

sealed class PayableItem {
    abstract val id: String
    abstract val title: String
    abstract val amountCents: Long
    abstract val dueDate: Long
    abstract val isPaid: Boolean
    abstract val urgency: PayableUrgency
    abstract val categoryName: String
    abstract val categoryColorHex: String
    abstract val nature: ExpenseNature

    data class BillPayable(
        val installment: BillInstallment,
        override val urgency: PayableUrgency
    ) : PayableItem() {
        override val id: String get() = installment.id
        override val title: String get() = installment.billTitle
        override val amountCents: Long get() = installment.amountCents
        override val dueDate: Long get() = installment.dueDate
        override val isPaid: Boolean get() = installment.isPaid
        override val categoryName: String get() = installment.categoryName
        override val categoryColorHex: String get() = installment.categoryColorHex
        override val nature: ExpenseNature get() = installment.nature
    }

    data class InvoicePayable(
        val invoice: CreditCardInvoice,
        val cardName: String,
        val cardColorHex: String,
        override val urgency: PayableUrgency
    ) : PayableItem() {
        override val id: String get() = invoice.id
        override val title: String get() = "Fatura $cardName • ${invoice.referenceMonth}"
        override val amountCents: Long get() = invoice.totalAmountCents
        override val dueDate: Long get() = invoice.dueDate
        override val isPaid: Boolean get() = invoice.status == InvoiceStatus.PAGA
        override val categoryName: String get() = "Cartão de Crédito"
        override val categoryColorHex: String get() = cardColorHex
        override val nature: ExpenseNature get() = ExpenseNature.NECESSARIO
    }
}
