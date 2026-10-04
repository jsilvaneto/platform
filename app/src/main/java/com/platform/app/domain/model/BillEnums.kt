package com.platform.app.domain.model

enum class BillType(val label: String) {
    SINGLE("Avulsa"),
    INSTALLMENT("Parcelada"),
    RECURRING("Recorrente")
}

enum class BillStatus(val label: String) {
    PENDING("A Pagar"),
    PAID("Paga"),
    OVERDUE("Vencida"),
    PAUSED("Pausada")
}

enum class RecurrenceFrequency(val label: String) {
    DAILY("Diariamente"),
    WEEKLY("Semanalmente"),
    MONTHLY("Mensalmente"),
    YEARLY("Anualmente")
}

enum class RecurrenceEndType(val label: String) {
    FOREVER("Para sempre"),
    UNTIL_DATE("Até uma data"),
    BY_OCCURRENCES("Por número de eventos")
}
