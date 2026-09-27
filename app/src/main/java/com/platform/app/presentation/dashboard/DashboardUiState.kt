package com.platform.app.presentation.dashboard

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.FinancialDashboardMetrics

data class DashboardUiState(
    val selectedMonthMillis: Long = System.currentTimeMillis(),
    val metrics: FinancialDashboardMetrics? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
