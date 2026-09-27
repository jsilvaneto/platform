package com.platform.app.domain.model

enum class BillType(val label: String) {
    SINGLE("Avulsa"),
    INSTALLMENT("Parcelada"),
    RECURRING("Recorrente")
}

enum class BillStatus(val label: String) {
    PENDING("A Pagar"),
    PAID("Paga"),
    OVERDUE("Vencida")
}
