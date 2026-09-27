package com.platform.app.domain.model

data class FinancialDashboardMetrics(
    val monthMillis: Long,
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
    val futureInstallmentsCount: Int = 0
)

data class CategorySpend(
    val categoryName: String,
    val colorHex: String,
    val amountCents: Long,
    val percentage: Float
)

data class FutureMonthProjection(
    val monthMillis: Long,
    val monthLabel: String,
    val totalCommittedCents: Long,
    val installmentsCount: Int
)
