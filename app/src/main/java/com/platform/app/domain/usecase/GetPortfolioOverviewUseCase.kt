package com.platform.app.domain.usecase

import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.domain.model.MonthlyBarData
import com.platform.app.domain.model.PortfolioOverview
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Caso de uso da Visão Geral da Carteira (Prompt 58).
 * Agrega métricas de atenção, parcelamento em aberto, tiles operacionais,
 * série temporal de 12 meses e compromissos com previsão de liberação orçamentária.
 */
class GetPortfolioOverviewUseCase @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val getCardUsageUseCase: GetCardUsageUseCase,
    private val goalRepository: GoalRepository
) {

    operator fun invoke(nowMillis: Long = System.currentTimeMillis()): Flow<PortfolioOverview> {
        // Intervalo do dia atual para cálculo de vencem hoje e atrasadas
        val dayCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        dayCal.set(Calendar.HOUR_OF_DAY, 0)
        dayCal.set(Calendar.MINUTE, 0)
        dayCal.set(Calendar.SECOND, 0)
        dayCal.set(Calendar.MILLISECOND, 0)
        val startOfDay = dayCal.timeInMillis

        dayCal.set(Calendar.HOUR_OF_DAY, 23)
        dayCal.set(Calendar.MINUTE, 59)
        dayCal.set(Calendar.SECOND, 59)
        dayCal.set(Calendar.MILLISECOND, 999)
        val endOfDay = dayCal.timeInMillis

        // Intervalo de 12 meses (2 passados, o atual e 9 futuros)
        val startCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, -2)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startRange = startCal.timeInMillis

        val endCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, 9)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endRange = endCal.timeInMillis

        // Fluxo A: Indicadores estruturais de contas
        val flowA = combine(
            financialRepository.getAttentionCounts(startOfDay, endOfDay),
            financialRepository.getOpenInstallmentsOverview(),
            financialRepository.getActiveMonthlyRecurringCost(),
            getCardUsageUseCase()
        ) { attention, openInstallments, fixedCost, cardUsages ->
            val totalUsedCents = cardUsages.sumOf { it.usedLimitCents }
            val totalLimitCents = cardUsages.sumOf { it.totalLimitCents }
            val usagePercentage = if (totalLimitCents > 0L) {
                ((totalUsedCents.toDouble() / totalLimitCents.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else null

            TupleA(
                attention = attention,
                openInstallments = openInstallments,
                fixedCostMonthlyCents = fixedCost,
                cardsUsagePercentage = usagePercentage,
                cardsTotalUsedCents = totalUsedCents,
                cardsTotalLimitCents = totalLimitCents
            )
        }

        // Fluxo B: Metas, série de 12 meses e compromissos que encerram
        val flowB = combine(
            goalRepository.getGoals(),
            financialRepository.getInstallmentsForPeriod(startRange, endRange),
            financialRepository.getFreeingUpCommitments()
        ) { goals, installments, freeingUpRaw ->
            val totalCurrentCents = goals.sumOf { it.currentAmountCents }
            val totalTargetCents = goals.sumOf { it.targetAmountCents }
            val reservePercentage = if (goals.isNotEmpty() && totalTargetCents > 0L) {
                ((totalCurrentCents.toDouble() / totalTargetCents.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else null

            val monthlySeries = build12MonthSeries(nowMillis, installments)
            val freeingUp = freeingUpRaw.take(5)

            TupleB(
                goalsReservePercentage = reservePercentage,
                monthlySeries = monthlySeries,
                freeingUpCommitments = freeingUp
            )
        }

        return combine(flowA, flowB) { a, b ->
            PortfolioOverview(
                overdueCount = a.attention.overdueCount,
                dueTodayCount = a.attention.dueTodayCount,
                openInstallmentsAmountCents = a.openInstallments.totalAmountCents,
                openInstallmentsCount = a.openInstallments.count,
                lastInstallmentDueDate = a.openInstallments.lastDueDate,
                fixedCostMonthlyCents = a.fixedCostMonthlyCents,
                cardsUsagePercentage = a.cardsUsagePercentage,
                cardsTotalUsedCents = a.cardsTotalUsedCents,
                cardsTotalLimitCents = a.cardsTotalLimitCents,
                goalsReservePercentage = b.goalsReservePercentage,
                monthlySeries = b.monthlySeries,
                freeingUpCommitments = b.freeingUpCommitments
            )
        }
    }

    private fun build12MonthSeries(
        nowMillis: Long,
        installments: List<com.platform.app.domain.model.BillInstallment>
    ): List<MonthlyBarData> {
        val monthFormat = SimpleDateFormat("MMM", Locale("pt", "BR"))
        val result = mutableListOf<MonthlyBarData>()

        for (offset in -2..9) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.MONTH, offset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMillis = cal.timeInMillis
            val endMillis = cal.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val monthLabel = monthFormat.format(startMillis).uppercase().replace(".", "")
            val monthTotal = installments.filter {
                it.dueDate in startMillis..endMillis && !it.isSkipped && !it.isPaused
            }.sumOf { it.amountCents }

            result.add(
                MonthlyBarData(
                    monthTimestamp = startMillis,
                    label = monthLabel,
                    amountCents = monthTotal,
                    isPast = offset < 0,
                    isCurrent = offset == 0,
                    isPeak = false,
                    isLowestFuture = false
                )
            )
        }

        // Identifica pico e menor valor entre os 9 meses futuros (offset 1..9)
        val futureBars = result.filter { !it.isPast && !it.isCurrent }
        val maxFutureAmount = futureBars.map { it.amountCents }.filter { it > 0L }.maxOrNull()
        val minFutureAmount = futureBars.map { it.amountCents }.minOrNull()

        return result.map { bar ->
            val isFuture = !bar.isPast && !bar.isCurrent
            val isPeak = isFuture && maxFutureAmount != null && bar.amountCents == maxFutureAmount && bar.amountCents > 0L
            val isLowest = isFuture && minFutureAmount != null && bar.amountCents == minFutureAmount && !isPeak
            bar.copy(
                isPeak = isPeak,
                isLowestFuture = isLowest
            )
        }
    }

    private data class TupleA(
        val attention: com.platform.app.domain.model.AttentionCounts,
        val openInstallments: com.platform.app.domain.model.OpenInstallmentsSummary,
        val fixedCostMonthlyCents: Long,
        val cardsUsagePercentage: Int?,
        val cardsTotalUsedCents: Long,
        val cardsTotalLimitCents: Long
    )

    private data class TupleB(
        val goalsReservePercentage: Int?,
        val monthlySeries: List<MonthlyBarData>,
        val freeingUpCommitments: List<FreeingUpCommitment>
    )
}
