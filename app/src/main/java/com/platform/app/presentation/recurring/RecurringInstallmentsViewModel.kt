package com.platform.app.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.domain.model.BillType
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.usecase.ToggleInstallmentPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringInstallmentsViewModel @Inject constructor(
    private val repository: FinancialRepository,
    private val togglePaymentUseCase: ToggleInstallmentPaymentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringInstallmentsUiState())
    val uiState: StateFlow<RecurringInstallmentsUiState> = _uiState.asStateFlow()

    private val _effectChannel = Channel<RecurringInstallmentsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<RecurringInstallmentsUiEffect> = _effectChannel.receiveAsFlow()

    init {
        loadData()
    }

    fun onAction(action: RecurringInstallmentsUiAction) {
        when (action) {
            is RecurringInstallmentsUiAction.SelectFilter -> handleSelectFilter(action.type)
            is RecurringInstallmentsUiAction.StatusFilterChanged -> handleStatusFilter(action.status)
            is RecurringInstallmentsUiAction.SearchQueryChanged -> handleSearchQuery(action.query)
            is RecurringInstallmentsUiAction.TogglePayment -> handleTogglePayment(action.installmentId, action.currentPaid)
            is RecurringInstallmentsUiAction.DeleteBill -> handleDeleteBill(action.billId)
            is RecurringInstallmentsUiAction.OpenAdjustInstallment -> {
                _uiState.update { it.copy(installmentToAdjust = action.installment) }
            }
            is RecurringInstallmentsUiAction.DismissAdjustInstallment -> {
                _uiState.update { it.copy(installmentToAdjust = null) }
            }
            is RecurringInstallmentsUiAction.SaveAdjustInstallment -> handleSaveAdjustInstallment(action)
            is RecurringInstallmentsUiAction.Refresh -> loadData()
        }
    }

    private fun handleSaveAdjustInstallment(action: RecurringInstallmentsUiAction.SaveAdjustInstallment) {
        viewModelScope.launch {
            try {
                repository.updateInstallment(
                    installmentId = action.installmentId,
                    newAmountCents = action.newAmountCents,
                    newDueDate = action.newDueDate
                )
                _uiState.update { it.copy(installmentToAdjust = null) }
                _effectChannel.send(RecurringInstallmentsUiEffect.ShowSnackbar("Parcela ajustada com sucesso!"))
            } catch (e: Exception) {
                _effectChannel.send(RecurringInstallmentsUiEffect.ShowSnackbar("Erro ao ajustar parcela: ${e.message}"))
            }
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            repository.getBills(),
            repository.getAllInstallments()
        ) { bills, allInstallments ->
            val installmentsByBill = allInstallments.groupBy { it.billId }

            // Filtra apenas contas que são do tipo parcelada ou recorrente
            val targetBills = bills.filter { it.type == BillType.INSTALLMENT || it.type == BillType.RECURRING }

            val items = targetBills.map { bill ->
                val insts = installmentsByBill[bill.id]?.sortedBy { it.installmentNumber } ?: emptyList()
                val paidInsts = insts.filter { it.isPaid }
                val paidCount = paidInsts.size
                val totalPaid = paidInsts.sumOf { it.amountCents }

                val nextInst = insts
                    .filter { !it.isPaid }
                    .minByOrNull { it.dueDate }

                val remaining = if (bill.type == BillType.INSTALLMENT) {
                    (bill.totalAmountCents - totalPaid).coerceAtLeast(0L)
                } else {
                    nextInst?.amountCents ?: bill.totalAmountCents
                }
                val progress = if (bill.type == BillType.INSTALLMENT && bill.totalAmountCents > 0L) {
                    (totalPaid.toFloat() / bill.totalAmountCents.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val estimatedPayoff = if (bill.type == BillType.INSTALLMENT) {
                    insts.maxOfOrNull { it.dueDate }
                } else null

                BillWithInstallments(
                    bill = bill,
                    installments = insts,
                    paidInstallmentsCount = paidCount,
                    totalPaidCents = totalPaid,
                    remainingCents = remaining,
                    progress = progress,
                    nextInstallment = nextInst,
                    estimatedPayoffDate = estimatedPayoff
                )
            }

            val totalActiveInstallments = items
                .filter { it.bill.type == BillType.INSTALLMENT }
                .sumOf { it.remainingCents }

            val totalMonthlyRecurring = items
                .filter { it.bill.type == BillType.RECURRING }
                .sumOf { it.bill.totalAmountCents }

            val filtered = applyFilter(
                items = items,
                typeFilter = _uiState.value.selectedFilter,
                statusFilter = _uiState.value.statusFilter,
                query = _uiState.value.searchQuery
            )

            _uiState.update {
                it.copy(
                    items = items,
                    filteredItems = filtered,
                    totalActiveInstallmentsCents = totalActiveInstallments,
                    totalMonthlyRecurringCents = totalMonthlyRecurring,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }.catch { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Erro ao carregar pagamentos planejados."
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun handleSelectFilter(type: BillType?) {
        _uiState.update {
            val filtered = applyFilter(it.items, type, it.statusFilter, it.searchQuery)
            it.copy(selectedFilter = type, filteredItems = filtered)
        }
    }

    private fun handleStatusFilter(status: RecurringStatusFilter) {
        _uiState.update {
            val filtered = applyFilter(it.items, it.selectedFilter, status, it.searchQuery)
            it.copy(statusFilter = status, filteredItems = filtered)
        }
    }

    private fun handleSearchQuery(query: String) {
        _uiState.update {
            val filtered = applyFilter(it.items, it.selectedFilter, it.statusFilter, query)
            it.copy(searchQuery = query, filteredItems = filtered)
        }
    }

    private fun applyFilter(
        items: List<BillWithInstallments>,
        typeFilter: BillType?,
        statusFilter: RecurringStatusFilter,
        query: String
    ): List<BillWithInstallments> {
        return items.filter { item ->
            val matchesType = typeFilter == null || item.bill.type == typeFilter

            val isCompleted = if (item.bill.type == BillType.INSTALLMENT) {
                item.paidInstallmentsCount == item.installments.size && item.installments.isNotEmpty()
            } else false

            val matchesStatus = when (statusFilter) {
                RecurringStatusFilter.ALL -> true
                RecurringStatusFilter.ACTIVE -> !isCompleted
                RecurringStatusFilter.COMPLETED -> isCompleted
            }

            val matchesQuery = query.isBlank() ||
                    item.bill.title.contains(query, ignoreCase = true) ||
                    (item.installments.firstOrNull()?.categoryName?.contains(query, ignoreCase = true) == true) ||
                    (item.installments.firstOrNull()?.contactName?.contains(query, ignoreCase = true) == true)

            matchesType && matchesStatus && matchesQuery
        }
    }

    private fun handleTogglePayment(installmentId: String, currentPaid: Boolean) {
        viewModelScope.launch {
            try {
                togglePaymentUseCase(installmentId, currentPaid)
                val msg = if (!currentPaid) "Parcela marcada como paga!" else "Pagamento desfeito."
                _effectChannel.send(RecurringInstallmentsUiEffect.ShowSnackbar(msg))
            } catch (e: Exception) {
                _effectChannel.send(RecurringInstallmentsUiEffect.ShowSnackbar("Erro ao alterar pagamento: ${e.message}"))
            }
        }
    }

    private fun handleDeleteBill(billId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBill(billId)
                _effectChannel.send(RecurringInstallmentsUiEffect.ShowSnackbar("Conta excluída com sucesso."))
            } catch (e: Exception) {
                _effectChannel.send(RecurringInstallmentsUiEffect.ShowSnackbar("Erro ao excluir conta: ${e.message}"))
            }
        }
    }
}
