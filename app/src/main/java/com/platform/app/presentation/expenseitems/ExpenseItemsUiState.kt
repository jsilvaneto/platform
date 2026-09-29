package com.platform.app.presentation.expenseitems

import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseItem

data class ExpenseItemsUiState(
    val items: List<ExpenseItem> = emptyList(),
    val filteredItems: List<ExpenseItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
