package com.platform.app.presentation.dashboard

import com.platform.app.core.mvi.UiAction

sealed interface DashboardUiAction : UiAction {
    object PreviousMonth : DashboardUiAction
    object NextMonth : DashboardUiAction
    object CurrentMonth : DashboardUiAction
    data class SelectMonth(val monthMillis: Long) : DashboardUiAction
    data class SelectTab(val tab: StatisticsTab) : DashboardUiAction
    object TogglePrivacyMode : DashboardUiAction
    data class ChangeViewMode(val mode: DashboardViewMode) : DashboardUiAction
    data class TogglePayment(
        val installmentId: String,
        val currentPaid: Boolean,
        val actualPaymentDate: Long? = null
    ) : DashboardUiAction
    object Refresh : DashboardUiAction
}
