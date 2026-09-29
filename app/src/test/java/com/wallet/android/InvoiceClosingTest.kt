package com.wallet.android

import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class InvoiceClosingTest {

    @Test
    fun `should calculate available limit accurately without becoming negative`() {
        val card = CreditCard(
            id = "card-1",
            name = "Cartão Platinum",
            totalLimitCents = 1000000L, // R$ 10.000,00
            closingDay = 25,
            dueDay = 5
        )

        // Limite disponível inicial
        val summaryInitial = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = null,
            usedLimitCents = 0L
        )
        assertEquals(1000000L, summaryInitial.availableLimitCents)

        // Após gasto de R$ 3.500,00 (350000 centavos)
        val summaryUsed = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = null,
            usedLimitCents = 350000L
        )
        assertEquals(650000L, summaryUsed.availableLimitCents)

        // Quando o limite total é exatamente atingido
        val summaryMax = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = null,
            usedLimitCents = 1000000L
        )
        assertEquals(0L, summaryMax.availableLimitCents)

        // Quando o limite é excedido (por exemplo juros ou excesso), não deve retornar negativo
        val summaryOver = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = null,
            usedLimitCents = 1200000L
        )
        assertEquals(0L, summaryOver.availableLimitCents)

        // Cálculo via utilitário direto
        val available = CreditCardCalculator.calculateAvailableLimit(1000000L, 250000L)
        assertEquals(750000L, available)
    }

    @Test
    fun `should close invoice when current date reaches or passes closing day`() {
        val closingDay = 25
        val year = 2026
        val month = Calendar.SEPTEMBER // Mês 8 (base 0)

        val closingTimestamp = CreditCardCalculator.calculateClosingDate(year, month, closingDay)

        // 1. Data antes do fechamento (dia 24 às 12:00) -> Fatura deve estar ABERTA
        val calBefore = Calendar.getInstance().apply {
            set(year, month, 24, 12, 0, 0)
        }
        val statusBefore = CreditCardCalculator.determineInvoiceStatus(
            currentTimestamp = calBefore.timeInMillis,
            closingTimestamp = closingTimestamp,
            isPaid = false
        )
        assertEquals(InvoiceStatus.ABERTA, statusBefore)

        // 2. Data no próprio dia de fechamento após 23:59:59 ou dia seguinte (dia 26 às 10:00) -> Fatura FECHADA
        val calAfter = Calendar.getInstance().apply {
            set(year, month, 26, 10, 0, 0)
        }
        val statusAfter = CreditCardCalculator.determineInvoiceStatus(
            currentTimestamp = calAfter.timeInMillis,
            closingTimestamp = closingTimestamp,
            isPaid = false
        )
        assertEquals(InvoiceStatus.FECHADA, statusAfter)

        // 3. Fatura já quitada -> Sempre PAGA independente da data
        val statusPaid = CreditCardCalculator.determineInvoiceStatus(
            currentTimestamp = calAfter.timeInMillis,
            closingTimestamp = closingTimestamp,
            isPaid = true
        )
        assertEquals(InvoiceStatus.PAGA, statusPaid)
    }

    @Test
    fun `should determine whether purchase belongs to current or next invoice based on closing day`() {
        val closingDay = 25
        val year = 2026
        val month = Calendar.SEPTEMBER

        val closingTimestamp = CreditCardCalculator.calculateClosingDate(year, month, closingDay)

        // Compra no dia 20 de setembro -> Pertence à fatura atual
        val purchaseOnDay20 = Calendar.getInstance().apply {
            set(year, month, 20, 15, 30, 0)
        }.timeInMillis
        assertTrue(CreditCardCalculator.shouldBelongToCurrentInvoice(purchaseOnDay20, closingTimestamp))

        // Compra no dia 25 de setembro às 22:00 -> Pertence à fatura atual
        val purchaseOnDay25Night = Calendar.getInstance().apply {
            set(year, month, 25, 22, 0, 0)
        }.timeInMillis
        assertTrue(CreditCardCalculator.shouldBelongToCurrentInvoice(purchaseOnDay25Night, closingTimestamp))

        // Compra no dia 26 de setembro -> NÃO pertence à fatura atual (vai para o próximo ciclo)
        val purchaseOnDay26 = Calendar.getInstance().apply {
            set(year, month, 26, 8, 0, 0)
        }.timeInMillis
        assertFalse(CreditCardCalculator.shouldBelongToCurrentInvoice(purchaseOnDay26, closingTimestamp))
    }

    @Test
    fun `should calculate due date into following month when due day is before or equal to closing day`() {
        val closingDay = 25
        val dueDay = 5 // Vence dia 5 do mês seguinte
        val year = 2026
        val month = Calendar.SEPTEMBER

        val dueDate = CreditCardCalculator.calculateDueDate(year, month, closingDay, dueDay)

        val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH))
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH))
    }
}
