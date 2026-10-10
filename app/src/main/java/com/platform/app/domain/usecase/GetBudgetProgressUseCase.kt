package com.platform.app.domain.usecase

import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Budget
import com.platform.app.domain.repository.BudgetRepository
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class BudgetProgressItem(
    val budget: Budget,
    val paidCents: Long,
    val pendingCents: Long,
    val spentCents: Long,
    val progress: Float,
    val isExceeded: Boolean
)

data class BudgetProgressOverview(
    val budgets: List<BudgetProgressItem>,
    val totalLimitCents: Long,
    val totalPaidCents: Long,
    val totalPendingCents: Long,
    val totalSpentCents: Long,
    val overallProgress: Float
)

class GetBudgetProgressUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val financialRepository: FinancialRepository
) {
    operator fun invoke(targetMonthMillis: Long = System.currentTimeMillis()): Flow<BudgetProgressOverview> {
        val startOfMonth = DateUtils.getStartOfMonth(targetMonthMillis)
        val endOfMonth = DateUtils.getEndOfMonth(targetMonthMillis)

        return combine(
            budgetRepository.getBudgets(),
            financialRepository.getAllInstallments()
        ) { budgets, allInstallments ->
            // 1. Filtrar ocorrências não pausadas pertencentes ao mês alvo:
            // - Para compras com cartão (creditCardId ou invoiceId): alocadas na data da compra (createdAt)
            // - Para despesas avulsas/bancárias: alocadas na data de vencimento (dueDate)
            val monthInstallments = allInstallments.filter { inst ->
                if (inst.isPaused) return@filter false
                val effectiveDate = if (inst.creditCardId != null || inst.invoiceId != null) {
                    inst.createdAt
                } else {
                    inst.dueDate
                }
                effectiveDate in startOfMonth..endOfMonth
            }

            // 2. Calcular progresso para cada orçamento configurado
            val items = budgets.map { budget ->
                val matching = monthInstallments.filter { inst ->
                    if (budget.categoryId != null) {
                        inst.categoryId == budget.categoryId
                    } else {
                        true // Teto Geral engloba todas as despesas
                    }
                }
                val paid = matching.filter { it.isPaid }.sumOf { it.amountCents }
                val pending = matching.filter { !it.isPaid }.sumOf { it.amountCents }
                val spent = paid + pending
                val progress = if (budget.limitAmountCents > 0L) {
                    (spent.toFloat() / budget.limitAmountCents.toFloat()).coerceIn(0f, 2f)
                } else {
                    0f
                }

                BudgetProgressItem(
                    budget = budget,
                    paidCents = paid,
                    pendingCents = pending,
                    spentCents = spent,
                    progress = progress,
                    isExceeded = spent > budget.limitAmountCents
                )
            }

            // 3. Regra de Teto Geral vs Sublimites por Categoria:
            // Se houver Teto Geral, ele representa o total global; tetos de categoria não somam
            val generalBudget = budgets.find { it.categoryId == null }
            val totalLimit: Long
            val totalPaid: Long
            val totalPending: Long
            val totalSpent: Long

            if (generalBudget != null) {
                totalLimit = generalBudget.limitAmountCents
                totalPaid = monthInstallments.filter { it.isPaid }.sumOf { it.amountCents }
                totalPending = monthInstallments.filter { !it.isPaid }.sumOf { it.amountCents }
                totalSpent = totalPaid + totalPending
            } else {
                totalLimit = budgets.sumOf { it.limitAmountCents }
                val budgetedCategoryIds = budgets.mapNotNull { it.categoryId }.toSet()
                val relevantInstallments = monthInstallments.filter { it.categoryId in budgetedCategoryIds }
                totalPaid = relevantInstallments.filter { it.isPaid }.sumOf { it.amountCents }
                totalPending = relevantInstallments.filter { !it.isPaid }.sumOf { it.amountCents }
                totalSpent = totalPaid + totalPending
            }

            val overallProgress = if (totalLimit > 0L) {
                (totalSpent.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            BudgetProgressOverview(
                budgets = items,
                totalLimitCents = totalLimit,
                totalPaidCents = totalPaid,
                totalPendingCents = totalPending,
                totalSpentCents = totalSpent,
                overallProgress = overallProgress
            )
        }
    }
}
