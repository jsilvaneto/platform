package com.platform.app.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    private val ptBrLocale = Locale("pt", "BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(ptBrLocale)

    /**
     * Converte um valor em centavos inteiros (ex: 15050L) para moeda formatada (ex: "R$ 150,50").
     * Previne qualquer erro de arredondamento de ponto flutuante.
     */
    fun formatCentsToCurrency(amountCents: Long): String {
        val amountDouble = amountCents / 100.0
        return currencyFormat.format(amountDouble)
    }

    /**
     * Converte uma string digitada pelo usuário (ex: "150,50" ou "R$ 150,50") em centavos inteiros (15050L).
     */
    fun parseInputToCents(input: String): Long {
        val digitsOnly = input.replace(Regex("[^0-9]"), "")
        if (digitsOnly.isBlank()) return 0L
        return digitsOnly.toLongOrNull() ?: 0L
    }
}
