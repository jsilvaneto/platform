package com.platform.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.domain.repository.FinancialRepository
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType
import com.platform.app.domain.usecase.CalculateMonthlyForecastUseCase
import com.platform.app.domain.usecase.CreateBillUseCase
import com.platform.app.domain.usecase.GetFinancialDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

sealed interface HomeUiEffect {
    data class ShowUndoSnackbar(
        val message: String,
        val actionLabel: String = "Desfazer",
        val undoAction: HomeUiAction
    ) : HomeUiEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateMonthlyForecastUseCase: CalculateMonthlyForecastUseCase,
    private val getFinancialDashboardUseCase: GetFinancialDashboardUseCase,
    private val repository: FinancialRepository,
    private val createBillUseCase: CreateBillUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect: SharedFlow<HomeUiEffect> = _uiEffect.asSharedFlow()

    private var forecastJob: Job? = null

    // Cache of domain objects to recompute day selection quickly
    private var cachedAllInstallments: List<BillInstallment> = emptyList()
    private var cachedAllInvoices: List<CreditCardInvoice> = emptyList()
    private var cachedCardsMap: Map<String, CreditCard> = emptyMap()

    init {
        repository.getExpenseItems()
            .onEach { items ->
                _uiState.update { it.copy(allExpenseItems = items) }
            }
            .catch { /* ignore */ }
            .launchIn(viewModelScope)

        observeData(_uiState.value.selectedMonthMillis)
    }

    fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.ChangeViewMode -> {
                val autoSelectDay = if (action.viewMode == HomeViewMode.CALENDAR && _uiState.value.selectedCalendarDayMillis == null) {
                    val activeDays = _uiState.value.calendarDays.filter { it.itemsCount > 0 && !it.isFullyPaid }
                    val now = System.currentTimeMillis()
                    activeDays.firstOrNull { it.dateMillis >= now }?.dateMillis ?: activeDays.firstOrNull()?.dateMillis
                } else {
                    _uiState.value.selectedCalendarDayMillis
                }
                _uiState.update { state ->
                    val selectedItems = if (autoSelectDay != null) {
                        filterPayablesForDay(
                            dayStartMillis = autoSelectDay,
                            allInstallments = cachedAllInstallments,
                            allInvoices = cachedAllInvoices,
                            cardsMap = cachedCardsMap
                        )
                    } else {
                        state.daySelectedItems
                    }
                    state.copy(
                        viewMode = action.viewMode,
                        selectedCalendarDayMillis = autoSelectDay,
                        daySelectedItems = selectedItems
                    )
                }
            }
            is HomeUiAction.SelectCalendarDay -> {
                _uiState.update { state ->
                    val selectedItems = if (action.dayMillis != null) {
                        filterPayablesForDay(
                            dayStartMillis = action.dayMillis,
                            allInstallments = cachedAllInstallments,
                            allInvoices = cachedAllInvoices,
                            cardsMap = cachedCardsMap
                        )
                    } else {
                        emptyList()
                    }
                    state.copy(
                        selectedCalendarDayMillis = action.dayMillis,
                        daySelectedItems = selectedItems
                    )
                }
            }
            is HomeUiAction.ToggleOverdueBanner -> {
                _uiState.update { it.copy(isOverdueBannerExpanded = action.expanded) }
            }
            is HomeUiAction.PreviousMonth -> {
                val newMonth = DateUtils.addMonths(_uiState.value.selectedMonthMillis, -1)
                observeData(newMonth)
            }
            is HomeUiAction.NextMonth -> {
                val newMonth = DateUtils.addMonths(_uiState.value.selectedMonthMillis, 1)
                observeData(newMonth)
            }
            is HomeUiAction.CurrentMonth -> {
                observeData(System.currentTimeMillis())
            }
            is HomeUiAction.SelectMonth -> {
                observeData(action.monthMillis)
            }
            is HomeUiAction.PayBill -> {
                viewModelScope.launch {
                    repository.toggleInstallmentPayment(
                        installmentId = action.installmentId,
                        isPaid = true,
                        paidTimestamp = System.currentTimeMillis(),
                        actualPaymentDate = action.actualPaymentDate
                    )
                    _uiEffect.emit(
                        HomeUiEffect.ShowUndoSnackbar(
                            message = "Conta marcada como paga!",
                            undoAction = HomeUiAction.UndoPayBill(action.installmentId)
                        )
                    )
                }
            }
            is HomeUiAction.UndoPayBill -> {
                viewModelScope.launch {
                    repository.toggleInstallmentPayment(
                        installmentId = action.installmentId,
                        isPaid = false,
                        paidTimestamp = null
                    )
                }
            }
            is HomeUiAction.PayInvoice -> {
                viewModelScope.launch {
                    repository.payInvoice(action.invoiceId, action.actualPaymentDate)
                    _uiEffect.emit(
                        HomeUiEffect.ShowUndoSnackbar(
                            message = "Fatura marcada como paga!",
                            undoAction = HomeUiAction.UndoPayInvoice(action.invoiceId)
                        )
                    )
                }
            }
            is HomeUiAction.UndoPayInvoice -> {
                viewModelScope.launch {
                    repository.reopenInvoice(action.invoiceId)
                }
            }
            is HomeUiAction.TogglePaidSection -> {
                _uiState.update { it.copy(isPaidSectionExpanded = action.expanded) }
            }
            is HomeUiAction.SaveQuickExpense -> {
                viewModelScope.launch {
                    val item = _uiState.value.allExpenseItems.find { it.id == action.itemId }
                    val billId = UUID.randomUUID().toString()
                    val title = item?.name ?: "Despesa"
                    val now = System.currentTimeMillis()
                    val bill = Bill(
                        id = billId,
                        title = title,
                        description = title,
                        type = BillType.SINGLE,
                        totalAmountCents = action.amountCents,
                        categoryId = item?.categoryId,
                        itemId = action.itemId,
                        totalInstallments = 1,
                        createdAt = now
                    )
                    createBillUseCase(
                        bill = bill,
                        firstDueDate = now,
                        isFirstInstallmentPaid = action.isPaid
                    )
                    _uiEffect.emit(
                        HomeUiEffect.ShowUndoSnackbar(
                            message = "Despesa lançada!",
                            undoAction = HomeUiAction.UndoSaveBill(billId)
                        )
                    )
                }
            }
            is HomeUiAction.UndoSaveBill -> {
                viewModelScope.launch {
                    repository.deleteBill(action.billId)
                }
            }
            is HomeUiAction.Refresh -> {
                observeData(_uiState.value.selectedMonthMillis)
            }
        }
    }

    private fun observeData(monthMillis: Long) {
        forecastJob?.cancel()
        _uiState.update { it.copy(selectedMonthMillis = monthMillis, isLoading = true) }

        forecastJob = combine(
            calculateMonthlyForecastUseCase(monthMillis),
            getFinancialDashboardUseCase(monthMillis),
            repository.getAllInstallments(),
            repository.getAllCreditCardInvoices(),
            repository.getCreditCards()
        ) { forecast, dashboard, allInstallments, allInvoices, cards ->
            val cardsMap = cards.associateBy { it.id }
            cachedAllInstallments = allInstallments
            cachedAllInvoices = allInvoices
            cachedCardsMap = cardsMap

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply { timeInMillis = now }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfToday = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfToday = cal.timeInMillis
            val sevenDaysAhead = endOfToday + (7L * 24 * 3600 * 1000)
            val thirtyDaysAhead = endOfToday + (30L * 24 * 3600 * 1000)

            // 1. Global Overdue Items (All unpaid across entire database with dueDate < startOfToday)
            val overdueBills = allInstallments
                .filter { !it.isPaid && it.invoiceId == null && it.dueDate < startOfToday }
                .map { PayableItem.BillPayable(it, PayableUrgency.OVERDUE) }

            val overdueInvoices = allInvoices
                .filter { it.status != InvoiceStatus.PAGA && it.dueDate < startOfToday }
                .map { inv ->
                    val card = cardsMap[inv.creditCardId]
                    PayableItem.InvoicePayable(
                        invoice = inv,
                        cardName = card?.name ?: "Cartão",
                        cardColorHex = card?.colorHex ?: "#3B82F6",
                        urgency = PayableUrgency.OVERDUE
                    )
                }

            val globalOverdue = (overdueBills + overdueInvoices).sortedBy { it.dueDate }
            val globalOverdueTotal = globalOverdue.sumOf { it.amountCents }

            // 2. Next 30 Days Items (All pending in startOfToday..thirtyDaysAhead)
            val next30Bills = allInstallments
                .filter { !it.isPaid && it.invoiceId == null && it.dueDate in startOfToday..thirtyDaysAhead }
                .map { inst ->
                    val urgency = when {
                        inst.dueDate in startOfToday..endOfToday -> PayableUrgency.DUE_TODAY
                        inst.dueDate in (endOfToday + 1)..sevenDaysAhead -> PayableUrgency.NEXT_7_DAYS
                        else -> PayableUrgency.LATER
                    }
                    PayableItem.BillPayable(inst, urgency)
                }

            val next30Invoices = allInvoices
                .filter { it.status != InvoiceStatus.PAGA && it.dueDate in startOfToday..thirtyDaysAhead }
                .map { inv ->
                    val card = cardsMap[inv.creditCardId]
                    val urgency = when {
                        inv.dueDate in startOfToday..endOfToday -> PayableUrgency.DUE_TODAY
                        inv.dueDate in (endOfToday + 1)..sevenDaysAhead -> PayableUrgency.NEXT_7_DAYS
                        else -> PayableUrgency.LATER
                    }
                    PayableItem.InvoicePayable(
                        invoice = inv,
                        cardName = card?.name ?: "Cartão",
                        cardColorHex = card?.colorHex ?: "#3B82F6",
                        urgency = urgency
                    )
                }

            val next30Items = (next30Bills + next30Invoices).sortedBy { it.dueDate }
            val next30Total = next30Items.sumOf { it.amountCents }

            // 3. Calendar Days for Selected Month
            val calendarDays = buildCalendarDays(
                monthMillis = monthMillis,
                allInstallments = allInstallments,
                allInvoices = allInvoices,
                startOfToday = startOfToday,
                endOfToday = endOfToday
            )

            // 4. Selected Day Items
            val currentSelectedDay = _uiState.value.selectedCalendarDayMillis
            val dayItems = if (currentSelectedDay != null) {
                filterPayablesForDay(
                    dayStartMillis = currentSelectedDay,
                    allInstallments = allInstallments,
                    allInvoices = allInvoices,
                    cardsMap = cardsMap
                )
            } else {
                emptyList()
            }

            _uiState.update { current ->
                current.copy(
                    forecastResult = forecast,
                    dashboardMetrics = dashboard,
                    globalOverdueItems = globalOverdue,
                    globalOverdueTotalCents = globalOverdueTotal,
                    next30DaysItems = next30Items,
                    next30DaysTotalCents = next30Total,
                    calendarDays = calendarDays,
                    daySelectedItems = dayItems,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }.catch { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Erro ao carregar dados financeiros."
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun buildCalendarDays(
        monthMillis: Long,
        allInstallments: List<BillInstallment>,
        allInvoices: List<CreditCardInvoice>,
        startOfToday: Long,
        endOfToday: Long
    ): List<CalendarDayItem> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = monthMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))
        val result = mutableListOf<CalendarDayItem>()

        val standaloneInstallments = allInstallments.filter { it.invoiceId == null }

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val dayEnd = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            val dayInsts = standaloneInstallments.filter { it.dueDate in dayStart..dayEnd }
            val dayInvs = allInvoices.filter { it.dueDate in dayStart..dayEnd }
            val allDayItemsCount = dayInsts.size + dayInvs.size

            val dayOfWeek = dayFormat.format(Date(dayStart)).replace(".", "").uppercase(Locale("pt", "BR"))

            if (allDayItemsCount == 0) {
                result.add(
                    CalendarDayItem(
                        dateMillis = dayStart,
                        dayOfMonth = day,
                        dayOfWeekLabel = dayOfWeek,
                        hasOverdue = false,
                        hasDueToday = false,
                        hasPending = false,
                        isFullyPaid = false,
                        totalAmountCents = 0L,
                        itemsCount = 0
                    )
                )
                continue
            }

            val hasOverdue = dayInsts.any { !it.isPaid && it.dueDate < startOfToday } ||
                    dayInvs.any { it.status != InvoiceStatus.PAGA && it.dueDate < startOfToday }

            val hasDueToday = dayInsts.any { !it.isPaid && it.dueDate in startOfToday..endOfToday } ||
                    dayInvs.any { it.status != InvoiceStatus.PAGA && it.dueDate in startOfToday..endOfToday }

            val hasPending = dayInsts.any { !it.isPaid && it.dueDate > endOfToday } ||
                    dayInvs.any { it.status != InvoiceStatus.PAGA && it.dueDate > endOfToday }

            val isFullyPaid = allDayItemsCount > 0 &&
                    dayInsts.all { it.isPaid } &&
                    dayInvs.all { it.status == InvoiceStatus.PAGA }

            val totalAmount = dayInsts.sumOf { it.amountCents } + dayInvs.sumOf { it.totalAmountCents }

            result.add(
                CalendarDayItem(
                    dateMillis = dayStart,
                    dayOfMonth = day,
                    dayOfWeekLabel = dayOfWeek,
                    hasOverdue = hasOverdue,
                    hasDueToday = hasDueToday,
                    hasPending = hasPending,
                    isFullyPaid = isFullyPaid,
                    totalAmountCents = totalAmount,
                    itemsCount = allDayItemsCount
                )
            )
        }

        return result
    }

    private fun filterPayablesForDay(
        dayStartMillis: Long,
        allInstallments: List<BillInstallment>,
        allInvoices: List<CreditCardInvoice>,
        cardsMap: Map<String, CreditCard>
    ): List<PayableItem> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis
        val sevenDaysAhead = endOfToday + (7L * 24 * 3600 * 1000)

        cal.timeInMillis = dayStartMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val dayEnd = cal.timeInMillis

        val insts = allInstallments
            .filter { it.invoiceId == null && it.dueDate in dayStartMillis..dayEnd }
            .map { inst ->
                val urgency = when {
                    inst.isPaid -> PayableUrgency.PAID
                    inst.dueDate < startOfToday -> PayableUrgency.OVERDUE
                    inst.dueDate in startOfToday..endOfToday -> PayableUrgency.DUE_TODAY
                    inst.dueDate in (endOfToday + 1)..sevenDaysAhead -> PayableUrgency.NEXT_7_DAYS
                    else -> PayableUrgency.LATER
                }
                PayableItem.BillPayable(inst, urgency)
            }

        val invs = allInvoices
            .filter { it.dueDate in dayStartMillis..dayEnd }
            .map { inv ->
                val card = cardsMap[inv.creditCardId]
                val cardName = card?.name ?: "Cartão"
                val cardColor = card?.colorHex ?: "#3B82F6"
                val urgency = when {
                    inv.status == InvoiceStatus.PAGA -> PayableUrgency.PAID
                    inv.dueDate < startOfToday -> PayableUrgency.OVERDUE
                    inv.dueDate in startOfToday..endOfToday -> PayableUrgency.DUE_TODAY
                    inv.dueDate in (endOfToday + 1)..sevenDaysAhead -> PayableUrgency.NEXT_7_DAYS
                    else -> PayableUrgency.LATER
                }
                PayableItem.InvoicePayable(inv, cardName, cardColor, urgency)
            }

        return (insts + invs).sortedBy { it.dueDate }
    }
}
