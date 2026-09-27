package com.platform.app.presentation.recurring

import com.platform.app.core.mvi.UiAction
import com.platform.app.core.mvi.UiEffect
import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType

data class BillWithInstallments(
    val bill: Bill,
    val installments: List<BillInstallment>,
    val paidInstallmentsCount: Int,
    val totalPaidCents: Long,
    val remainingCents: Long,
    val progress: Float,
    val nextInstallment: BillInstallment?
)

data class RecurringInstallmentsUiState(
    val selectedFilter: BillType? = null, // null = Todos, INSTALLMENT, RECURRING
    val items: List<BillWithInstallments> = emptyList(),
    val filteredItems: List<BillWithInstallments> = emptyList(),
    val totalActiveInstallmentsCents: Long = 0L,
    val totalMonthlyRecurringCents: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface RecurringInstallmentsUiAction : UiAction {
    data class SelectFilter(val type: BillType?) : RecurringInstallmentsUiAction
    data class TogglePayment(val installmentId: String, val currentPaid: Boolean) : RecurringInstallmentsUiAction
    data class DeleteBill(val billId: String) : RecurringInstallmentsUiAction
    object Refresh : RecurringInstallmentsUiAction
}

sealed interface RecurringInstallmentsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : RecurringInstallmentsUiEffect
}
