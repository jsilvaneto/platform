package com.platform.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateDebtPayoffPlanUseCaseTest {

    private val useCase = CalculateDebtPayoffPlanUseCase()

    @Test
    fun `calculate with empty debts should return zero months`() {
        val plan = useCase.calculate(emptyList(), 50000L)
        assertEquals(0, plan.totalMonthsToDebtFree)
        assertEquals(0L, plan.initialTotalDebtCents)
    }

    @Test
    fun `calculate snowball strategy should eliminate smallest debt first and accelerate payoff`() {
        // Dívida 1: Cartão A - R$ 1.000,00 restante, parcela mínima R$ 200,00
        val debt1 = DebtItem(
            id = "d1",
            title = "Cartão A",
            totalRemainingAmountCents = 100000L,
            monthlyPaymentCents = 20000L
        )

        // Dívida 2: Empréstimo B - R$ 500,00 restante, parcela mínima R$ 100,00
        val debt2 = DebtItem(
            id = "d2",
            title = "Empréstimo B",
            totalRemainingAmountCents = 50000L,
            monthlyPaymentCents = 10000L
        )

        // Dívida 3: Financiamento C - R$ 3.000,00 restante, parcela mínima R$ 300,00
        val debt3 = DebtItem(
            id = "d3",
            title = "Financiamento C",
            totalRemainingAmountCents = 300000L,
            monthlyPaymentCents = 30000L
        )

        // Aporte extra de R$ 200,00 (20.000 centavos) por mês
        val plan = useCase.calculate(
            debts = listOf(debt1, debt2, debt3),
            extraMonthlyAmountCents = 20000L,
            strategy = DebtPayoffStrategy.SNOWBALL
        )

        assertEquals(450000L, plan.initialTotalDebtCents)
        // Ordem Snowball: menor saldo primeiro -> d2 (500), depois d1 (1000), depois d3 (3000)
        assertEquals(listOf("d2", "d1", "d3"), plan.debtEliminationOrder)

        // Todas as dívidas devem chegar a zero
        assertTrue(plan.totalMonthsToDebtFree > 0)
        val lastMonth = plan.monthlyProgression.last()
        assertEquals(0L, lastMonth.totalRemainingCents)
    }

    @Test
    fun `calculate avalanche strategy should eliminate debt with highest monthly commitment first`() {
        val debt1 = DebtItem(
            id = "d1",
            title = "Cheque Especial",
            totalRemainingAmountCents = 100000L,
            monthlyPaymentCents = 40000L
        )
        val debt2 = DebtItem(
            id = "d2",
            title = "Cartão",
            totalRemainingAmountCents = 80000L,
            monthlyPaymentCents = 15000L
        )

        val plan = useCase.calculate(
            debts = listOf(debt1, debt2),
            extraMonthlyAmountCents = 10000L,
            strategy = DebtPayoffStrategy.AVALANCHE
        )

        // Ordem Avalanche: maior parcela/encargo primeiro -> d1, depois d2
        assertEquals(listOf("d1", "d2"), plan.debtEliminationOrder)
        assertEquals(0L, plan.monthlyProgression.last().totalRemainingCents)
    }
}
