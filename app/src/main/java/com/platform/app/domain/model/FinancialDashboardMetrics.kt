package com.platform.app.domain.model

data class FinancialDashboardMetrics(
    val monthMillis: Long,
    val totalDueMonthCents: Long = 0L,
    val totalPaidMonthCents: Long = 0L,
    val totalPendingMonthCents: Long = 0L,
    val totalOverdueMonthCents: Long = 0L,
    val upcomingInstallments: List<BillInstallment> = emptyList(),
    val categoryDistribution: List<CategorySpend> = emptyList()
)

data class CategorySpend(
    val categoryName: String,
    val colorHex: String,
    val amountCents: Long,
    val percentage: Float
)
