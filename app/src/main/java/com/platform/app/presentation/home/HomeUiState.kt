package com.platform.app.presentation.home

import com.platform.app.domain.usecase.MonthlyForecastResult

data class HomeUiState(
    val selectedMonthMillis: Long = System.currentTimeMillis(),
    val forecastResult: MonthlyForecastResult? = null,
    val isPaidSectionExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
