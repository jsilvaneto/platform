package com.platform.app.presentation.overview

import com.platform.app.domain.model.PortfolioOverview

data class OverviewUiState(
    val isLoading: Boolean = true,
    val overview: PortfolioOverview = PortfolioOverview()
)
