package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetFinancialDashboardUseCase @Inject constructor(
    private val repository: FinancialRepository
) {
    operator fun invoke(monthMillis: Long): Flow<FinancialDashboardMetrics> {
        val startOfMonth = DateUtils.getStartOfMonth(monthMillis)
        val endOfMonth = DateUtils.getEndOfMonth(monthMillis)
        val now = System.currentTimeMillis()

        return combine(
            repository.getInstallmentsForPeriod(startOfMonth, endOfMonth),
            repository.getAllInstallments()
        ) { installments, allInstallments ->
            var totalDue = 0L
            var totalPaid = 0L
            var totalPending = 0L
            var totalOverdue = 0L

            val categoryMap = mutableMapOf<String, Pair<String, Long>>()

            for (inst in installments) {
                totalDue += inst.amountCents

                if (inst.isPaid) {
                    totalPaid += inst.amountCents
                } else {
                    if (inst.dueDate < now) {
                        totalOverdue += inst.amountCents
                    } else {
                        totalPending += inst.amountCents
                    }
                }

                val currentCategory = categoryMap[inst.categoryName] ?: Pair(inst.categoryColorHex, 0L)
                categoryMap[inst.categoryName] = Pair(inst.categoryColorHex, currentCategory.second + inst.amountCents)
            }

            val categoryDistribution = categoryMap.map { (catName, pair) ->
                val (color, amount) = pair
                val percentage = if (totalDue > 0) (amount.toFloat() / totalDue.toFloat()) * 100f else 0f
                CategorySpend(
                    categoryName = catName,
                    colorHex = color,
                    amountCents = amount,
                    percentage = percentage
                )
            }.sortedByDescending { it.amountCents }

            val upcoming = installments
                .filter { !it.isPaid }
                .sortedBy { it.dueDate }
                .take(5)

            // Vencimentos dos próximos 7 dias
            val sevenDaysAhead = now + (7L * 24 * 3600 * 1000)
            val upcomingWeek = allInstallments
                .filter { !it.isPaid && it.dueDate in now..sevenDaysAhead }
                .sortedBy { it.dueDate }

            // Projeções dos próximos 6 meses para planejamento
            val futureProjections = (1..6).map { i ->
                val futureMonthEpoch = DateUtils.addMonths(monthMillis, i)
                val futureStart = DateUtils.getStartOfMonth(futureMonthEpoch)
                val futureEnd = DateUtils.getEndOfMonth(futureMonthEpoch)
                val monthInsts = allInstallments.filter { it.dueDate in futureStart..futureEnd }
                FutureMonthProjection(
                    monthMillis = futureMonthEpoch,
                    monthLabel = DateUtils.formatMonthYear(futureMonthEpoch),
                    totalCommittedCents = monthInsts.sumOf { it.amountCents },
                    installmentsCount = monthInsts.size
                )
            }

            val totalCommittedFuture = allInstallments
                .filter { !it.isPaid && it.dueDate >= startOfMonth }
                .sumOf { it.amountCents }

            FinancialDashboardMetrics(
                monthMillis = monthMillis,
                totalDueMonthCents = totalDue,
                totalPaidMonthCents = totalPaid,
                totalPendingMonthCents = totalPending,
                totalOverdueMonthCents = totalOverdue,
                upcomingInstallments = upcoming,
                upcomingWeekInstallments = upcomingWeek,
                categoryDistribution = categoryDistribution,
                futureMonthsProjections = futureProjections,
                totalCommittedFutureCents = totalCommittedFuture
            )
        }
    }
}
