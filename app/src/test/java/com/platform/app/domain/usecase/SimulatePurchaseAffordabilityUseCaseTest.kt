package com.platform.app.domain.usecase

import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SimulatePurchaseAffordabilityUseCaseTest {

    private val getCreditCardSummariesUseCase = mockk<GetCreditCardSummariesUseCase>(relaxed = true)
    private val useCase = SimulatePurchaseAffordabilityUseCase(getCreditCardSummariesUseCase)

    @Test
    fun `simulate with empty card summaries should return empty result`() {
        val result = useCase.simulate(emptyList(), 10000L)

        assertFalse(result.hasAffordableCard)
        assertEquals(0, result.options.size)
    }

    @Test
    fun `simulate with two cards should recommend card with greatest float when both have limit`() {
        // Data fixa: 15/10/2026
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        }
        val currentTs = cal.timeInMillis

        // Cartão A: fecha dia 10 (já fechou esse mês, compra cai na próxima fatura de novembro -> máximo float)
        val cardA = CreditCard(
            id = "card_a",
            name = "Nubank",
            totalLimitCents = 500000L, // R$ 5.000,00
            closingDay = 10,
            dueDay = 17,
            colorHex = "#820AD1"
        )
        val summaryA = CreditCardWithInvoiceSummary(
            card = cardA,
            currentInvoice = null,
            usedLimitCents = 100000L // R$ 1.000,00 usado -> R$ 4.000,00 livre
        )

        // Cartão B: fecha dia 25 (ainda não fechou este mês, compra cai na fatura atual de outubro -> prazo curto)
        val cardB = CreditCard(
            id = "card_b",
            name = "Itaú",
            totalLimitCents = 300000L, // R$ 3.000,00
            closingDay = 25,
            dueDay = 30,
            colorHex = "#EC7000"
        )
        val summaryB = CreditCardWithInvoiceSummary(
            card = cardB,
            currentInvoice = null,
            usedLimitCents = 50000L // R$ 500,00 usado -> R$ 2.500,00 livre
        )

        // Compra de R$ 800,00 (80.000 centavos)
        val result = useCase.simulate(listOf(summaryA, summaryB), 80000L, currentTs)

        assertTrue(result.hasAffordableCard)
        assertEquals("card_a", result.recommendedCardId)
        val recOption = result.options.first { it.cardId == "card_a" }
        assertTrue(recOption.isRecommended)
        assertNotNull(recOption.recommendationReason)
    }

    @Test
    fun `simulate with insufficient limit should report cannot afford`() {
        val card = CreditCard(
            id = "card_c",
            name = "Inter",
            totalLimitCents = 100000L, // R$ 1.000,00
            closingDay = 5,
            dueDay = 12,
            colorHex = "#FF7A00"
        )
        val summary = CreditCardWithInvoiceSummary(
            card = card,
            currentInvoice = null,
            usedLimitCents = 80000L // R$ 800,00 usado -> R$ 200,00 livre
        )

        // Compra de R$ 500,00 (50.000 centavos)
        val result = useCase.simulate(listOf(summary), 50000L)

        assertFalse(result.hasAffordableCard)
        assertEquals(null, result.recommendedCardId)
        assertFalse(result.options[0].canAfford)
    }
}
