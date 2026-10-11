package com.platform.app.domain.model

/**
 * Resumo agregado de indicadores financeiros para uma competência ou período mensal.
 */
data class MonthSummary(
    val totalCents: Long,
    val paidCents: Long,
    val pendingCents: Long,
    val overdueCents: Long,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0
) {
    val completionPercentage: Float
        get() = if (totalCents > 0) (paidCents.toFloat() / totalCents.toFloat()).coerceIn(0f, 1f) else 0f

    val totalCount: Int
        get() = paidCount + pendingCount + overdueCount

    companion object {
        val ZERO = MonthSummary(
            totalCents = 0L,
            paidCents = 0L,
            pendingCents = 0L,
            overdueCents = 0L,
            paidCount = 0,
            pendingCount = 0,
            overdueCount = 0
        )

        fun fromInstallments(
            installments: List<BillInstallment>,
            nowMillis: Long = System.currentTimeMillis()
        ): MonthSummary {
            var total = 0L
            var paid = 0L
            var pending = 0L
            var overdue = 0L
            var paidCount = 0
            var pendingCount = 0
            var overdueCount = 0

            for (inst in installments) {
                if (inst.isPaused || inst.isSkipped) continue
                total += inst.amountCents
                if (inst.isPaid) {
                    paid += inst.amountCents
                    paidCount++
                } else if (inst.dueDate < nowMillis) {
                    overdue += inst.amountCents
                    overdueCount++
                } else {
                    pending += inst.amountCents
                    pendingCount++
                }
            }

            return MonthSummary(
                totalCents = total,
                paidCents = paid,
                pendingCents = pending,
                overdueCents = overdue,
                paidCount = paidCount,
                pendingCount = pendingCount,
                overdueCount = overdueCount
            )
        }
    }
}

/**
 * Funções de extensão no domínio para somas seguras de parcelas,
 * garantindo tratamento correto de status PAUSED e SKIPPED e imutabilidade.
 */
fun Iterable<BillInstallment>.calculateTotalCents(): Long {
    var sum = 0L
    for (item in this) {
        if (!item.isPaused && !item.isSkipped) sum += item.amountCents
    }
    return sum
}

fun Iterable<BillInstallment>.calculatePaidCents(): Long {
    var sum = 0L
    for (item in this) {
        if (!item.isPaused && !item.isSkipped && item.isPaid) sum += item.amountCents
    }
    return sum
}

fun Iterable<BillInstallment>.calculateOpenCents(): Long {
    var sum = 0L
    for (item in this) {
        if (!item.isPaused && !item.isSkipped && !item.isPaid) sum += item.amountCents
    }
    return sum
}
