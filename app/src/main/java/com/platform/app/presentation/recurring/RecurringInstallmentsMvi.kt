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
    val nextInstallment: BillInstallment?,
    val estimatedPayoffDate: Long? = null,
    val regularAmountCents: Long = bill.totalAmountCents,
    val hasVariableFirstInstallment: Boolean = false,
    val firstInstallmentAmountCents: Long = bill.totalAmountCents,
    val isPaused: Boolean = bill.isPaused,
    val contactName: String? = null,
    val itemName: String? = null,
    val categoryName: String = "Geral",
    val categoryIconName: String = "category",
    val categoryColorHex: String = "#64748B"
)

data class TimelineMonthSummary(
    val monthLabel: String,
    val timestamp: Long,
    val totalCents: Long,
    val pendingCents: Long,
    val paidCents: Long,
    val installmentsCount: Int,
    val items: List<BillInstallment>
)

enum class RecurringStatusFilter(val label: String) {
    ALL("Todas"),
    ACTIVE("Em Andamento"),
    PAUSED("Pausadas"),
    COMPLETED("Concluídas")
}

data class RecurringInstallmentsUiState(
    val selectedFilter: BillType? = null, // null = Todos, INSTALLMENT, RECURRING
    val statusFilter: RecurringStatusFilter = RecurringStatusFilter.ALL,
    val searchQuery: String = "",
    val items: List<BillWithInstallments> = emptyList(),
    val filteredItems: List<BillWithInstallments> = emptyList(),
    val futureTimeline: List<TimelineMonthSummary> = emptyList(),
    val installmentToAdjust: BillInstallment? = null,
    val totalActiveInstallmentsCents: Long = 0L,
    val totalOriginalFinancedCents: Long = 0L,
    val totalPaidInstallmentsCents: Long = 0L,
    val totalMonthlyRecurringCents: Long = 0L,
    val pendingThisMonthRecurringCents: Long = 0L,
    val paidThisMonthRecurringCents: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState

sealed interface RecurringInstallmentsUiAction : UiAction {
    data class SelectFilter(val type: BillType?) : RecurringInstallmentsUiAction
    data class StatusFilterChanged(val status: RecurringStatusFilter) : RecurringInstallmentsUiAction
    data class SearchQueryChanged(val query: String) : RecurringInstallmentsUiAction
    data class TogglePayment(
        val installmentId: String,
        val currentPaid: Boolean,
        val actualPaymentDate: Long? = null
    ) : RecurringInstallmentsUiAction
    data class DeleteBill(val billId: String) : RecurringInstallmentsUiAction
    data class OpenAdjustInstallment(val installment: BillInstallment) : RecurringInstallmentsUiAction
    object DismissAdjustInstallment : RecurringInstallmentsUiAction
    data class SaveAdjustInstallment(
        val installmentId: String,
        val newAmountCents: Long,
        val newDueDate: Long,
        val applyToFuturePending: Boolean = false
    ) : RecurringInstallmentsUiAction
    data class DeleteSingleInstallment(val installmentId: String) : RecurringInstallmentsUiAction
    data class DeleteFutureInstallments(val billId: String, val fromDueDate: Long) : RecurringInstallmentsUiAction
    data class TogglePauseBill(val billId: String, val isCurrentlyPaused: Boolean) : RecurringInstallmentsUiAction
    data class StopRecurringBill(val billId: String) : RecurringInstallmentsUiAction
    data class UpdateBillMonthlyAmount(
        val billId: String,
        val newAmountCents: Long
    ) : RecurringInstallmentsUiAction
    object Refresh : RecurringInstallmentsUiAction
}

sealed interface RecurringInstallmentsUiEffect : UiEffect {
    data class ShowSnackbar(val message: String) : RecurringInstallmentsUiEffect
}
