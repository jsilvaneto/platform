package com.platform.app.presentation.home

import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.usecase.MonthlyForecastResult

enum class HomeViewMode {
    LIST,
    CALENDAR
}

data class CalendarDayItem(
    val dateMillis: Long,
    val dayOfMonth: Int,
    val dayOfWeekLabel: String,
    val hasOverdue: Boolean = false,
    val hasDueToday: Boolean = false,
    val hasPending: Boolean = false,
    val isFullyPaid: Boolean = false,
    val totalAmountCents: Long = 0L,
    val itemsCount: Int = 0
)

data class HomeUiState(
    val viewMode: HomeViewMode = HomeViewMode.LIST,
    val selectedMonthMillis: Long = System.currentTimeMillis(),
    val selectedCalendarDayMillis: Long? = null,
    val forecastResult: MonthlyForecastResult? = null,
    val dashboardMetrics: FinancialDashboardMetrics? = null,
    val globalOverdueItems: List<PayableItem> = emptyList(),
    val globalOverdueTotalCents: Long = 0L,
    val next30DaysItems: List<PayableItem> = emptyList(),
    val next30DaysTotalCents: Long = 0L,
    val calendarDays: List<CalendarDayItem> = emptyList(),
    val daySelectedItems: List<PayableItem> = emptyList(),
    val isOverdueBannerExpanded: Boolean = true,
    val isPaidSectionExpanded: Boolean = false,
    val allExpenseItems: List<com.platform.app.domain.model.ExpenseItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
