package com.platform.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoletoParserTest {

    @Test
    fun `parse valid bank digitable line should extract amount, factor and barcode`() {
        // Exemplo de linha digitável bancária real de 47 dígitos com DVs matemáticos válidos (9, 6, 0)
        val line = "00190.00009 01234.567806 00000.000000 5 99990000015025"

        val result = BoletoParser.parse(line)

        assertTrue(result.isValid)
        assertEquals(BoletoType.BANCARIO, result.type)
        assertEquals("001", result.bankCode)
        assertEquals(15025L, result.amountCents) // R$ 150,25
        assertNotNull(result.dueDate)
        assertNotNull(result.barcode)
        assertEquals(44, result.barcode?.length)
    }

    @Test
    fun `parse bank digitable line with invalid check digit should return invalid`() {
        // Linha com DV adulterado no primeiro campo: '8' em vez de '9'
        val invalidLine = "00190.00008 01234.567806 00000.000000 5 99990000015025"

        val result = BoletoParser.parse(invalidLine)

        assertFalse(result.isValid)
        assertEquals(BoletoType.BANCARIO, result.type)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `parse valid 44-digit bank barcode should extract amount and bank`() {
        // 44 dígitos: 001 9 5 9999 0000015025 000000123456780000000000
        val barcode = "00195999900000150250000001234567800000000000"

        val result = BoletoParser.parse(barcode)

        assertTrue(result.isValid)
        assertEquals(BoletoType.BANCARIO, result.type)
        assertEquals("001", result.bankCode)
        assertEquals(15025L, result.amountCents)
        assertEquals(barcode, result.barcode)
    }

    @Test
    fun `parse valid dealer barcode starting with 8 should extract amount`() {
        // 44 dígitos iniciando em 8: concessionária com valor R$ 85,40 (00000008540) nas posições 4..14
        val dealerBarcode = "84670000000854000000000000000000000000000000"

        val result = BoletoParser.parse(dealerBarcode)

        assertTrue(result.isValid)
        assertEquals(BoletoType.CONCESSIONARIA, result.type)
        assertEquals(8540L, result.amountCents)
        assertEquals(dealerBarcode, result.barcode)
    }

    @Test
    fun `parse blank or malformed input should return invalid`() {
        val emptyResult = BoletoParser.parse("")
        assertFalse(emptyResult.isValid)

        val shortResult = BoletoParser.parse("123456789")
        assertFalse(shortResult.isValid)
        assertEquals(BoletoType.DESCONHECIDO, shortResult.type)
    }
}
