package com.platform.app.presentation.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CreateBillUseCase
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
    val selectedContactId: String? = null,
    val isPaid: Boolean = false,
    val expenseType: BillType = BillType.SINGLE,
    val installmentsCount: Int = 2,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val recurrenceEndType: RecurrenceEndType = RecurrenceEndType.FOREVER,
    val recurrenceEndDate: Long = DateUtils.addMonths(System.currentTimeMillis(), 12),
    val recurrenceOccurrencesCount: Int = 12,
    val selectedPaymentMethodId: String? = null,
    val selectedFinancialAccountId: String? = null,
    val isCreditCard: Boolean = false,
    val selectedCreditCardId: String? = null,
    val categories: List<Category> = emptyList(),
    val allExpenseItems: List<ExpenseItem> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val financialAccounts: List<FinancialAccount> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val creditCardSummaries: Map<String, Long> = emptyMap(), // cardId -> availableLimitCents
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

    val selectedContact: Contact?
        get() = contacts.find { it.id == selectedContactId }

    val selectedPaymentMethod: PaymentMethod?
        get() = paymentMethods.find { it.id == selectedPaymentMethodId }

    val selectedFinancialAccount: FinancialAccount?
        get() = financialAccounts.find { it.id == selectedFinancialAccountId }

    val selectedCreditCard: CreditCard?
        get() = creditCards.find { it.id == selectedCreditCardId }

    val inheritedNature: ExpenseNature
        get() = selectedItem?.nature ?: selectedCategory?.nature ?: ExpenseNature.NECESSARIO

    val availableLimitForSelectedCard: Long?
        get() = selectedCreditCardId?.let { creditCardSummaries[it] }

    val isExceedingCreditLimit: Boolean
        get() = isCreditCard && selectedCreditCard != null && availableLimitForSelectedCard != null && amountCents > (availableLimitForSelectedCard ?: 0L)

    val installmentPreviewAmount: Long
        get() = if (expenseType == BillType.INSTALLMENT && installmentsCount > 0) {
            amountCents / installmentsCount
        } else {
            amountCents
        }

    val effectiveTitle: String
        get() = if (description.isNotBlank()) description.trim() else (selectedItem?.name ?: if (selectedItemId != null) "Despesa" else "")

    val isValid: Boolean
        get() = effectiveTitle.isNotBlank() &&
                amountCents > 0L &&
                selectedItemId != null &&
                selectedContactId != null &&
                (!isCreditCard || selectedCreditCardId != null)
}

sealed interface NewExpenseUiEffect {
    data object ExpenseSaved : NewExpenseUiEffect
    data class ShowError(val message: String) : NewExpenseUiEffect
}

@HiltViewModel
class NewExpenseViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val createBillUseCase: CreateBillUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewExpenseUiState())
    val uiState: StateFlow<NewExpenseUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NewExpenseUiEffect>()
    val uiEffect: SharedFlow<NewExpenseUiEffect> = _uiEffect.asSharedFlow()

    init {
        val duplicateBillId: String? = savedStateHandle.get<String>("duplicateBillId")
        if (duplicateBillId != null) {
            viewModelScope.launch {
                val originalBill = repository.getBillById(duplicateBillId)
                if (originalBill != null) {
                    _uiState.update { current ->
                        current.copy(
                            description = originalBill.description,
                            amountCents = originalBill.totalAmountCents,
                            selectedCategoryId = originalBill.categoryId,
                            selectedItemId = originalBill.itemId,
                            selectedContactId = originalBill.contactId,
                            selectedFinancialAccountId = originalBill.financialAccountId,
                            selectedPaymentMethodId = originalBill.paymentMethodId,
                            expenseType = originalBill.type,
                            installmentsCount = originalBill.totalInstallments,
                            isCreditCard = originalBill.invoiceId != null,
                            recurrenceFrequency = originalBill.recurrenceFrequency ?: current.recurrenceFrequency,
                            recurrenceEndType = originalBill.recurrenceEndType ?: current.recurrenceEndType,
                            recurrenceEndDate = originalBill.recurrenceEndDate ?: current.recurrenceEndDate,
                            recurrenceOccurrencesCount = if (originalBill.type == BillType.RECURRING) originalBill.totalInstallments else current.recurrenceOccurrencesCount
                        )
                    }
                }
            }
        }

        val baseOptionsFlow = combine(
            repository.getCategories(),
            repository.getExpenseItems(),
            repository.getContacts(),
            repository.getFinancialAccounts(),
            repository.getPaymentMethods()
        ) { categories, items, contacts, accounts, methods ->
            BaseOptions(categories, items, contacts, accounts, methods)
        }

        val cardDataFlow = combine(
            repository.getCreditCards(),
            repository.getAllInstallments()
        ) { cards, installments ->
            val summaries = cards.associate { card ->
                val used = installments
                    .filter { it.invoiceId != null && !it.isPaid }
                    .sumOf { it.amountCents }
                card.id to CreditCardCalculator.calculateAvailableLimit(card.totalLimitCents, used)
            }
            cards to summaries
        }

        combine(baseOptionsFlow, cardDataFlow) { base, (cards, summaries) ->
            _uiState.update { current ->
                current.copy(
                    categories = base.categories,
                    allExpenseItems = base.items,
                    contacts = base.contacts,
                    financialAccounts = base.accounts,
                    paymentMethods = base.methods,
                    creditCards = cards,
                    creditCardSummaries = summaries,
                    selectedContactId = current.selectedContactId ?: base.contacts.firstOrNull()?.id,
                    selectedFinancialAccountId = current.selectedFinancialAccountId ?: base.accounts.firstOrNull()?.id,
                    selectedPaymentMethodId = current.selectedPaymentMethodId ?: base.methods.firstOrNull()?.id,
                    selectedCreditCardId = current.selectedCreditCardId ?: cards.firstOrNull()?.id
                )
            }
        }.launchIn(viewModelScope)
    }

    private data class BaseOptions(
        val categories: List<Category>,
        val items: List<ExpenseItem>,
        val contacts: List<Contact>,
        val accounts: List<FinancialAccount>,
        val methods: List<PaymentMethod>
    )

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onAmountChange(amountCents: Long) {
        _uiState.update { it.copy(amountCents = amountCents) }
    }

    fun onDueDateChange(dueDate: Long) {
        _uiState.update { current ->
            val updatedEndDate = if (current.recurrenceEndDate <= dueDate) {
                DateUtils.addMonths(dueDate, 12)
            } else current.recurrenceEndDate
            current.copy(dueDate = dueDate, recurrenceEndDate = updatedEndDate)
        }
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

    fun onContactSelect(contactId: String?) {
        _uiState.update { it.copy(selectedContactId = contactId) }
    }

    fun onPaymentStatusChange(isPaid: Boolean) {
        _uiState.update { it.copy(isPaid = isPaid) }
    }

    fun onExpenseTypeChange(type: BillType) {
        _uiState.update { it.copy(expenseType = type) }
    }

    fun onInstallmentsCountChange(count: Int) {
        _uiState.update { it.copy(installmentsCount = count.coerceIn(2, 72)) }
    }

    fun onRecurrenceFrequencyChange(frequency: RecurrenceFrequency) {
        _uiState.update { current ->
            val defaultCount = when (frequency) {
                RecurrenceFrequency.DAILY -> 30
                RecurrenceFrequency.WEEKLY -> 12
                RecurrenceFrequency.MONTHLY -> 12
                RecurrenceFrequency.YEARLY -> 5
            }
            current.copy(
                recurrenceFrequency = frequency,
                recurrenceOccurrencesCount = defaultCount
            )
        }
    }

    fun onRecurrenceEndTypeChange(endType: RecurrenceEndType) {
        _uiState.update { it.copy(recurrenceEndType = endType) }
    }

    fun onRecurrenceEndDateChange(endDate: Long) {
        _uiState.update { it.copy(recurrenceEndDate = endDate) }
    }

    fun onRecurrenceOccurrencesCountChange(count: Int) {
        _uiState.update { it.copy(recurrenceOccurrencesCount = count.coerceIn(2, 365)) }
    }

    fun onPaymentMethodSelect(methodId: String?) {
        _uiState.update { state ->
            val method = state.paymentMethods.find { it.id == methodId }
            val isCredit = method?.name?.let { name ->
                val lower = name.lowercase()
                lower.contains("crédito") && !lower.contains("débito")
            } ?: false

            state.copy(
                selectedPaymentMethodId = methodId,
                isCreditCard = isCredit,
                selectedCreditCardId = if (isCredit) {
                    state.selectedCreditCardId ?: state.creditCards.firstOrNull()?.id
                } else null
            )
        }
    }

    fun onFinancialAccountSelect(accountId: String?) {
        _uiState.update { it.copy(selectedFinancialAccountId = accountId) }
    }

    fun onToggleCreditCard(isCreditCard: Boolean) {
        _uiState.update { state ->
            val updatedPaymentMethodId = if (isCreditCard) {
                state.paymentMethods.find {
                    val lower = it.name.lowercase()
                    lower.contains("crédito") && !lower.contains("débito")
                }?.id ?: state.selectedPaymentMethodId
            } else {
                val currentMethod = state.paymentMethods.find { it.id == state.selectedPaymentMethodId }
                val isCurrentCredit = currentMethod?.name?.let {
                    val lower = it.lowercase()
                    lower.contains("crédito") && !lower.contains("débito")
                } ?: false
                if (isCurrentCredit) {
                    state.paymentMethods.find {
                        val lower = it.name.lowercase()
                        !lower.contains("crédito")
                    }?.id ?: state.selectedPaymentMethodId
                } else {
                    state.selectedPaymentMethodId
                }
            }
            state.copy(
                isCreditCard = isCreditCard,
                selectedPaymentMethodId = updatedPaymentMethodId,
                selectedCreditCardId = if (isCreditCard) {
                    state.selectedCreditCardId ?: state.creditCards.firstOrNull()?.id
                } else null
            )
        }
    }

    fun onCreditCardSelect(cardId: String?) {
        _uiState.update { it.copy(selectedCreditCardId = cardId) }
    }

    fun createQuickContact(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newContact = Contact(
                id = UUID.randomUUID().toString(),
                name = name.trim()
            )
            repository.saveContact(newContact)
            _uiState.update { it.copy(selectedContactId = newContact.id) }
        }
    }

    fun saveExpense() {
        val state = _uiState.value
        if (!state.isValid) {
            val error = when {
                state.selectedItemId == null -> "Selecione o Item da despesa (obrigatório)."
                state.amountCents <= 0L -> "Informe um valor válido maior que R$ 0,00."
                state.selectedContactId == null -> "Selecione o Contato / Fornecedor (obrigatório)."
                state.effectiveTitle.isBlank() -> "Selecione um item ou informe a descrição."
                state.isCreditCard && state.selectedCreditCardId == null -> "Selecione o Cartão de Crédito."
                else -> "Preencha todos os campos obrigatórios."
            }
            viewModelScope.launch { _uiEffect.emit(NewExpenseUiEffect.ShowError(error)) }
            return
        }

        viewModelScope.launch {
            try {
                val billId = UUID.randomUUID().toString()
                val finalTitle = state.effectiveTitle
                val finalDescription = if (state.description.isNotBlank()) state.description.trim() else finalTitle

                val totalInstallments = when (state.expenseType) {
                    BillType.SINGLE -> 1
                    BillType.INSTALLMENT -> state.installmentsCount.coerceAtLeast(1)
                    BillType.RECURRING -> when (state.recurrenceEndType) {
                        RecurrenceEndType.BY_OCCURRENCES -> state.recurrenceOccurrencesCount
                        else -> 12
                    }
                }

                val bill = Bill(
                    id = billId,
                    title = finalTitle,
                    description = finalDescription,
                    type = state.expenseType,
                    totalAmountCents = state.amountCents,
                    categoryId = state.selectedCategoryId,
                    itemId = state.selectedItemId,
                    invoiceId = null,
                    contactId = state.selectedContactId,
                    financialAccountId = state.selectedFinancialAccountId,
                    paymentMethodId = state.selectedPaymentMethodId,
                    totalInstallments = totalInstallments,
                    recurrenceFrequency = if (state.expenseType == BillType.RECURRING) state.recurrenceFrequency else null,
                    recurrenceEndType = if (state.expenseType == BillType.RECURRING) state.recurrenceEndType else null,
                    recurrenceEndDate = if (state.expenseType == BillType.RECURRING && state.recurrenceEndType == RecurrenceEndType.UNTIL_DATE) state.recurrenceEndDate else null,
                    createdAt = System.currentTimeMillis()
                )

                val selectedCard = if (state.isCreditCard) state.selectedCreditCard else null

                createBillUseCase(
                    bill = bill,
                    firstDueDate = state.dueDate,
                    creditCard = selectedCard,
                    isFirstInstallmentPaid = state.isPaid,
                    actualPaymentDate = if (state.isPaid) System.currentTimeMillis() else null
                )

                _uiState.update { it.copy(isSaved = true) }
                _uiEffect.emit(NewExpenseUiEffect.ExpenseSaved)
            } catch (e: Exception) {
                _uiEffect.emit(NewExpenseUiEffect.ShowError(e.localizedMessage ?: "Erro ao salvar despesa."))
            }
        }
    }
}
