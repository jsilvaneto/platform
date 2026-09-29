package com.platform.app.presentation.dashboard

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.FinancialDashboardMetrics

enum class DashboardViewMode {
    OVERVIEW,       // Diagnóstico Geral / Categorias / Contas
    PREDICTIONS     // Projeção Preditiva (Curva de Desoneração 6-12 meses)
}

enum class StatisticsTab(val title: String) {
    PAST("Passado"),
    PRESENT("Presente"),
    FUTURE("Futuro")
}

data class DashboardUiState(
    val selectedMonthMillis: Long = System.currentTimeMillis(),
    val metrics: FinancialDashboardMetrics? = null,
    val viewMode: DashboardViewMode = DashboardViewMode.OVERVIEW,
    val selectedTab: StatisticsTab = StatisticsTab.PRESENT,
    val isPrivacyMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
