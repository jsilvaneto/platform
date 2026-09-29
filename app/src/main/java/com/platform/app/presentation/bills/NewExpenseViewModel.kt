package com.platform.app.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NewExpenseUiState(
    val description: String = "",
    val amountCents: Long = 0L,
    val dueDate: Long = System.currentTimeMillis(),
    val selectedCategoryId: String? = null,
    val selectedItemId: String? = null,
    val selectedCreditCardId: String? = null,
    val isRecurring: Boolean = false,
    val categories: List<Category> = emptyList(),
    val allExpenseItems: List<ExpenseItem> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
) {
    val filteredItems: List<ExpenseItem>
        get() = if (selectedCategoryId != null) {
            allExpenseItems.filter { it.categoryId == selectedCategoryId }
        } else {
            allExpenseItems
        }

    val selectedCategory: Category?
        get() = categories.find { it.id == selectedCategoryId }

    val selectedItem: ExpenseItem?
        get() = allExpenseItems.find { it.id == selectedItemId }

    val inheritedNature: ExpenseNature
        get() = selectedItem?.nature ?: selectedCategory?.nature ?: ExpenseNature.NECESSARIO

    val isValid: Boolean
        get() = description.isNotBlank() && amountCents > 0L
}

sealed interface NewExpenseUiEffect {
    data object ExpenseSaved : NewExpenseUiEffect
    data class ShowError(val message: String) : NewExpenseUiEffect
}

@HiltViewModel
class NewExpenseViewModel @Inject constructor(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewExpenseUiState())
    val uiState: StateFlow<NewExpenseUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NewExpenseUiEffect>()
    val uiEffect: SharedFlow<NewExpenseUiEffect> = _uiEffect.asSharedFlow()

    init {
        combine(
            repository.getCategories(),
            repository.getExpenseItems(),
            repository.getCreditCards()
        ) { categories, items, cards ->
            _uiState.update {
                it.copy(
                    categories = categories,
                    allExpenseItems = items,
                    creditCards = cards
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onAmountChange(amountCents: Long) {
        _uiState.update { it.copy(amountCents = amountCents) }
    }

    fun onDueDateChange(dueDate: Long) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }

    fun onCategorySelect(categoryId: String?) {
        _uiState.update { state ->
            val updatedItemId = if (state.selectedItemId != null) {
                val item = state.allExpenseItems.find { it.id == state.selectedItemId }
                if (item?.categoryId == categoryId) state.selectedItemId else null
            } else null

            state.copy(selectedCategoryId = categoryId, selectedItemId = updatedItemId)
        }
    }

    fun onItemSelect(itemId: String?) {
        _uiState.update { state ->
            if (itemId == null) {
                state.copy(selectedItemId = null)
            } else {
                val item = state.allExpenseItems.find { it.id == itemId }
                state.copy(
                    selectedItemId = itemId,
                    selectedCategoryId = item?.categoryId ?: state.selectedCategoryId
                )
            }
        }
    }

    fun onCreditCardSelect(cardId: String?) {
        _uiState.update { it.copy(selectedCreditCardId = cardId) }
    }

    fun saveExpense() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            try {
                val billId = UUID.randomUUID().toString()
                val installmentId = UUID.randomUUID().toString()

                val bill = Bill(
                    id = billId,
                    title = state.description.trim(),
                    description = state.description.trim(),
                    type = if (state.isRecurring) BillType.RECURRING else BillType.SINGLE,
                    totalAmountCents = state.amountCents,
                    categoryId = state.selectedCategoryId,
                    itemId = state.selectedItemId,
                    invoiceId = null,
                    totalInstallments = 1,
                    createdAt = System.currentTimeMillis()
                )

                val installment = BillInstallment(
                    id = installmentId,
                    billId = billId,
                    billTitle = state.description.trim(),
                    categoryId = state.selectedCategoryId,
                    categoryName = state.selectedCategory?.name ?: "Geral",
                    categoryColorHex = state.selectedCategory?.colorHex ?: "#64748B",
                    nature = state.inheritedNature,
                    itemId = state.selectedItemId,
                    itemName = state.selectedItem?.name,
                    invoiceId = null,
                    installmentNumber = 1,
                    totalInstallments = 1,
                    amountCents = state.amountCents,
                    dueDate = state.dueDate,
                    status = BillStatus.PENDING,
                    type = bill.type
                )

                repository.saveBillWithInstallments(bill, listOf(installment))
                _uiState.update { it.copy(isSaved = true) }
                _uiEffect.emit(NewExpenseUiEffect.ExpenseSaved)
            } catch (e: Exception) {
                _uiEffect.emit(NewExpenseUiEffect.ShowError(e.localizedMessage ?: "Erro ao salvar despesa."))
            }
        }
    }
}
