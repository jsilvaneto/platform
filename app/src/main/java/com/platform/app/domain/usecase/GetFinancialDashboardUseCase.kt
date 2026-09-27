package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFinancialDashboardUseCase @Inject constructor(
    private val repository: FinancialRepository
) {
    operator fun invoke(monthMillis: Long): Flow<FinancialDashboardMetrics> {
        val startOfMonth = DateUtils.getStartOfMonth(monthMillis)
        val endOfMonth = DateUtils.getEndOfMonth(monthMillis)
        val now = System.currentTimeMillis()

        return repository.getInstallmentsForPeriod(startOfMonth, endOfMonth).map { installments ->
            var totalDue = 0L
            var totalPaid = 0L
            var totalPending = 0L
            var totalOverdue = 0L

            val categoryMap = mutableMapOf<String, Pair<String, Long>>() // name -> (color, totalCents)

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

                // Agrupamento por Categoria
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

            FinancialDashboardMetrics(
                monthMillis = monthMillis,
                totalDueMonthCents = totalDue,
                totalPaidMonthCents = totalPaid,
                totalPendingMonthCents = totalPending,
                totalOverdueMonthCents = totalOverdue,
                upcomingInstallments = upcoming,
                categoryDistribution = categoryDistribution
            )
        }
    }
}
