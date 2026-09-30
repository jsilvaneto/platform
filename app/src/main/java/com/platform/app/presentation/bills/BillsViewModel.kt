package com.platform.app.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
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
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val repository: FinancialRepository,
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
            is BillsUiAction.TogglePayment -> handleTogglePayment(action.installment)
            is BillsUiAction.DeleteBill -> handleDeleteBill(action.billId)
            is BillsUiAction.SearchQueryChanged -> handleSearchQuery(action.query)
            is BillsUiAction.TypeFilterChanged -> handleTypeFilter(action.type)
            is BillsUiAction.StatusFilterChanged -> handleStatusFilter(action.status)
            is BillsUiAction.PeriodFilterChanged -> handlePeriodFilter(action.period)
            is BillsUiAction.YearChanged -> handleYearChanged(action.year)
            is BillsUiAction.PayBatch -> handlePayBatch(action.installmentIds)
            is BillsUiAction.DeleteBatch -> handleDeleteBatch(action.billIds)
            is BillsUiAction.OpenEditInstallment -> handleOpenEdit(action.installment)
            is BillsUiAction.DismissEditInstallment -> handleDismissEdit()
            is BillsUiAction.SaveInstallmentEdit -> handleSaveEdit(action)
            is BillsUiAction.Refresh -> {
                loadAuxiliaryData()
                loadInstallments()
            }
            is BillsUiAction.ResetFilters -> handleResetFilters()
        }
    }

    private fun handleResetFilters() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        _uiState.update { current ->
            val filtered = applyFilters(
                installments = current.installments,
                query = "",
                typeFilter = null,
                statusFilter = null,
                periodFilter = BillPeriodFilter.ALL,
                selectedYear = currentYear
            )
            recalculateMetrics(current, filtered).copy(
                selectedYear = currentYear,
                periodFilter = BillPeriodFilter.ALL,
                typeFilter = null,
                statusFilter = null,
                searchQuery = "",
                filteredInstallments = filtered
            )
        }
    }

    private fun handleOpenEdit(installment: BillInstallment) {
        _uiState.update { it.copy(editingInstallment = installment) }
    }

    private fun handleDismissEdit() {
        _uiState.update { it.copy(editingInstallment = null) }
    }

    private fun handleSaveEdit(action: BillsUiAction.SaveInstallmentEdit) {
        viewModelScope.launch {
            try {
                repository.updateBillAndInstallment(
                    installmentId = action.installmentId,
                    billId = action.billId,
                    title = action.title,
                    description = action.description,
                    amountCents = action.amountCents,
                    dueDate = action.dueDate,
                    categoryId = action.categoryId,
                    itemId = action.itemId,
                    contactId = action.contactId,
                    financialAccountId = action.financialAccountId,
                    paymentMethodId = action.paymentMethodId
                )
                _uiState.update { it.copy(editingInstallment = null) }
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Registro atualizado com sucesso!"))
            } catch (e: Exception) {
                _effectChannel.send(BillsUiEffect.ShowSnackbar("Erro ao salvar alterações: ${e.message}"))
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
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val yearsFromData = installments.map { getYearFromTimestamp(it.dueDate) }.toSet()
                    val allYears = (yearsFromData + currentYear).sortedDescending()

                    val filtered = applyFilters(
                        installments = installments,
                        query = current.searchQuery,
                        typeFilter = current.typeFilter,
                        statusFilter = current.statusFilter,
                        periodFilter = current.periodFilter,
                        selectedYear = current.selectedYear
                    )

                    val now = System.currentTimeMillis()
                    val total = filtered.sumOf { it.amountCents }
                    val paid = filtered.filter { it.isPaid }.sumOf { it.amountCents }
                    val pending = filtered.filter { !it.isPaid && it.dueDate >= now }.sumOf { it.amountCents }
                    val overdue = filtered.filter { !it.isPaid && it.dueDate < now }.sumOf { it.amountCents }

                    current.copy(
                        installments = installments,
                        filteredInstallments = filtered,
                        availableYears = allYears,
                        totalPeriodCents = total,
                        paidPeriodCents = paid,
                        pendingPeriodCents = pending,
                        overduePeriodCents = overdue,
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
            val filtered = applyFilters(
                current.installments,
                query,
                current.typeFilter,
                current.statusFilter,
                current.periodFilter,
                current.selectedYear
            )
            recalculateMetrics(current, filtered).copy(searchQuery = query, filteredInstallments = filtered)
        }
    }

    private fun handleTypeFilter(type: BillType?) {
        _uiState.update { current ->
            val filtered = applyFilters(
                current.installments,
                current.searchQuery,
                type,
                current.statusFilter,
                current.periodFilter,
                current.selectedYear
            )
            recalculateMetrics(current, filtered).copy(typeFilter = type, filteredInstallments = filtered)
        }
    }

    private fun handleStatusFilter(status: BillStatus?) {
        _uiState.update { current ->
            val filtered = applyFilters(
                current.installments,
                current.searchQuery,
                current.typeFilter,
                status,
                current.periodFilter,
                current.selectedYear
            )
            recalculateMetrics(current, filtered).copy(statusFilter = status, filteredInstallments = filtered)
        }
    }

    private fun handlePeriodFilter(period: BillPeriodFilter) {
        _uiState.update { current ->
            val filtered = applyFilters(
                current.installments,
                current.searchQuery,
                current.typeFilter,
                current.statusFilter,
                period,
                current.selectedYear
            )
            recalculateMetrics(current, filtered).copy(periodFilter = period, filteredInstallments = filtered)
        }
    }

    private fun handleYearChanged(year: Int?) {
        _uiState.update { current ->
            val filtered = applyFilters(
                current.installments,
                current.searchQuery,
                current.typeFilter,
                current.statusFilter,
                current.periodFilter,
                year
            )
            recalculateMetrics(current, filtered).copy(selectedYear = year, filteredInstallments = filtered)
        }
    }

    private fun recalculateMetrics(state: BillsUiState, filtered: List<BillInstallment>): BillsUiState {
        val now = System.currentTimeMillis()
        val total = filtered.sumOf { it.amountCents }
        val paid = filtered.filter { it.isPaid }.sumOf { it.amountCents }
        val pending = filtered.filter { !it.isPaid && it.dueDate >= now }.sumOf { it.amountCents }
        val overdue = filtered.filter { !it.isPaid && it.dueDate < now }.sumOf { it.amountCents }
        return state.copy(
            totalPeriodCents = total,
            paidPeriodCents = paid,
            pendingPeriodCents = pending,
            overduePeriodCents = overdue
        )
    }

    private fun applyFilters(
        installments: List<BillInstallment>,
        query: String,
        typeFilter: BillType?,
        statusFilter: BillStatus?,
        periodFilter: BillPeriodFilter,
        selectedYear: Int?
    ): List<BillInstallment> {
        val now = System.currentTimeMillis()
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth = DateUtils.getEndOfMonth(now)
        val thirtyDaysAhead = now + (30L * 24 * 3600 * 1000)

        val filtered = installments.filter { inst ->
            // Filtro de ano
            val matchesYear = if (selectedYear != null) {
                getYearFromTimestamp(inst.dueDate) == selectedYear
            } else true

            // Filtro de período relativo
            val matchesPeriod = when (periodFilter) {
                BillPeriodFilter.ALL -> true
                BillPeriodFilter.THIS_MONTH -> inst.dueDate in startOfMonth..endOfMonth
                BillPeriodFilter.NEXT_30_DAYS -> inst.dueDate in now..thirtyDaysAhead
                BillPeriodFilter.OVERDUE -> !inst.isPaid && inst.dueDate < now
            }

            // Busca por texto
            val matchesQuery = query.isBlank() ||
                    inst.billTitle.contains(query, ignoreCase = true) ||
                    inst.categoryName.contains(query, ignoreCase = true) ||
                    (inst.contactName?.contains(query, ignoreCase = true) == true) ||
                    (inst.financialAccountName?.contains(query, ignoreCase = true) == true)

            // Tipo da conta
            val matchesType = typeFilter == null || inst.type == typeFilter

            // Status de liquidação
            val matchesStatus = when (statusFilter) {
                null -> true
                BillStatus.PAID -> inst.isPaid
                BillStatus.PENDING -> !inst.isPaid && inst.dueDate >= now
                BillStatus.OVERDUE -> !inst.isPaid && inst.dueDate < now
            }

            matchesYear && matchesPeriod && matchesQuery && matchesType && matchesStatus
        }

        // 1- os primeiros registros sempre ser os mais recentes (ordenar por data decrescente)
        return filtered.sortedByDescending { it.dueDate }
    }

    private fun getYearFromTimestamp(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.YEAR)
    }
}
