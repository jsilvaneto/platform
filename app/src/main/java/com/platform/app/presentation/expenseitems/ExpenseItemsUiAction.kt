package com.platform.app.presentation.expenseitems

import com.platform.app.domain.model.ExpenseItem

sealed interface ExpenseItemsUiAction {
    data class SearchQueryChanged(val query: String) : ExpenseItemsUiAction
    data class CategoryFilterChanged(val categoryId: String?) : ExpenseItemsUiAction
    data class SaveItem(val item: ExpenseItem) : ExpenseItemsUiAction
    data class DeleteItem(val itemId: String) : ExpenseItemsUiAction
}
