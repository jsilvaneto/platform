package com.platform.app.presentation.management

import com.platform.app.core.mvi.UiAction
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory

sealed interface ManagementUiAction : UiAction {
    data class SelectTab(val index: Int) : ManagementUiAction

    // Accounts
    data class SaveAccount(val account: FinancialAccount) : ManagementUiAction
    data class DeleteAccount(val accountId: String) : ManagementUiAction

    // Payment Methods
    data class SavePaymentMethod(val method: PaymentMethod) : ManagementUiAction
    data class DeletePaymentMethod(val methodId: String) : ManagementUiAction

    // Categories & Subcategories
    data class SaveCategory(val category: Category) : ManagementUiAction
    data class DeleteCategory(val categoryId: String) : ManagementUiAction
    data class SaveSubcategory(val subcategory: Subcategory) : ManagementUiAction
    data class DeleteSubcategory(val subcategoryId: String) : ManagementUiAction

    object Refresh : ManagementUiAction
}
