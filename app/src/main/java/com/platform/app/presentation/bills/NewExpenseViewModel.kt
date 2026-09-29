package com.platform.app.presentation.bills

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
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CalculateInstallmentsUseCase
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

    val isValid: Boolean
        get() = description.isNotBlank() &&
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
    private val calculateInstallmentsUseCase: CalculateInstallmentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewExpenseUiState())
    val uiState: StateFlow<NewExpenseUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NewExpenseUiEffect>()
    val uiEffect: SharedFlow<NewExpenseUiEffect> = _uiEffect.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCategoriesIfEmpty()
            repository.seedInitialExpenseItemsIfEmpty()
            repository.seedInitialContactsIfEmpty()
            repository.seedInitialFinancialAccountsIfEmpty()
            repository.seedInitialPaymentMethodsIfEmpty()
            repository.seedInitialCreditCardsIfEmpty()
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

    fun onPaymentMethodSelect(methodId: String?) {
        _uiState.update { state ->
            val method = state.paymentMethods.find { it.id == methodId }
            val isCard = method?.name?.contains("cartão", ignoreCase = true) == true ||
                    method?.name?.contains("crédito", ignoreCase = true) == true
            state.copy(
                selectedPaymentMethodId = methodId,
                isCreditCard = if (isCard) true else state.isCreditCard
            )
        }
    }

    fun onFinancialAccountSelect(accountId: String?) {
        _uiState.update { it.copy(selectedFinancialAccountId = accountId) }
    }

    fun onToggleCreditCard(isCreditCard: Boolean) {
        _uiState.update { state ->
            state.copy(
                isCreditCard = isCreditCard,
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
                state.description.isBlank() -> "Informe a descrição da despesa."
                state.amountCents <= 0L -> "Informe um valor válido maior que R$ 0,00."
                state.selectedItemId == null -> "Selecione o Item da despesa (obrigatório)."
                state.selectedContactId == null -> "Selecione o Contato / Fornecedor (obrigatório)."
                state.isCreditCard && state.selectedCreditCardId == null -> "Selecione o Cartão de Crédito."
                else -> "Preencha todos os campos obrigatórios."
            }
            viewModelScope.launch { _uiEffect.emit(NewExpenseUiEffect.ShowError(error)) }
            return
        }

        viewModelScope.launch {
            try {
                val billId = UUID.randomUUID().toString()
                val totalInstallments = when (state.expenseType) {
                    BillType.SINGLE -> 1
                    BillType.INSTALLMENT -> state.installmentsCount.coerceAtLeast(2)
                    BillType.RECURRING -> 12
                }

                if (state.isCreditCard && state.selectedCreditCardId != null) {
                    val card = state.selectedCreditCard ?: repository.getCreditCards().let { null }
                    val closingDay = card?.closingDay ?: 25
                    val baseRefMonth = CreditCardCalculator.determineInvoiceReferenceMonth(state.dueDate, closingDay)

                    val installments = mutableListOf<BillInstallment>()
                    val baseAmount = state.amountCents / totalInstallments
                    val remainder = state.amountCents % totalInstallments

                    for (i in 1..totalInstallments) {
                        val refMonth = CreditCardCalculator.addMonthsToReferenceMonth(baseRefMonth, i - 1)
                        val invoice = repository.getOrCreateInvoiceForMonth(state.selectedCreditCardId, refMonth)
                        val installmentAmount = when (state.expenseType) {
                            BillType.SINGLE -> state.amountCents
                            BillType.INSTALLMENT -> if (i == 1) baseAmount + remainder else baseAmount
                            BillType.RECURRING -> state.amountCents
                        }

                        val isFirstAndPaid = state.isPaid && i == 1
                        val instStatus = if (isFirstAndPaid) BillStatus.PAID else BillStatus.PENDING
                        val paidAt = if (isFirstAndPaid) System.currentTimeMillis() else null

                        installments.add(
                            BillInstallment(
                                id = UUID.randomUUID().toString(),
                                billId = billId,
                                billTitle = state.description.trim(),
                                categoryId = state.selectedCategoryId,
                                categoryName = state.selectedCategory?.name ?: "Geral",
                                categoryColorHex = state.selectedCategory?.colorHex ?: "#64748B",
                                nature = state.inheritedNature,
                                itemId = state.selectedItemId,
                                itemName = state.selectedItem?.name,
                                invoiceId = invoice.id,
                                contactId = state.selectedContactId,
                                contactName = state.selectedContact?.name,
                                financialAccountId = state.selectedFinancialAccountId,
                                financialAccountName = state.selectedFinancialAccount?.name,
                                paymentMethodId = state.selectedPaymentMethodId,
                                paymentMethodName = state.selectedPaymentMethod?.name,
                                installmentNumber = i,
                                totalInstallments = totalInstallments,
                                amountCents = installmentAmount,
                                dueDate = invoice.dueDate,
                                paidAt = paidAt,
                                status = instStatus,
                                type = state.expenseType
                            )
                        )
                    }

                    val bill = Bill(
                        id = billId,
                        title = state.description.trim(),
                        description = state.description.trim(),
                        type = state.expenseType,
                        totalAmountCents = if (state.expenseType == BillType.RECURRING) state.amountCents else state.amountCents,
                        categoryId = state.selectedCategoryId,
                        itemId = state.selectedItemId,
                        invoiceId = installments.firstOrNull()?.invoiceId,
                        contactId = state.selectedContactId,
                        financialAccountId = state.selectedFinancialAccountId,
                        paymentMethodId = state.selectedPaymentMethodId,
                        totalInstallments = totalInstallments,
                        createdAt = System.currentTimeMillis()
                    )

                    repository.saveBillWithInstallments(bill, installments)
                } else {
                    // Sem cartão de crédito
                    val bill = Bill(
                        id = billId,
                        title = state.description.trim(),
                        description = state.description.trim(),
                        type = state.expenseType,
                        totalAmountCents = state.amountCents,
                        categoryId = state.selectedCategoryId,
                        itemId = state.selectedItemId,
                        invoiceId = null,
                        contactId = state.selectedContactId,
                        financialAccountId = state.selectedFinancialAccountId,
                        paymentMethodId = state.selectedPaymentMethodId,
                        totalInstallments = totalInstallments,
                        createdAt = System.currentTimeMillis()
                    )

                    val generatedInstallments = calculateInstallmentsUseCase(bill, state.dueDate)
                    val finalizedInstallments = generatedInstallments.mapIndexed { index, inst ->
                        val isFirstAndPaid = state.isPaid && (state.expenseType == BillType.SINGLE || index == 0)
                        inst.copy(
                            contactId = state.selectedContactId,
                            contactName = state.selectedContact?.name,
                            financialAccountId = state.selectedFinancialAccountId,
                            financialAccountName = state.selectedFinancialAccount?.name,
                            paymentMethodId = state.selectedPaymentMethodId,
                            paymentMethodName = state.selectedPaymentMethod?.name,
                            itemId = state.selectedItemId,
                            itemName = state.selectedItem?.name,
                            categoryId = state.selectedCategoryId,
                            categoryName = state.selectedCategory?.name ?: "Geral",
                            categoryColorHex = state.selectedCategory?.colorHex ?: "#64748B",
                            nature = state.inheritedNature,
                            status = if (isFirstAndPaid) BillStatus.PAID else BillStatus.PENDING,
                            paidAt = if (isFirstAndPaid) System.currentTimeMillis() else null
                        )
                    }

                    repository.saveBillWithInstallments(bill, finalizedInstallments)
                }

                _uiState.update { it.copy(isSaved = true) }
                _uiEffect.emit(NewExpenseUiEffect.ExpenseSaved)
            } catch (e: Exception) {
                _uiEffect.emit(NewExpenseUiEffect.ShowError(e.localizedMessage ?: "Erro ao salvar despesa."))
            }
        }
    }
}
