package com.platform.app.presentation.dashboard

import com.platform.app.core.mvi.UiAction

sealed interface DashboardUiAction : UiAction {
    object PreviousMonth : DashboardUiAction
    object NextMonth : DashboardUiAction
    object CurrentMonth : DashboardUiAction
    object Refresh : DashboardUiAction
}
