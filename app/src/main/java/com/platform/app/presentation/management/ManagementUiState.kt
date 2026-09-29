package com.platform.app.presentation.management

import com.platform.app.core.mvi.UiState
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod

data class ManagementUiState(
    val selectedTab: Int = 0,
    val accounts: List<FinancialAccount> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val categories: List<Category> = emptyList(),
    val installments: List<BillInstallment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
