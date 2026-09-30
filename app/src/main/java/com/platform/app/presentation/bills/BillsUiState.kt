package com.platform.app.presentation.bills

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import java.util.Calendar

enum class BillPeriodFilter(val label: String) {
    ALL("Todos"),
    THIS_MONTH("Este Mês"),
    NEXT_30_DAYS("Próx. 30 dias"),
    OVERDUE("Atrasadas")
}

data class BillsUiState(
    val selectedYear: Int? = Calendar.getInstance().get(Calendar.YEAR),
    val availableYears: List<Int> = emptyList(),
    val periodFilter: BillPeriodFilter = BillPeriodFilter.ALL,
    val installments: List<BillInstallment> = emptyList(),
    val filteredInstallments: List<BillInstallment> = emptyList(),
    val categories: List<Category> = emptyList(),
    val expenseItems: List<ExpenseItem> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val financialAccounts: List<FinancialAccount> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val typeFilter: BillType? = null,
    val statusFilter: BillStatus? = null,
    val searchQuery: String = "",
    val editingInstallment: BillInstallment? = null,
    val totalPeriodCents: Long = 0L,
    val paidPeriodCents: Long = 0L,
    val pendingPeriodCents: Long = 0L,
    val overduePeriodCents: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
