package com.platform.app.presentation.bills

import com.platform.app.core.mvi.UiAction
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType

sealed interface BillsUiAction : UiAction {
    data class TogglePayment(val installment: BillInstallment) : BillsUiAction
    data class DeleteBill(val billId: String) : BillsUiAction
    data class SearchQueryChanged(val query: String) : BillsUiAction
    data class TypeFilterChanged(val type: BillType?) : BillsUiAction
    data class StatusFilterChanged(val status: BillStatus?) : BillsUiAction
    data class PeriodFilterChanged(val period: BillPeriodFilter) : BillsUiAction
    data class YearChanged(val year: Int?) : BillsUiAction
    data class PayBatch(val installmentIds: List<String>) : BillsUiAction
    data class DeleteBatch(val billIds: List<String>) : BillsUiAction
    data class OpenEditInstallment(val installment: BillInstallment) : BillsUiAction
    object DismissEditInstallment : BillsUiAction
    data class SaveInstallmentEdit(
        val installmentId: String,
        val billId: String,
        val title: String,
        val description: String,
        val amountCents: Long,
        val dueDate: Long,
        val categoryId: String?,
        val itemId: String?,
        val contactId: String?,
        val financialAccountId: String?,
        val paymentMethodId: String?
    ) : BillsUiAction
    object Refresh : BillsUiAction
}
