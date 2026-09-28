package com.platform.app.presentation.bills

import com.platform.app.core.mvi.UiAction
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType

sealed interface BillsUiAction : UiAction {
    data class CreateBill(
        val title: String,
        val description: String,
        val type: BillType,
        val totalAmountCents: Long,
        val categoryId: String?,
        val subcategoryId: String? = null,
        val contactId: String? = null,
        val financialAccountId: String? = null,
        val paymentMethodId: String? = null,
        val totalInstallments: Int,
        val firstDueDate: Long
    ) : BillsUiAction

    data class TogglePayment(val installment: BillInstallment) : BillsUiAction
    data class DeleteBill(val billId: String) : BillsUiAction
    data class SearchQueryChanged(val query: String) : BillsUiAction
    data class TypeFilterChanged(val type: BillType?) : BillsUiAction
    data class StatusFilterChanged(val status: BillStatus?) : BillsUiAction
    data class PeriodFilterChanged(val period: BillPeriodFilter) : BillsUiAction
    data class MonthChanged(val monthMillis: Long) : BillsUiAction
    object Refresh : BillsUiAction
}
