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

    /**
     * Determina o mês de referência ("YYYY-MM") da fatura para uma compra realizada em [purchaseTimestamp].
     * Se o dia da compra for maior que o [closingDay], a compra entrará na fatura do mês subsequente.
     */
    fun determineInvoiceReferenceMonth(purchaseTimestamp: Long, closingDay: Int): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = purchaseTimestamp
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        if (dayOfMonth > closingDay) {
            cal.add(Calendar.MONTH, 1)
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(java.util.Locale.US, "%04d-%02d", year, month)
    }

    /**
     * Avança ou recua [monthsToAdd] meses a partir de uma competência "YYYY-MM".
     */
    fun addMonthsToReferenceMonth(referenceMonth: String, monthsToAdd: Int): String {
        val parts = referenceMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.add(Calendar.MONTH, monthsToAdd)
        val resYear = cal.get(Calendar.YEAR)
        val resMonth = cal.get(Calendar.MONTH) + 1
        return String.format(java.util.Locale.US, "%04d-%02d", resYear, resMonth)
    }

    /**
     * Valida integralmente os atributos de um cartão de crédito.
     */
    fun validateCard(
        name: String,
        totalLimitCents: Long,
        closingDay: Int,
        dueDay: Int
    ): CardValidationResult {
        val nameTrim = name.trim()
        val nameError = when {
            nameTrim.isBlank() -> "O nome do cartão é obrigatório"
            nameTrim.length < 2 -> "O nome deve ter no mínimo 2 caracteres"
            else -> null
        }
        val limitError = if (totalLimitCents <= 0L) {
            "O limite total deve ser maior que R$ 0,00"
        } else null

        val closingError = if (closingDay !in 1..31) {
            "Dia de corte inválido (deve ser entre 1 e 31)"
        } else null

        val dueError = if (dueDay !in 1..31) {
            "Dia de vencimento inválido (deve ser entre 1 e 31)"
        } else null

        return CardValidationResult(
            isValid = nameError == null && limitError == null && closingError == null && dueError == null,
            nameError = nameError,
            limitError = limitError,
            closingDayError = closingError,
            dueDayError = dueError
        )
    }
}

data class CardValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val limitError: String? = null,
    val closingDayError: String? = null,
    val dueDayError: String? = null
)
