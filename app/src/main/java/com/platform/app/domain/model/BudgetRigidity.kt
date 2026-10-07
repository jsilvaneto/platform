package com.platform.app.domain.model

import java.util.Locale

enum class BudgetRigidityStatus {
    VAZIO,
    ENGESSADO,
    ESTILO_DE_VIDA_ELEVADO,
    SOBRECARREGADO,
    SEM_POUPANCA,
    NAO_CLASSIFICADO,
    EQUILIBRADO,
    EXCELENTE
}

data class BudgetRigidityAnalysis(
    val status: BudgetRigidityStatus,
    val badgeLabel: String,
    val description: String,
    val mandatoryPercentage: Float,
    val necessaryPercentage: Float,
    val wantsPercentage: Float,
    val nonePercentage: Float,
    val savingsPercentage: Float,
    val essentialsPercentage: Float,
    val totalBudgetCents: Long,
    val savingsCents: Long
)

object BudgetRigidityCalculator {

    fun calculate(
        mandatoryAmountCents: Long,
        necessaryAmountCents: Long,
        wantsAmountCents: Long,
        noneAmountCents: Long,
        savingsAmountCents: Long
    ): BudgetRigidityAnalysis {
        val totalBudgetCents = (mandatoryAmountCents + necessaryAmountCents + wantsAmountCents + noneAmountCents + savingsAmountCents)
            .coerceAtLeast(0L)

        if (totalBudgetCents == 0L) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.VAZIO,
                badgeLabel = "Sem Lançamentos",
                description = "Cadastre contas a pagar e metas para avaliar a rigidez do seu orçamento.",
                mandatoryPercentage = 0f,
                necessaryPercentage = 0f,
                wantsPercentage = 0f,
                nonePercentage = 0f,
                savingsPercentage = 0f,
                essentialsPercentage = 0f,
                totalBudgetCents = 0L,
                savingsCents = 0L
            )
        }

        val totalFloat = totalBudgetCents.toFloat()
        val mandatoryPct = (mandatoryAmountCents.toFloat() / totalFloat) * 100f
        val necessaryPct = (necessaryAmountCents.toFloat() / totalFloat) * 100f
        val wantsPct = (wantsAmountCents.toFloat() / totalFloat) * 100f
        val nonePct = (noneAmountCents.toFloat() / totalFloat) * 100f
        val savingsPct = (savingsAmountCents.toFloat() / totalFloat) * 100f
        val essentialsPct = mandatoryPct + necessaryPct

        // Regra 1: Sobrecarga Essencial (0% Desejos e 0% Poupança)
        // Quando 100% do orçamento é absorvido por despesas básicas/não categorizadas, sem lazer ou poupança
        if (wantsAmountCents == 0L && savingsAmountCents == 0L) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.SOBRECARREGADO,
                badgeLabel = "Atenção: Sobrecarga Essencial",
                description = "100% do orçamento consumido por despesas básicas, sem margem para desejos (0%) ou poupança (0%). Orçamento vulnerável a imprevistos.",
                mandatoryPercentage = mandatoryPct,
                necessaryPercentage = necessaryPct,
                wantsPercentage = wantsPct,
                nonePercentage = nonePct,
                savingsPercentage = savingsPct,
                essentialsPercentage = essentialsPct,
                totalBudgetCents = totalBudgetCents,
                savingsCents = savingsAmountCents
            )
        }

        // Regra 2: Orçamento Engessado
        // Mais de 55% estritamente em contas Obrigatórias ou mais de 75% em Essenciais totais
        if (mandatoryPct > 55f || essentialsPct > 75f) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.ENGESSADO,
                badgeLabel = "Atenção: Orçamento Engessado",
                description = "Gastos obrigatórios/essenciais absorvem ${String.format(Locale.US, "%.0f", essentialsPct)}% do orçamento (${String.format(Locale.US, "%.0f", mandatoryPct)}% obrigatório). Margem de ajuste muito estreita.",
                mandatoryPercentage = mandatoryPct,
                necessaryPercentage = necessaryPct,
                wantsPercentage = wantsPct,
                nonePercentage = nonePct,
                savingsPercentage = savingsPct,
                essentialsPercentage = essentialsPct,
                totalBudgetCents = totalBudgetCents,
                savingsCents = savingsAmountCents
            )
        }

        // Regra 3: Estilo de Vida Elevado
        // Mais de 35% do orçamento destinado a desejos/estilo de vida (ideal 50-30-20 é até 30%)
        if (wantsPct > 35f) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.ESTILO_DE_VIDA_ELEVADO,
                badgeLabel = "Alerta: Estilo de Vida Elevado",
                description = "Gastos com desejos e estilo de vida representam ${String.format(Locale.US, "%.0f", wantsPct)}% do orçamento (ideal: até 30%). Risco de comprimir reservas e metas.",
                mandatoryPercentage = mandatoryPct,
                necessaryPercentage = necessaryPct,
                wantsPercentage = wantsPct,
                nonePercentage = nonePct,
                savingsPercentage = savingsPct,
                essentialsPercentage = essentialsPct,
                totalBudgetCents = totalBudgetCents,
                savingsCents = savingsAmountCents
            )
        }

        // Regra 4: Sem Poupança / Poupança Insuficiente
        // Desejos ativos, mas menos de 5% em metas/poupança (ideal é 20%)
        if (savingsPct < 5f) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.SEM_POUPANCA,
                badgeLabel = "Atenção: Poupança Insuficiente",
                description = "Poupança e metas representam apenas ${String.format(Locale.US, "%.0f", savingsPct)}% do orçamento (ideal: 20%). Procure direcionar recursos para suas metas.",
                mandatoryPercentage = mandatoryPct,
                necessaryPercentage = necessaryPct,
                wantsPercentage = wantsPct,
                nonePercentage = nonePct,
                savingsPercentage = savingsPct,
                essentialsPercentage = essentialsPct,
                totalBudgetCents = totalBudgetCents,
                savingsCents = savingsAmountCents
            )
        }

        // Regra 5: Alto Volume Não Classificado
        // Mais de 20% das despesas sem categoria/natureza
        if (nonePct > 20f) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.NAO_CLASSIFICADO,
                badgeLabel = "Atenção: Gastos Não Classificados",
                description = "${String.format(Locale.US, "%.0f", nonePct)}% dos gastos estão sem natureza definida. Classifique suas despesas para um diagnóstico mais fiel.",
                mandatoryPercentage = mandatoryPct,
                necessaryPercentage = necessaryPct,
                wantsPercentage = wantsPct,
                nonePercentage = nonePct,
                savingsPercentage = savingsPct,
                essentialsPercentage = essentialsPct,
                totalBudgetCents = totalBudgetCents,
                savingsCents = savingsAmountCents
            )
        }

        // Regra 6: Excelente (Padrão 50-30-20 Equilibrado Real)
        // Essenciais <= 60% (com Obrigatório <= 50%), Desejos entre 10% e 35%, Poupança >= 15%, Não classificado <= 10%
        if (essentialsPct <= 60f && mandatoryPct <= 50f && wantsPct in 10f..35f && savingsPct >= 15f && nonePct <= 10f) {
            return BudgetRigidityAnalysis(
                status = BudgetRigidityStatus.EXCELENTE,
                badgeLabel = "Excelente: Padrão 50-30-20 Equilibrado",
                description = "Distribuição exemplar: ${String.format(Locale.US, "%.0f", essentialsPct)}% essenciais, ${String.format(Locale.US, "%.0f", wantsPct)}% desejos e ${String.format(Locale.US, "%.0f", savingsPct)}% poupança. Margem de segurança robusta.",
                mandatoryPercentage = mandatoryPct,
                necessaryPercentage = necessaryPct,
                wantsPercentage = wantsPct,
                nonePercentage = nonePct,
                savingsPercentage = savingsPct,
                essentialsPercentage = essentialsPct,
                totalBudgetCents = totalBudgetCents,
                savingsCents = savingsAmountCents
            )
        }

        // Regra 7: Equilibrado / Em Evolução
        // Cenário saudável com poupança ativa (> 5%), mas ainda em ajuste para a meta 50-30-20
        return BudgetRigidityAnalysis(
            status = BudgetRigidityStatus.EQUILIBRADO,
            badgeLabel = "Bom: Orçamento em Equilíbrio",
            description = "Distribuição saudável com poupança ativa (${String.format(Locale.US, "%.0f", savingsPct)}%). Continue direcionando excedentes para metas.",
            mandatoryPercentage = mandatoryPct,
            necessaryPercentage = necessaryPct,
            wantsPercentage = wantsPct,
            nonePercentage = nonePct,
            savingsPercentage = savingsPct,
            essentialsPercentage = essentialsPct,
            totalBudgetCents = totalBudgetCents,
            savingsCents = savingsAmountCents
        )
    }
}
