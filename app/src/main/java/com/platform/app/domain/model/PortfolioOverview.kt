package com.platform.app.domain.model

/**
 * Modelo de domínio unificado com os agregados da Visão Geral da Carteira (Prompt 58).
 * Contém métricas de atenção, parcelado em aberto, tiles operacionais, série temporal de 12 meses
 * e projeção de quando o orçamento fica livre.
 */
data class PortfolioOverview(
    // Atenção: nº de atrasadas + vencem hoje (aviso que leva a Hoje; some se 0)
    val overdueCount: Int = 0,
    val dueTodayCount: Int = 0,

    // Hero "Parcelado em aberto": soma das parcelas não pagas de INSTALLMENT, nº de parcelas e mês do último vencimento
    val openInstallmentsAmountCents: Long = 0L,
    val openInstallmentsCount: Int = 0,
    val lastInstallmentDueDate: Long? = null,

    // Tiles
    val fixedCostMonthlyCents: Long = 0L,
    val cardsUsagePercentage: Int? = null,
    val cardsTotalUsedCents: Long = 0L,
    val cardsTotalLimitCents: Long = 0L,
    val goalsReservePercentage: Int? = null,

    // Série de 12 meses: 2 passados, o atual e 9 futuros
    val monthlySeries: List<MonthlyBarData> = emptyList(),

    // "Quando fica livre": até 5 compromissos por data de término, com o valor que libera por mês
    val freeingUpCommitments: List<FreeingUpCommitment> = emptyList()
) {
    val attentionCount: Int get() = overdueCount + dueTodayCount
    val hasAttention: Boolean get() = attentionCount > 0
    val hasDebts: Boolean get() = openInstallmentsAmountCents > 0L
    val isEmpty: Boolean get() = openInstallmentsAmountCents == 0L &&
            fixedCostMonthlyCents == 0L &&
            cardsTotalUsedCents == 0L &&
            monthlySeries.all { it.amountCents == 0L }
}

/**
 * Dados de uma barra na série histórica e prospectiva de 12 meses.
 */
data class MonthlyBarData(
    val monthTimestamp: Long,
    val label: String,
    val amountCents: Long,
    val isPast: Boolean,
    val isCurrent: Boolean,
    val isPeak: Boolean,
    val isLowestFuture: Boolean
)

/**
 * Compromisso financeiro com previsão de término, indicando margem liberada no orçamento.
 */
data class FreeingUpCommitment(
    val billId: String,
    val title: String,
    val endDate: Long,
    val monthlyAmountCents: Long
)

data class AttentionCounts(
    val overdueCount: Int,
    val dueTodayCount: Int
)

data class OpenInstallmentsSummary(
    val totalAmountCents: Long,
    val count: Int,
    val lastDueDate: Long?
)

