package com.platform.app.domain.model

data class FinancialDashboardMetrics(
    val monthMillis: Long,
    val currentMonthLabel: String = "",
    val totalDueMonthCents: Long = 0L,
    val totalPaidMonthCents: Long = 0L,
    val totalPendingMonthCents: Long = 0L,
    val totalOverdueMonthCents: Long = 0L,
    val previousMonthDueCents: Long = 0L,
    val previousMonthPaidCents: Long = 0L,
    val totalHistoricalPaidCents: Long = 0L,
    val onTimePaymentRate: Int = 100,
    val upcomingInstallments: List<BillInstallment> = emptyList(),
    val upcomingWeekInstallments: List<BillInstallment> = emptyList(),
    val categoryDistribution: List<CategorySpend> = emptyList(),
    val natureDistribution: List<NatureSpend> = emptyList(),
    val futureMonthsProjections: List<FutureMonthProjection> = emptyList(),
    val totalCommittedFutureCents: Long = 0L,
    val futureInstallmentsCount: Int = 0,
    val fixedMonthlyTotalCents: Long = 0L,
    val activeRecurringCount: Int = 0,
    val totalInstallmentsRemainingCents: Long = 0L,
    val activeInstallmentsCount: Int = 0,
    val accountsDistribution: List<AccountSpend> = emptyList(),
    // Visão Panorâmica do Passado
    val pastMonthsHistory: List<PastMonthHistory> = emptyList(),
    val historicalMonthlyAverageCents: Long = 0L,
    val highestSpendMonthLabel: String = "",
    val highestSpendMonthCents: Long = 0L,
    val lowestSpendMonthLabel: String = "",
    val lowestSpendMonthCents: Long = 0L,
    // Visão do Presente (Meios de Pagamento e Contatos)
    val creditCardSpendCents: Long = 0L,
    val nonCardSpendCents: Long = 0L,
    val creditCardPercentage: Float = 0f,
    val topContactsSpend: List<ContactSpend> = emptyList(),
    // Visão do Futuro (Previsibilidade e Liberação de Caixa)
    val nextCompletingInstallments: List<CompletingInstallmentSummary> = emptyList(),
    val projectedFreedMonthlyFlowCents: Long = 0L
)

data class PastMonthHistory(
    val monthMillis: Long,
    val monthLabel: String,
    val totalDueCents: Long,
    val totalPaidCents: Long,
    val paidRate: Int
)

data class ContactSpend(
    val contactName: String,
    val amountCents: Long,
    val percentage: Float
)

data class CompletingInstallmentSummary(
    val title: String,
    val finalInstallmentNumber: Int,
    val totalInstallments: Int,
    val completionMonthLabel: String,
    val freedMonthlyAmountCents: Long
)

data class CategorySpend(
    val categoryName: String,
    val colorHex: String,
    val amountCents: Long,
    val percentage: Float
)

data class NatureSpend(
    val nature: ExpenseNature,
    val amountCents: Long,
    val percentage: Float
)

data class AccountSpend(
    val accountName: String,
    val bankName: String,
    val totalAmountCents: Long,
    val pendingBillsCount: Int
)

data class FutureMonthProjection(
    val monthMillis: Long,
    val monthLabel: String,
    val totalCommittedCents: Long,
    val installmentsCount: Int,
    val fixedRecurringCents: Long = 0L,
    val installmentsCents: Long = 0L
)
