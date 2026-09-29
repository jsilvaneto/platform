package com.platform.app.domain.model

import java.util.Calendar

object CreditCardCalculator {

    /**
     * Calcula o limite disponível a partir do limite total e do limite consumido.
     * Invariante: O limite disponível nunca é negativo (mínimo 0 centavos).
     */
    fun calculateAvailableLimit(totalLimitCents: Long, usedLimitCents: Long): Long {
        return (totalLimitCents - usedLimitCents).coerceAtLeast(0L)
    }

    /**
     * Determina o status da fatura com base no timestamp atual, data de fechamento e quitação.
     * - Se já quitada -> PAGA
     * - Se a data atual alcançou ou ultrapassou a data de corte (fechamento) -> FECHADA
     * - Se a data atual é anterior à data de corte -> ABERTA
     */
    fun determineInvoiceStatus(
        currentTimestamp: Long,
        closingTimestamp: Long,
        isPaid: Boolean
    ): InvoiceStatus {
        if (isPaid) return InvoiceStatus.PAGA
        return if (currentTimestamp >= closingTimestamp) {
            InvoiceStatus.FECHADA
        } else {
            InvoiceStatus.ABERTA
        }
    }

    /**
     * Determina se uma transação realizada em [purchaseTimestamp] deve ser incluída
     * na fatura cujo corte ocorre em [closingTimestamp].
     * Transações realizadas até 23:59:59 do dia de corte entram na fatura atual.
     */
    fun shouldBelongToCurrentInvoice(
        purchaseTimestamp: Long,
        closingTimestamp: Long
    ): Boolean {
        return purchaseTimestamp <= closingTimestamp
    }

    /**
     * Gera o timestamp da data de fechamento para um dado mês/ano e dia de corte.
     */
    fun calculateClosingDate(year: Int, month: Int, closingDay: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, closingDay.coerceIn(1, maxDay))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    /**
     * Gera o timestamp da data de vencimento. Se o dia de vencimento for menor ou igual
     * ao dia de corte, o vencimento ocorre no mês seguinte.
     */
    fun calculateDueDate(year: Int, month: Int, closingDay: Int, dueDay: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        if (dueDay <= closingDay) {
            cal.add(Calendar.MONTH, 1)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, dueDay.coerceIn(1, maxDay))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
