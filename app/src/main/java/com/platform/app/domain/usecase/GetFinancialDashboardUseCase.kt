package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.AccountSpend
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.BudgetRigidityCalculator
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.domain.model.NatureSpend
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetFinancialDashboardUseCase @Inject constructor(
    private val repository: FinancialRepository,
    private val goalRepository: GoalRepository
) {
    operator fun invoke(monthMillis: Long): Flow<FinancialDashboardMetrics> {
        val startOfMonth = DateUtils.getStartOfMonth(monthMillis)
        val endOfMonth = DateUtils.getEndOfMonth(monthMillis)
        val now = System.currentTimeMillis()

        return combine(
            repository.getInstallmentsForPeriod(startOfMonth, endOfMonth),
            repository.getAllInstallments(),
            repository.getBills(),
            repository.getFinancialAccounts(),
            goalRepository.getMonthlyContribution(startOfMonth, endOfMonth)
        ) { installments, allInstallments, bills, accounts, monthlySavingsCents ->
            var totalDue = 0L
            var totalPaid = 0L
            var totalPending = 0L
            var totalOverdue = 0L

            val categoryMap = mutableMapOf<String, Pair<String, Long>>()

            for (inst in installments) {
                if (inst.isPaused) continue
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

            val savingsCents = monthlySavingsCents.coerceAtLeast(0L)
            val totalBudget = totalDue + savingsCents

            val activeInstallmentsForMonth = installments.filter { !it.isPaused }
            val natureMap = activeInstallmentsForMonth.groupBy { it.nature }
            val natureDistribution = natureMap.map { (nature, instList) ->
                val amount = instList.sumOf { it.amountCents }
                val percentage = if (totalBudget > 0) (amount.toFloat() / totalBudget.toFloat()) * 100f else 0f
                NatureSpend(nature = nature, amountCents = amount, percentage = percentage)
            }.sortedByDescending { it.amountCents }

            val savingsPercentage = if (totalBudget > 0) (savingsCents.toFloat() / totalBudget.toFloat()) * 100f else 0f

            val mandatoryAmount = natureDistribution.firstOrNull { it.nature == ExpenseNature.OBRIGATORIO }?.amountCents ?: 0L
            val necessaryAmount = natureDistribution.firstOrNull { it.nature == ExpenseNature.NECESSARIO }?.amountCents ?: 0L
            val wantsAmount = natureDistribution.firstOrNull { it.nature == ExpenseNature.DESEJA }?.amountCents ?: 0L
            val noneAmount = natureDistribution.firstOrNull { it.nature == ExpenseNature.NENHUM }?.amountCents ?: 0L

            val budgetRigidity = BudgetRigidityCalculator.calculate(
                mandatoryAmountCents = mandatoryAmount,
                necessaryAmountCents = necessaryAmount,
                wantsAmountCents = wantsAmount,
                noneAmountCents = noneAmount,
                savingsAmountCents = savingsCents
            )

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
                val recurringCents = monthInsts.filter { it.type == BillType.RECURRING }.sumOf { it.amountCents }
                val instCents = monthInsts.filter { it.type == BillType.INSTALLMENT }.sumOf { it.amountCents }

                FutureMonthProjection(
                    monthMillis = futureMonthEpoch,
                    monthLabel = DateUtils.formatMonthYear(futureMonthEpoch),
                    totalCommittedCents = monthInsts.sumOf { it.amountCents },
                    installmentsCount = monthInsts.size,
                    fixedRecurringCents = recurringCents,
                    installmentsCents = instCents
                )
            }

            val totalCommittedFuture = allInstallments
                .filter { !it.isPaid && it.dueDate >= startOfMonth }
                .sumOf { it.amountCents }

            val futureInstallmentsCount = allInstallments
                .count { !it.isPaid && it.dueDate > endOfMonth }

            // Métricas do Mês Anterior (Passado Imediato)
            val prevMonthEpoch = DateUtils.addMonths(monthMillis, -1)
            val prevStart = DateUtils.getStartOfMonth(prevMonthEpoch)
            val prevEnd = DateUtils.getEndOfMonth(prevMonthEpoch)
            val prevMonthInsts = allInstallments.filter { it.dueDate in prevStart..prevEnd }
            val prevDue = prevMonthInsts.sumOf { it.amountCents }
            val prevPaid = prevMonthInsts.filter { it.isPaid }.sumOf { it.amountCents }

            // Histórico Panorâmico dos Últimos 6 Meses (-5 a 0 relativo ao mês selecionado)
            val pastMonths = (-5..0).map { offset ->
                val pastEpoch = DateUtils.addMonths(monthMillis, offset)
                val pStart = DateUtils.getStartOfMonth(pastEpoch)
                val pEnd = DateUtils.getEndOfMonth(pastEpoch)
                val pInsts = allInstallments.filter { it.dueDate in pStart..pEnd }
                val pDue = pInsts.sumOf { it.amountCents }
                val pPaid = pInsts.filter { it.isPaid }.sumOf { it.amountCents }
                val pRate = if (pDue > 0) ((pPaid.toFloat() / pDue.toFloat()) * 100).toInt() else 0
                com.platform.app.domain.model.PastMonthHistory(
                    monthMillis = pastEpoch,
                    monthLabel = DateUtils.formatShortMonthYear(pastEpoch),
                    totalDueCents = pDue,
                    totalPaidCents = pPaid,
                    paidRate = pRate
                )
            }

            val monthsWithSpend = pastMonths.filter { it.totalDueCents > 0 }
            val avgMonthlySpend = if (monthsWithSpend.isNotEmpty()) {
                monthsWithSpend.sumOf { it.totalDueCents } / monthsWithSpend.size
            } else totalDue

            val highestMonth = pastMonths.maxByOrNull { it.totalDueCents }
            val lowestMonth = monthsWithSpend.minByOrNull { it.totalDueCents }

            // Histórico Total e Pontualidade
            val allPaidInsts = allInstallments.filter { it.isPaid }
            val totalHistPaid = allPaidInsts.sumOf { it.amountCents }
            val onTimeCount = allPaidInsts.count {
                val effectivePaymentDate = it.actualPaymentDate ?: it.paidAt ?: it.dueDate
                effectivePaymentDate <= it.dueDate || DateUtils.getStartOfDay(effectivePaymentDate) <= DateUtils.getStartOfDay(it.dueDate)
            }
            val onTimeRate = if (allPaidInsts.isNotEmpty()) {
                ((onTimeCount.toFloat() / allPaidInsts.size.toFloat()) * 100).toInt()
            } else 100

            // Divisão por Meio de Pagamento no Mês Selecionado (Cartão vs Outros)
            val creditCardSpend = installments.filter { it.invoiceId != null }.sumOf { it.amountCents }
            val nonCardSpend = (totalDue - creditCardSpend).coerceAtLeast(0L)
            val creditCardPct = if (totalDue > 0) (creditCardSpend.toFloat() / totalDue.toFloat()) * 100f else 0f

            // Distribuição Detalhada por Forma de Pagamento com Porcentagens
            val paymentMethodsGroup = installments.groupBy { inst ->
                when {
                    !inst.paymentMethodName.isNullOrBlank() -> inst.paymentMethodName.trim()
                    inst.invoiceId != null -> "Cartão de Crédito"
                    else -> "Outros / Não Definido"
                }
            }
            val paymentMethodsDistribution = paymentMethodsGroup.map { (methodName, instList) ->
                val amount = instList.sumOf { it.amountCents }
                val pct = if (totalDue > 0) (amount.toFloat() / totalDue.toFloat()) * 100f else 0f
                com.platform.app.domain.model.PaymentMethodSpend(
                    methodName = methodName,
                    amountCents = amount,
                    percentage = pct,
                    count = instList.size
                )
            }.sortedByDescending { it.amountCents }

            // Top Contatos / Fornecedores no Mês Selecionado
            val contactsGroup = installments.groupBy {
                it.contactName?.trim()?.takeIf { name -> name.isNotEmpty() } ?: "Diversos / Sem Contato"
            }
            val topContacts = contactsGroup.map { (contactName, instList) ->
                val amount = instList.sumOf { it.amountCents }
                val pct = if (totalDue > 0) (amount.toFloat() / totalDue.toFloat()) * 100f else 0f
                com.platform.app.domain.model.ContactSpend(
                    contactName = contactName,
                    amountCents = amount,
                    percentage = pct
                )
            }.sortedByDescending { it.amountCents }.take(5)

            // Cronograma de Parcelamentos que Concluem nos Próximos 6 Meses (Liberação de Caixa)
            val installmentBills = allInstallments
                .filter { it.type == BillType.INSTALLMENT && it.totalInstallments > 1 }
                .groupBy { it.billId }

            val sixMonthsAheadEnd = DateUtils.getEndOfMonth(DateUtils.addMonths(monthMillis, 6))
            val completingList = mutableListOf<com.platform.app.domain.model.CompletingInstallmentSummary>()

            installmentBills.forEach { (_, instList) ->
                val maxInst = instList.maxByOrNull { it.installmentNumber }
                if (maxInst != null && maxInst.installmentNumber == maxInst.totalInstallments) {
                    if (maxInst.dueDate in startOfMonth..sixMonthsAheadEnd) {
                        completingList.add(
                            com.platform.app.domain.model.CompletingInstallmentSummary(
                                title = maxInst.billTitle,
                                finalInstallmentNumber = maxInst.installmentNumber,
                                totalInstallments = maxInst.totalInstallments,
                                completionMonthLabel = DateUtils.formatMonthYear(maxInst.dueDate),
                                freedMonthlyAmountCents = maxInst.amountCents
                            )
                        )
                    }
                }
            }
            val nextCompleting = completingList.sortedBy { it.completionMonthLabel }.take(6)
            val freedFlow = nextCompleting.sumOf { it.freedMonthlyAmountCents }

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
                    bankName = acc.accountType.displayName,
                    totalAmountCents = accPending.sumOf { it.amountCents },
                    pendingBillsCount = accPending.size,
                    accountType = acc.accountType
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
                savingsCents = savingsCents,
                savingsPercentage = savingsPercentage,
                budgetRigidity = budgetRigidity,
                futureMonthsProjections = futureProjections,
                totalCommittedFutureCents = totalCommittedFuture,
                futureInstallmentsCount = futureInstallmentsCount,
                fixedMonthlyTotalCents = fixedMonthlyTotal,
                activeRecurringCount = activeRecurring,
                totalInstallmentsRemainingCents = totalInstallmentsRemaining,
                activeInstallmentsCount = activeInstallments,
                accountsDistribution = accountsDistribution,
                pastMonthsHistory = pastMonths,
                historicalMonthlyAverageCents = avgMonthlySpend,
                highestSpendMonthLabel = highestMonth?.monthLabel.orEmpty(),
                highestSpendMonthCents = highestMonth?.totalDueCents ?: 0L,
                lowestSpendMonthLabel = lowestMonth?.monthLabel.orEmpty(),
                lowestSpendMonthCents = lowestMonth?.totalDueCents ?: 0L,
                creditCardSpendCents = creditCardSpend,
                nonCardSpendCents = nonCardSpend,
                creditCardPercentage = creditCardPct,
                paymentMethodsDistribution = paymentMethodsDistribution,
                topContactsSpend = topContacts,
                nextCompletingInstallments = nextCompleting,
                projectedFreedMonthlyFlowCents = freedFlow
            )
        }
    }
}
