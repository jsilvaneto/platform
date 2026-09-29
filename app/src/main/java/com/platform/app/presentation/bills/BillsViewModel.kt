package com.platform.app.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.CreateBillUseCase
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val createBillUseCase: CreateBillUseCase,
    private val togglePaymentUseCase: ToggleInstallmentPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<BillsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<BillsUiEffect> = _effectChannel.receiveAsFlow()

    private var installmentsJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialCategoriesIfEmpty()
            repository.seedInitialFinancialAccountsIfEmpty()
            repository.seedInitialPaymentMethodsIfEmpty()
        }
        loadAuxiliaryData()
        loadInstallments()
    }

    fun onAction(action: BillsUiAction) {
        when (action) {
            is BillsUiAction.CreateBill -> handleCreateBill(action)
            is BillsUiAction.TogglePayment -> handleTogglePayment(action.installment)
            is BillsUiAction.DeleteBill -> handleDeleteBill(action.billId)
            is BillsUiAction.SearchQueryChanged -> handleSearchQuery(action.query)
            is BillsUiAction.TypeFilterChanged -> handleTypeFilter(action.type)
            is BillsUiAction.StatusFilterChanged -> handleStatusFilter(action.status)
            is BillsUiAction.PeriodFilterChanged -> handlePeriodFilter(action.period)
            is BillsUiAction.MonthChanged -> handleMonthChanged(action.monthMillis)
            is BillsUiAction.PayBatch -> handlePayBatch(action.installmentIds)
            is BillsUiAction.DeleteBatch -> handleDeleteBatch(action.billIds)
            is BillsUiAction.Refresh -> {
                loadAuxiliaryData()
                loadInstallments()
            }
        }
    }

    private fun handlePayBatch(installmentIds: List<String>) {
        viewModelScope.launch {
            try {
                installmentIds.forEach { id ->
                    repository.toggleInstallmentPayment(
                        installmentId = id,
                        isPaid = true,
                        paidTimestamp = System.currentTimeMillis()
                    )
                }
                _effectChannel.send(BillsUiEffect.ShowSnackbar("${installmentIds.size} conta(s) marcada(s) como paga(s)!"))
            } catch (e: Exception) {
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Erro no pagamento em lote: ${e.message}"))
            }
        }
    }

    private fun handleDeleteBatch(billIds: List<String>) {
        viewModelScope.launch {
            try {
                billIds.distinct().forEach { id ->
                    repository.deleteBill(id)
                }
                _effectChannel.send(BillsUiEffect.ShowSnackbar("${billIds.distinct().size} conta(s) excluída(s) com sucesso."))
            } catch (e: Exception) {
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Erro na exclusão em lote: ${e.message}"))
            }
        }
    }

    private fun loadAuxiliaryData() {
        combine(
            repository.getCategories(),
            repository.getExpenseItems(),
            repository.getContacts(),
            repository.getFinancialAccounts(),
            repository.getPaymentMethods()
        ) { categories, expenseItems, contacts, accounts, methods ->
            _uiState.update {
                it.copy(
                    categories = categories,
                    expenseItems = expenseItems,
                    contacts = contacts,
                    financialAccounts = accounts,
                    paymentMethods = methods
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun loadInstallments() {
        installmentsJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }

        installmentsJob = repository.getAllInstallments()
            .onEach { installments ->
                _uiState.update { current ->
                    val filtered = applyFilters(
                        installments = installments,
                        query = current.searchQuery,
                        typeFilter = current.typeFilter,
                        statusFilter = current.statusFilter,
                        periodFilter = current.periodFilter
                    )
                    current.copy(
                        installments = installments,
                        filteredInstallments = filtered,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Erro ao carregar dados locais."
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun handleCreateBill(action: BillsUiAction.CreateBill) {
        viewModelScope.launch {
            try {
                val bill = Bill(
                    id = UUID.randomUUID().toString(),
                    title = action.title.trim(),
                    description = action.description.trim(),
                    type = action.type,
                    totalAmountCents = action.totalAmountCents,
                    categoryId = action.categoryId,
                    itemId = action.itemId,
                    invoiceId = action.invoiceId,
                    contactId = action.contactId,
                    financialAccountId = action.financialAccountId,
                    paymentMethodId = action.paymentMethodId,
                    totalInstallments = action.totalInstallments
                )
                createBillUseCase(bill, action.firstDueDate)
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Conta '${bill.title}' cadastrada com sucesso!"))
            } catch (e: Exception) {
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Erro ao cadastrar conta: ${e.message}"))
            }
        }
    }

    private fun handleTogglePayment(installment: BillInstallment) {
        viewModelScope.launch {
            try {
                togglePaymentUseCase(installment.id, installment.isPaid)
                val msg = if (!installment.isPaid) "Parcela marcada como paga!" else "Pagamento desfeito."
                _effectChannel.send(BillsUiEffect.ShowSnackbar(msg))
            } catch (e: Exception) {
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Erro ao alterar pagamento: ${e.message}"))
            }
        }
    }

    private fun handleDeleteBill(billId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBill(billId)
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Conta excluída com sucesso."))
            } catch (e: Exception) {
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Erro ao excluir conta: ${e.message}"))
            }
        }
    }

    private fun handleSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilters(current.installments, query, current.typeFilter, current.statusFilter, current.periodFilter)
            current.copy(searchQuery = query, filteredInstallments = filtered)
        }
    }

    private fun handleTypeFilter(type: BillType?) {
        _uiState.update { current ->
            val filtered = applyFilters(current.installments, current.searchQuery, type, current.statusFilter, current.periodFilter)
            current.copy(typeFilter = type, filteredInstallments = filtered)
        }
    }

    private fun handleStatusFilter(status: BillStatus?) {
        _uiState.update { current ->
            val filtered = applyFilters(current.installments, current.searchQuery, current.typeFilter, status, current.periodFilter)
            current.copy(statusFilter = status, filteredInstallments = filtered)
        }
    }

    private fun handlePeriodFilter(period: BillPeriodFilter) {
        _uiState.update { current ->
            val filtered = applyFilters(current.installments, current.searchQuery, current.typeFilter, current.statusFilter, period)
            current.copy(periodFilter = period, filteredInstallments = filtered)
        }
    }

    private fun handleMonthChanged(monthMillis: Long) {
        _uiState.update { it.copy(selectedMonthMillis = monthMillis) }
        loadInstallments()
    }

    private fun applyFilters(
        installments: List<BillInstallment>,
        query: String,
        typeFilter: BillType?,
        statusFilter: BillStatus?,
        periodFilter: BillPeriodFilter
    ): List<BillInstallment> {
        val now = System.currentTimeMillis()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)
        val thirtyDaysAhead = now + (30L * 24 * 3600 * 1000)

        return installments.filter { inst ->
            val matchesPeriod = when (periodFilter) {
                BillPeriodFilter.ALL -> true
                BillPeriodFilter.THIS_MONTH -> inst.dueDate in startOfMonth..endOfMonth
                BillPeriodFilter.NEXT_30_DAYS -> inst.dueDate in now..thirtyDaysAhead
                BillPeriodFilter.OVERDUE -> !inst.isPaid && inst.dueDate < now
            }

            val matchesQuery = query.isBlank() ||
                    inst.billTitle.contains(query, ignoreCase = true) ||
                    inst.categoryName.contains(query, ignoreCase = true)

            val matchesType = typeFilter == null || inst.type == typeFilter

            val matchesStatus = when (statusFilter) {
                null -> true
                BillStatus.PAID -> inst.isPaid
                BillStatus.PENDING -> !inst.isPaid && inst.dueDate >= now
                BillStatus.OVERDUE -> !inst.isPaid && inst.dueDate < now
            }

            matchesPeriod && matchesQuery && matchesType && matchesStatus
        }
    }
}
