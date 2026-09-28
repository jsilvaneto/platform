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
    val futureMonthsProjections: List<FutureMonthProjection> = emptyList(),
    val totalCommittedFutureCents: Long = 0L,
    val futureInstallmentsCount: Int = 0,
    val fixedMonthlyTotalCents: Long = 0L,
    val activeRecurringCount: Int = 0,
    val totalInstallmentsRemainingCents: Long = 0L,
    val activeInstallmentsCount: Int = 0,
    val accountsDistribution: List<AccountSpend> = emptyList()
)

data class CategorySpend(
    val categoryName: String,
    val colorHex: String,
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
    val installmentsCount: Int
)
