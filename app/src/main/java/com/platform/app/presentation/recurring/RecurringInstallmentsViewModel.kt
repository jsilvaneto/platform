package com.platform.app.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
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
                val pendingInsts = insts.filter { !it.isPaid }
                val paidCount = paidInsts.size
                val totalPaid = paidInsts.sumOf { it.amountCents }

                val nextInst = pendingInsts.minByOrNull { it.dueDate }

                // Verdade financeira: Saldo restante é a soma real das parcelas pendentes!
                val remaining = if (bill.type == BillType.INSTALLMENT) {
                    pendingInsts.sumOf { it.amountCents }
                } else {
                    nextInst?.amountCents ?: bill.totalAmountCents
                }

                val totalFinanced = if (insts.isNotEmpty()) insts.sumOf { it.amountCents } else bill.totalAmountCents
                val progress = if (totalFinanced > 0L) {
                    (totalPaid.toFloat() / totalFinanced.toFloat()).coerceIn(0f, 1f)
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

            // Totais Reais de Compras Parceladas
            val installmentItems = items.filter { it.bill.type == BillType.INSTALLMENT }
            val totalActiveInstallments = installmentItems.sumOf { it.remainingCents }
            val totalPaidInstallments = installmentItems.sumOf { it.totalPaidCents }
            val totalOriginalFinanced = totalActiveInstallments + totalPaidInstallments

            // Totais Reais de Assinaturas & Recorrentes
            val recurringItems = items.filter { it.bill.type == BillType.RECURRING }
            val totalMonthlyRecurring = recurringItems.sumOf { it.bill.totalAmountCents }

            val now = System.currentTimeMillis()
            val startOfMonth = DateUtils.getStartOfMonth(now)
            val endOfMonth = DateUtils.getEndOfMonth(now)

            val recurringInstsThisMonth = allInstallments.filter { inst ->
                val bill = targetBills.find { it.id == inst.billId }
                bill?.type == BillType.RECURRING && inst.dueDate in startOfMonth..endOfMonth
            }
            val paidThisMonthRecurring = recurringInstsThisMonth.filter { it.isPaid }.sumOf { it.amountCents }
            val pendingThisMonthRecurring = recurringInstsThisMonth.filter { !it.isPaid }.sumOf { it.amountCents }

            // Linha do Tempo Futura (Cronograma dos próximos 6 a 12 meses)
            val futureInstallments = allInstallments
                .filter { it.dueDate >= startOfMonth }
                .sortedBy { it.dueDate }

            val futureTimeline = futureInstallments
                .groupBy { DateUtils.formatMonthYear(it.dueDate) }
                .map { (monthLabel, instList) ->
                    val total = instList.sumOf { it.amountCents }
                    val pending = instList.filter { !it.isPaid }.sumOf { it.amountCents }
                    val paid = instList.filter { it.isPaid }.sumOf { it.amountCents }
                    val firstDue = instList.firstOrNull()?.dueDate ?: 0L
                    TimelineMonthSummary(
                        monthLabel = monthLabel,
                        timestamp = firstDue,
                        totalCents = total,
                        pendingCents = pending,
                        paidCents = paid,
                        installmentsCount = instList.size,
                        items = instList
                    )
                }

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
                    futureTimeline = futureTimeline,
                    totalActiveInstallmentsCents = totalActiveInstallments,
                    totalOriginalFinancedCents = totalOriginalFinanced,
                    totalPaidInstallmentsCents = totalPaidInstallments,
                    totalMonthlyRecurringCents = totalMonthlyRecurring,
                    pendingThisMonthRecurringCents = pendingThisMonthRecurring,
                    paidThisMonthRecurringCents = paidThisMonthRecurring,
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
