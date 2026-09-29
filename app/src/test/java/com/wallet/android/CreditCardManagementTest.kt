package com.wallet.android

import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CreditCardManagementTest {

    @Test
    fun `should fully validate credit card fields rejecting blank name zero limit or invalid days`() {
        // 1. Nome em branco
        val resBlankName = CreditCardCalculator.validateCard(
            name = "   ",
            totalLimitCents = 500000L,
            closingDay = 25,
            dueDay = 5
        )
        assertFalse(resBlankName.isValid)
        assertNotNull(resBlankName.nameError)

        // 2. Limite menor ou igual a zero
        val resZeroLimit = CreditCardCalculator.validateCard(
            name = "Nubank",
            totalLimitCents = 0L,
            closingDay = 25,
            dueDay = 5
        )
        assertFalse(resZeroLimit.isValid)
        assertNotNull(resZeroLimit.limitError)

        // 3. Dia de corte inválido (ex: 0 ou 32)
        val resInvalidClosing = CreditCardCalculator.validateCard(
            name = "Nubank",
            totalLimitCents = 500000L,
            closingDay = 32,
            dueDay = 5
        )
        assertFalse(resInvalidClosing.isValid)
        assertNotNull(resInvalidClosing.closingDayError)

        // 4. Dia de vencimento inválido (ex: -1)
        val resInvalidDue = CreditCardCalculator.validateCard(
            name = "Nubank",
            totalLimitCents = 500000L,
            closingDay = 25,
            dueDay = 0
        )
        assertFalse(resInvalidDue.isValid)
        assertNotNull(resInvalidDue.dueDayError)

        // 5. Todos os dados válidos
        val resValid = CreditCardCalculator.validateCard(
            name = "Nubank Ultravioleta",
            totalLimitCents = 1500000L,
            closingDay = 25,
            dueDay = 5
        )
        assertTrue(resValid.isValid)
        assertNull(resValid.nameError)
        assertNull(resValid.limitError)
        assertNull(resValid.closingDayError)
        assertNull(resValid.dueDayError)
    }

    @Test
    fun `should determine invoice reference month accurately according to closing cut off day`() {
        val closingDay = 25
        val year = 2026
        val monthSept = Calendar.SEPTEMBER // 8 (0-indexed)

        // Compra no dia 15/09/2026 (antes do corte 25) -> Fatura 2026-09
        val purchaseBeforeCut = Calendar.getInstance().apply {
            set(year, monthSept, 15, 14, 0, 0)
        }.timeInMillis
        val refMonthBefore = CreditCardCalculator.determineInvoiceReferenceMonth(purchaseBeforeCut, closingDay)
        assertEquals("2026-09", refMonthBefore)

        // Compra no dia 26/09/2026 (após o corte 25) -> Fatura 2026-10
        val purchaseAfterCut = Calendar.getInstance().apply {
            set(year, monthSept, 26, 10, 30, 0)
        }.timeInMillis
        val refMonthAfter = CreditCardCalculator.determineInvoiceReferenceMonth(purchaseAfterCut, closingDay)
        assertEquals("2026-10", refMonthAfter)

        // Compra no dia 28/12/2026 (após o corte de dezembro) -> Fatura 2027-01 do ano seguinte
        val purchaseDecAfterCut = Calendar.getInstance().apply {
            set(year, Calendar.DECEMBER, 28, 18, 0, 0)
        }.timeInMillis
        val refMonthDecAfter = CreditCardCalculator.determineInvoiceReferenceMonth(purchaseDecAfterCut, closingDay)
        assertEquals("2027-01", refMonthDecAfter)
    }

    @Test
    fun `should calculate subsequent reference months for installments across year boundary`() {
        val baseRefMonth = "2026-10"

        // Parcela 1: Mês base (+0 meses)
        assertEquals("2026-10", CreditCardCalculator.addMonthsToReferenceMonth(baseRefMonth, 0))

        // Parcela 2: +1 mês
        assertEquals("2026-11", CreditCardCalculator.addMonthsToReferenceMonth(baseRefMonth, 1))

        // Parcela 3: +2 meses
        assertEquals("2026-12", CreditCardCalculator.addMonthsToReferenceMonth(baseRefMonth, 2))

        // Parcela 4: +3 meses (virada de ano para 2027)
        assertEquals("2027-01", CreditCardCalculator.addMonthsToReferenceMonth(baseRefMonth, 3))

        // Parcela 12: +11 meses
        assertEquals("2027-09", CreditCardCalculator.addMonthsToReferenceMonth(baseRefMonth, 11))
    }

    @Test
    fun `should calculate available limit accurately and restore after invoice payment`() {
        val card = CreditCard(
            id = "c1",
            name = "Visa",
            totalLimitCents = 500000L, // R$ 5.000,00
            closingDay = 20,
            dueDay = 1
        )

        val usedBefore = 200000L // R$ 2.000,00 consumidos
        val availableBefore = CreditCardCalculator.calculateAvailableLimit(card.totalLimitCents, usedBefore)
        assertEquals(300000L, availableBefore)

        // Após pagar fatura de R$ 2.000,00, o limite utilizado vai a 0
        val usedAfterPayment = 0L
        val availableAfterPayment = CreditCardCalculator.calculateAvailableLimit(card.totalLimitCents, usedAfterPayment)
        assertEquals(500000L, availableAfterPayment)
    }
}
