package com.platform.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BudgetRigidityCalculatorTest {

    @Test
    fun `should return VAZIO when total budget is 0`() {
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 0L,
            necessaryAmountCents = 0L,
            wantsAmountCents = 0L,
            noneAmountCents = 0L,
            savingsAmountCents = 0L
        )

        assertEquals(BudgetRigidityStatus.VAZIO, result.status)
        assertEquals("Sem Lançamentos", result.badgeLabel)
        assertEquals(0L, result.totalBudgetCents)
    }

    @Test
    fun `should return SOBRECARREGADO and not EXCELENTE when Deseja and Poupanca are 0`() {
        // Cenário do card antigo: Obrigatório <= 55% e Deseja <= 30% mas Deseja=0 e Poupança=0
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 50000L, // 50%
            necessaryAmountCents = 50000L, // 50%
            wantsAmountCents = 0L,         // 0%
            noneAmountCents = 0L,
            savingsAmountCents = 0L        // 0%
        )

        assertNotEquals(BudgetRigidityStatus.EXCELENTE, result.status)
        assertEquals(BudgetRigidityStatus.SOBRECARREGADO, result.status)
        assertEquals("Atenção: Sobrecarga Essencial", result.badgeLabel)
    }

    @Test
    fun `should return EXCELENTE when 50-30-20 distribution is balanced`() {
        // 50% Essenciais (35% Obrigatório + 15% Necessário), 30% Deseja, 20% Poupança
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 35000L,
            necessaryAmountCents = 15000L,
            wantsAmountCents = 30000L,
            noneAmountCents = 0L,
            savingsAmountCents = 20000L
        )

        assertEquals(BudgetRigidityStatus.EXCELENTE, result.status)
        assertEquals("Excelente: Padrão 50-30-20 Equilibrado", result.badgeLabel)
        assertEquals(50f, result.essentialsPercentage, 0.01f)
        assertEquals(30f, result.wantsPercentage, 0.01f)
        assertEquals(20f, result.savingsPercentage, 0.01f)
    }

    @Test
    fun `should return ENGESSADO when mandatory exceeds 55 percent`() {
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 60000L, // 60%
            necessaryAmountCents = 10000L,
            wantsAmountCents = 15000L,
            noneAmountCents = 0L,
            savingsAmountCents = 15000L
        )

        assertEquals(BudgetRigidityStatus.ENGESSADO, result.status)
        assertEquals("Atenção: Orçamento Engessado", result.badgeLabel)
    }

    @Test
    fun `should return ESTILO_DE_VIDA_ELEVADO when wants exceed 35 percent`() {
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 30000L,
            necessaryAmountCents = 10000L,
            wantsAmountCents = 45000L, // 45%
            noneAmountCents = 0L,
            savingsAmountCents = 15000L
        )

        assertEquals(BudgetRigidityStatus.ESTILO_DE_VIDA_ELEVADO, result.status)
        assertEquals("Alerta: Estilo de Vida Elevado", result.badgeLabel)
    }

    @Test
    fun `should return SEM_POUPANCA when wants exist but savings is under 5 percent`() {
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 50000L, // 50%
            necessaryAmountCents = 20000L, // 20% (essenciais 70%)
            wantsAmountCents = 30000L,     // 30%
            noneAmountCents = 0L,
            savingsAmountCents = 0L        // 0%
        )

        assertEquals(BudgetRigidityStatus.SEM_POUPANCA, result.status)
        assertEquals("Atenção: Poupança Insuficiente", result.badgeLabel)
    }

    @Test
    fun `should return NAO_CLASSIFICADO when unclassified exceeds 20 percent`() {
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 30000L,
            necessaryAmountCents = 10000L,
            wantsAmountCents = 20000L,
            noneAmountCents = 30000L, // 30%
            savingsAmountCents = 10000L
        )

        assertEquals(BudgetRigidityStatus.NAO_CLASSIFICADO, result.status)
        assertEquals("Atenção: Gastos Não Classificados", result.badgeLabel)
    }

    @Test
    fun `should return EQUILIBRADO when budget is healthy with moderate savings`() {
        val result = BudgetRigidityCalculator.calculate(
            mandatoryAmountCents = 45000L,
            necessaryAmountCents = 15000L,
            wantsAmountCents = 30000L,
            noneAmountCents = 0L,
            savingsAmountCents = 10000L // 10%
        )

        assertEquals(BudgetRigidityStatus.EQUILIBRADO, result.status)
        assertEquals("Bom: Orçamento em Equilíbrio", result.badgeLabel)
    }
}
