package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.AccountSpend
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.domain.model.NatureSpend
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
            repository.getAllInstallments(),
            repository.getBills(),
            repository.getFinancialAccounts()
        ) { installments, allInstallments, bills, accounts ->
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

            val natureMap = installments.groupBy { it.nature }
            val natureDistribution = natureMap.map { (nature, instList) ->
                val amount = instList.sumOf { it.amountCents }
                val percentage = if (totalDue > 0) (amount.toFloat() / totalDue.toFloat()) * 100f else 0f
                NatureSpend(nature = nature, amountCents = amount, percentage = percentage)
            }.sortedByDescending { it.amountCents }

            val upcoming = installments
                .filter { !it.isPaid }
                .sortedBy { it.dueDate }
                .take(5)

            // Vencimentos dos próximos 7 dias (ação rápida de liquidação)
            val sevenDaysAhead = now + (7L * 24 * 3600 * 1000)
            val upcomingWeek = allInstallments
                .filter { !it.isPaid && it.dueDate in (now - 24 * 3600 * 1000)..sevenDaysAhead }
                .sortedBy { it.dueDate }

            // Projeções dos próximos 6 a 12 meses para planejamento futuro (Curva de Desoneração)
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

            val futureInstallmentsCount = allInstallments
                .count { !it.isPaid && it.dueDate > endOfMonth }

            // Métricas do Mês Anterior (Passado)
            val prevMonthEpoch = DateUtils.addMonths(monthMillis, -1)
            val prevStart = DateUtils.getStartOfMonth(prevMonthEpoch)
            val prevEnd = DateUtils.getEndOfMonth(prevMonthEpoch)
            val prevMonthInsts = allInstallments.filter { it.dueDate in prevStart..prevEnd }
            val prevDue = prevMonthInsts.sumOf { it.amountCents }
            val prevPaid = prevMonthInsts.filter { it.isPaid }.sumOf { it.amountCents }

            // Histórico Total e Pontualidade
            val allPaidInsts = allInstallments.filter { it.isPaid }
            val totalHistPaid = allPaidInsts.sumOf { it.amountCents }
            val onTimeCount = allPaidInsts.count { (it.paidAt ?: it.dueDate) <= it.dueDate }
            val onTimeRate = if (allPaidInsts.isNotEmpty()) {
                ((onTimeCount.toFloat() / allPaidInsts.size.toFloat()) * 100).toInt()
            } else 100

            // Custo Fixo Recorrente Mensal (assinaturas e contas fixas ativas)
            val fixedMonthlyTotal = bills
                .filter { it.type == BillType.RECURRING }
                .sumOf { it.totalAmountCents }
            val activeRecurring = bills.count { it.type == BillType.RECURRING }

            // Saldo Devedor Parcelado Total (todas as parcelas ainda não pagas de compras parceladas)
            val totalInstallmentsRemaining = allInstallments
                .filter { !it.isPaid && it.type == BillType.INSTALLMENT }
                .sumOf { it.amountCents }
            val activeInstallments = bills.count { bill ->
                bill.type == BillType.INSTALLMENT && allInstallments.any { it.billId == bill.id && !it.isPaid }
            }

            // Distribuição por Contas Bancárias de Referência
            val accountsDistribution = accounts.map { acc ->
                val accPending = allInstallments.filter { it.financialAccountId == acc.id && !it.isPaid }
                AccountSpend(
                    accountName = acc.name,
                    bankName = acc.accountType,
                    totalAmountCents = accPending.sumOf { it.amountCents },
                    pendingBillsCount = accPending.size
                )
            }.filter { it.pendingBillsCount > 0 }

            FinancialDashboardMetrics(
                monthMillis = monthMillis,
                currentMonthLabel = DateUtils.formatMonthYear(monthMillis),
                totalDueMonthCents = totalDue,
                totalPaidMonthCents = totalPaid,
                totalPendingMonthCents = totalPending,
                totalOverdueMonthCents = totalOverdue,
                previousMonthDueCents = prevDue,
                previousMonthPaidCents = prevPaid,
                totalHistoricalPaidCents = totalHistPaid,
                onTimePaymentRate = onTimeRate,
                upcomingInstallments = upcoming,
                upcomingWeekInstallments = upcomingWeek,
                categoryDistribution = categoryDistribution,
                natureDistribution = natureDistribution,
                futureMonthsProjections = futureProjections,
                totalCommittedFutureCents = totalCommittedFuture,
                futureInstallmentsCount = futureInstallmentsCount,
                fixedMonthlyTotalCents = fixedMonthlyTotal,
                activeRecurringCount = activeRecurring,
                totalInstallmentsRemainingCents = totalInstallmentsRemaining,
                activeInstallmentsCount = activeInstallments,
                accountsDistribution = accountsDistribution
            )
        }
    }
}
