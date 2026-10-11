package com.platform.app.domain.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

enum class BoletoType {
    BANCARIO,
    CONCESSIONARIA,
    DESCONHECIDO
}

data class BoletoInfo(
    val rawInput: String,
    val normalizedCode: String,
    val type: BoletoType,
    val isValid: Boolean,
    val barcode: String?,
    val amountCents: Long?,
    val dueDate: Long?,
    val bankCode: String? = null,
    val errorMessage: String? = null
)

object BoletoParser {

    private val BASE_DATE_1997 = LocalDate.of(1997, 10, 7)
    private val BASE_DATE_2025 = LocalDate.of(2025, 2, 22)

    /**
     * Valida e extrai dados (valor em centavos, vencimento, código de barras)
     * de linha digitável ou código de barras de boletos brasileiros.
     */
    fun parse(input: String?): BoletoInfo {
        if (input.isNullOrBlank()) {
            return BoletoInfo(
                rawInput = "",
                normalizedCode = "",
                type = BoletoType.DESCONHECIDO,
                isValid = false,
                barcode = null,
                amountCents = null,
                dueDate = null,
                errorMessage = "Código não informado"
            )
        }

        val digits = input.filter { it.isDigit() }

        return when (digits.length) {
            44 -> parseBarcode(input, digits)
            47 -> parseBankDigitableLine(input, digits)
            48 -> parseDealerDigitableLine(input, digits)
            else -> BoletoInfo(
                rawInput = input,
                normalizedCode = digits,
                type = BoletoType.DESCONHECIDO,
                isValid = false,
                barcode = null,
                amountCents = null,
                dueDate = null,
                errorMessage = "Tamanho inválido (${digits.length} dígitos). Esperado 44, 47 ou 48 dígitos."
            )
        }
    }

    private fun parseBankDigitableLine(rawInput: String, digits: String): BoletoInfo {
        // Formato: AAABC.CCCCX DDDDD.DDDDDY EEEEE.EEEEEZ K UUUUVVVVVVVVVV (47 dígitos)
        // Campo 1: 0..9 (9 dados + 1 DV)
        // Campo 2: 10..20 (10 dados + 1 DV)
        // Campo 3: 21..31 (10 dados + 1 DV)
        // Campo 4: 32 (DV geral)
        // Campo 5: 33..46 (4 fator + 10 valor)

        val campo1 = digits.substring(0, 9)
        val dv1 = digits[9].digitToInt()
        val campo2 = digits.substring(10, 20)
        val dv2 = digits[20].digitToInt()
        val campo3 = digits.substring(21, 31)
        val dv3 = digits[31].digitToInt()
        val dvGeral = digits[32].digitToInt()
        val campo5 = digits.substring(33, 47)

        val validCampo1 = calculateMod10(campo1) == dv1
        val validCampo2 = calculateMod10(campo2) == dv2
        val validCampo3 = calculateMod10(campo3) == dv3

        if (!validCampo1 || !validCampo2 || !validCampo3) {
            return BoletoInfo(
                rawInput = rawInput,
                normalizedCode = digits,
                type = BoletoType.BANCARIO,
                isValid = false,
                barcode = null,
                amountCents = null,
                dueDate = null,
                bankCode = digits.substring(0, 3),
                errorMessage = "Dígito verificador de campo inválido na linha digitável"
            )
        }

        // Montar código de barras de 44 posições
        // 0..2: Banco (AAA)
        // 3: Moeda (B)
        // 4: DV Geral (K)
        // 5..8: Fator vencimento (UUUU)
        // 9..18: Valor (10 dígitos)
        // 19..23: Campo 1 (C.CCCC)
        // 24..33: Campo 2 (DDDDD.DDDDD)
        // 34..43: Campo 3 (EEEEE.EEEEE)
        val barcode = digits.substring(0, 4) +
                dvGeral +
                campo5 +
                digits.substring(4, 9) +
                digits.substring(10, 20) +
                digits.substring(21, 31)

        val factorStr = campo5.substring(0, 4)
        val amountStr = campo5.substring(4)
        val amountCents = amountStr.toLongOrNull()
        val dueDate = parseDueDateFromFactor(factorStr.toIntOrNull() ?: 0)

        return BoletoInfo(
            rawInput = rawInput,
            normalizedCode = digits,
            type = BoletoType.BANCARIO,
            isValid = true,
            barcode = barcode,
            amountCents = amountCents,
            dueDate = dueDate,
            bankCode = digits.substring(0, 3)
        )
    }

    private fun parseDealerDigitableLine(rawInput: String, digits: String): BoletoInfo {
        // Concessionárias / Convênios (48 dígitos, 4 blocos de 11 dados + 1 DV)
        if (digits[0] != '8') {
            return BoletoInfo(
                rawInput = rawInput,
                normalizedCode = digits,
                type = BoletoType.CONCESSIONARIA,
                isValid = false,
                barcode = null,
                amountCents = null,
                dueDate = null,
                errorMessage = "Código de concessionária deve iniciar com 8"
            )
        }

        val currencyOrMod = digits[2]
        val isMod10 = currencyOrMod == '6' || currencyOrMod == '8'

        var isValidBlocks = true
        val barcodeBuilder = StringBuilder()

        for (i in 0 until 4) {
            val start = i * 12
            val blockData = digits.substring(start, start + 11)
            val blockDv = digits[start + 11].digitToInt()
            val expectedDv = if (isMod10) calculateMod10(blockData) else calculateMod11Dealer(blockData)
            if (expectedDv != blockDv) {
                isValidBlocks = false
            }
            barcodeBuilder.append(blockData)
        }

        if (!isValidBlocks) {
            return BoletoInfo(
                rawInput = rawInput,
                normalizedCode = digits,
                type = BoletoType.CONCESSIONARIA,
                isValid = false,
                barcode = null,
                amountCents = null,
                dueDate = null,
                errorMessage = "Dígito verificador de bloco inválido na linha de arrecadação"
            )
        }

        val barcode = barcodeBuilder.toString()
        // Valor no código de barras de concessionárias fica nas posições 4..14 (11 dígitos)
        val valueStr = barcode.substring(4, 15)
        val amountCents = valueStr.toLongOrNull()

        return BoletoInfo(
            rawInput = rawInput,
            normalizedCode = digits,
            type = BoletoType.CONCESSIONARIA,
            isValid = true,
            barcode = barcode,
            amountCents = amountCents,
            dueDate = null // Concessionárias não possuem fator de vencimento padronizado no código de barras
        )
    }

    private fun parseBarcode(rawInput: String, digits: String): BoletoInfo {
        if (digits[0] == '8') {
            // Concessionária código de barras (44 dígitos)
            val valueStr = digits.substring(4, 15)
            val amountCents = valueStr.toLongOrNull()
            return BoletoInfo(
                rawInput = rawInput,
                normalizedCode = digits,
                type = BoletoType.CONCESSIONARIA,
                isValid = true,
                barcode = digits,
                amountCents = amountCents,
                dueDate = null
            )
        } else {
            // Boleto bancário código de barras (44 dígitos)
            val bankCode = digits.substring(0, 3)
            val factorStr = digits.substring(5, 9)
            val amountStr = digits.substring(9, 19)
            val amountCents = amountStr.toLongOrNull()
            val dueDate = parseDueDateFromFactor(factorStr.toIntOrNull() ?: 0)

            return BoletoInfo(
                rawInput = rawInput,
                normalizedCode = digits,
                type = BoletoType.BANCARIO,
                isValid = true,
                barcode = digits,
                amountCents = amountCents,
                dueDate = dueDate,
                bankCode = bankCode
            )
        }
    }

    private fun parseDueDateFromFactor(factor: Int): Long? {
        if (factor <= 0) return null

        val localDate = if (factor >= 1000 && factor <= 9999) {
            // Rollover BACEN 2025: Fatores a partir de 1000 referenciam a data base de 2025
            // para fatores calculados a partir de 22/02/2025, ou data base 1997 para fatores legados
            if (factor >= 3000) {
                BASE_DATE_1997.plusDays(factor.toLong())
            } else {
                BASE_DATE_2025.plusDays((factor - 1000).toLong())
            }
        } else {
            BASE_DATE_1997.plusDays(factor.toLong())
        }

        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun calculateMod10(block: String): Int {
        var sum = 0
        var multiplier = 2
        for (i in block.length - 1 downTo 0) {
            val product = block[i].digitToInt() * multiplier
            sum += if (product > 9) product - 9 else product
            multiplier = if (multiplier == 2) 1 else 2
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    private fun calculateMod11Dealer(block: String): Int {
        var sum = 0
        var multiplier = 2
        for (i in block.length - 1 downTo 0) {
            sum += block[i].digitToInt() * multiplier
            multiplier = if (multiplier == 9) 2 else multiplier + 1
        }
        val remainder = sum % 11
        return when (remainder) {
            0, 1 -> 0
            10 -> 1
            else -> 11 - remainder
        }
    }
}
