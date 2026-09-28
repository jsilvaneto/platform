package com.platform.app.presentation.bills

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory

enum class BillPeriodFilter {
    THIS_MONTH,
    NEXT_30_DAYS,
    OVERDUE,
    ALL
}

data class BillsUiState(
    val selectedMonthMillis: Long = System.currentTimeMillis(),
    val periodFilter: BillPeriodFilter = BillPeriodFilter.THIS_MONTH,
    val installments: List<BillInstallment> = emptyList(),
    val filteredInstallments: List<BillInstallment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val subcategories: List<Subcategory> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val financialAccounts: List<FinancialAccount> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val typeFilter: BillType? = null,
    val statusFilter: BillStatus? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
