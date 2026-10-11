package com.platform.app.domain.usecase

import javax.inject.Inject

enum class DebtPayoffStrategy {
    SNOWBALL, // Menor saldo devedor primeiro (foco comportamental e vitórias rápidas)
    AVALANCHE // Maior compromisso/juros primeiro (foco matemático e redução de encargos)
}

data class DebtItem(
    val id: String,
    val title: String,
    val totalRemainingAmountCents: Long,
    val monthlyPaymentCents: Long,
    val categoryName: String? = null
)

data class DebtPayoffMonth(
    val monthIndex: Int,
    val debtsRemaining: Map<String, Long>,
    val totalRemainingCents: Long,
    val totalPaidInMonthCents: Long,
    val paidOffDebtIds: List<String>
)

data class DebtPayoffPlan(
    val strategy: DebtPayoffStrategy,
    val initialTotalDebtCents: Long,
    val extraMonthlyAmountCents: Long,
    val totalMonthsToDebtFree: Int,
    val monthlyProgression: List<DebtPayoffMonth>,
    val debtEliminationOrder: List<String>
)

/**
 * Caso de uso para elaboração do plano de quitação acelerada de dívidas (Prompt 50).
 * Implementa tanto o método Bola de Neve (Snowball) quanto Avalanche.
 */
class CalculateDebtPayoffPlanUseCase @Inject constructor() {

    fun calculate(
        debts: List<DebtItem>,
        extraMonthlyAmountCents: Long = 0L,
        strategy: DebtPayoffStrategy = DebtPayoffStrategy.SNOWBALL
    ): DebtPayoffPlan {
        if (debts.isEmpty()) {
            return DebtPayoffPlan(
                strategy = strategy,
                initialTotalDebtCents = 0L,
                extraMonthlyAmountCents = extraMonthlyAmountCents,
                totalMonthsToDebtFree = 0,
                monthlyProgression = emptyList(),
                debtEliminationOrder = emptyList()
            )
        }

        // Ordenar dívidas pela estratégia escolhida
        val sortedDebts = when (strategy) {
            DebtPayoffStrategy.SNOWBALL -> debts.sortedWith(
                compareBy<DebtItem> { it.totalRemainingAmountCents }
                    .thenBy { it.monthlyPaymentCents }
            )
            DebtPayoffStrategy.AVALANCHE -> debts.sortedWith(
                compareByDescending<DebtItem> { it.monthlyPaymentCents }
                    .thenBy { it.totalRemainingAmountCents }
            )
        }

        val eliminationOrder = sortedDebts.map { it.id }
        val remainingMap = sortedDebts.associate { it.id to it.totalRemainingAmountCents }.toMutableMap()
        val initialTotal = sortedDebts.sumOf { it.totalRemainingAmountCents }

        val monthlyProgression = mutableListOf<DebtPayoffMonth>()
        var monthIndex = 1
        val maxMonths = 360 // Teto de segurança de 30 anos

        while (remainingMap.values.any { it > 0 } && monthIndex <= maxMonths) {
            var extraPool = extraMonthlyAmountCents
            var paidThisMonth = 0L
            val newlyPaidOff = mutableListOf<String>()

            // 1. Pagar a parcela mínima de cada dívida ativa
            for (debt in sortedDebts) {
                val currentRemaining = remainingMap[debt.id] ?: 0L
                if (currentRemaining > 0) {
                    val minPayment = debt.monthlyPaymentCents.coerceAtMost(currentRemaining)
                    remainingMap[debt.id] = currentRemaining - minPayment
                    paidThisMonth += minPayment

                    if (remainingMap[debt.id] == 0L) {
                        newlyPaidOff.add(debt.id)
                    }
                }
            }

            // 2. Canalizar o pool extra (efeito bola de neve) para a dívida prioritária ainda em aberto
            for (debt in sortedDebts) {
                val currentRemaining = remainingMap[debt.id] ?: 0L
                if (currentRemaining > 0 && extraPool > 0) {
                    val extraToPay = extraPool.coerceAtMost(currentRemaining)
                    remainingMap[debt.id] = currentRemaining - extraToPay
                    paidThisMonth += extraToPay
                    extraPool -= extraToPay

                    if (remainingMap[debt.id] == 0L && debt.id !in newlyPaidOff) {
                        newlyPaidOff.add(debt.id)
                    }
                }
            }

            val totalRemaining = remainingMap.values.sum()
            monthlyProgression.add(
                DebtPayoffMonth(
                    monthIndex = monthIndex,
                    debtsRemaining = remainingMap.toMap(),
                    totalRemainingCents = totalRemaining,
                    totalPaidInMonthCents = paidThisMonth,
                    paidOffDebtIds = newlyPaidOff
                )
            )

            monthIndex++
        }

        return DebtPayoffPlan(
            strategy = strategy,
            initialTotalDebtCents = initialTotal,
            extraMonthlyAmountCents = extraMonthlyAmountCents,
            totalMonthsToDebtFree = monthlyProgression.size,
            monthlyProgression = monthlyProgression,
            debtEliminationOrder = eliminationOrder
        )
    }
}
