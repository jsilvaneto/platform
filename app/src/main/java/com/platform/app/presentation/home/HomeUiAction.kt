package com.platform.app.presentation.home

sealed interface HomeUiAction {
    data object PreviousMonth : HomeUiAction
    data object NextMonth : HomeUiAction
    data object CurrentMonth : HomeUiAction
    data class SelectMonth(val monthMillis: Long) : HomeUiAction
    data class ChangeViewMode(val viewMode: HomeViewMode) : HomeUiAction
    data class SelectCalendarDay(val dayMillis: Long?) : HomeUiAction
    data class ToggleOverdueBanner(val expanded: Boolean) : HomeUiAction
    data class PayBill(val installmentId: String, val actualPaymentDate: Long? = null) : HomeUiAction
    data class UndoPayBill(val installmentId: String) : HomeUiAction
    data class PayInvoice(val invoiceId: String) : HomeUiAction
    data class UndoPayInvoice(val invoiceId: String) : HomeUiAction
    data class TogglePaidSection(val expanded: Boolean) : HomeUiAction
    data object Refresh : HomeUiAction
}
