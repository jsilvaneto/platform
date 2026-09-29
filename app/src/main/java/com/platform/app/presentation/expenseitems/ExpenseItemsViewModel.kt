package com.platform.app.presentation.expenseitems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseItemsViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseItemsUiState())
    val uiState: StateFlow<ExpenseItemsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialExpenseItemsIfEmpty()
        }
        loadData()
    }

    fun onAction(action: ExpenseItemsUiAction) {
        when (action) {
            is ExpenseItemsUiAction.SearchQueryChanged -> {
                _uiState.update { current ->
                    val filtered = applyFilters(current.items, action.query, current.selectedCategoryId)
                    current.copy(searchQuery = action.query, filteredItems = filtered)
                }
            }
            is ExpenseItemsUiAction.CategoryFilterChanged -> {
                _uiState.update { current ->
                    val filtered = applyFilters(current.items, current.searchQuery, action.categoryId)
                    current.copy(selectedCategoryId = action.categoryId, filteredItems = filtered)
                }
            }
            is ExpenseItemsUiAction.SaveItem -> {
                viewModelScope.launch {
                    repository.saveExpenseItem(action.item)
                }
            }
            is ExpenseItemsUiAction.DeleteItem -> {
                viewModelScope.launch {
                    repository.deleteExpenseItem(action.itemId)
                }
            }
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            repository.getExpenseItems(),
            repository.getCategories()
        ) { items, categories ->
            val current = _uiState.value
            val filtered = applyFilters(items, current.searchQuery, current.selectedCategoryId)
            _uiState.update {
                it.copy(
                    items = items,
                    filteredItems = filtered,
                    categories = categories,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun applyFilters(
        items: List<ExpenseItem>,
        query: String,
        categoryId: String?
    ): List<ExpenseItem> {
        return items.filter { item ->
            val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true) || item.categoryName.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || item.categoryId == categoryId
            matchesQuery && matchesCategory
        }
    }
}
