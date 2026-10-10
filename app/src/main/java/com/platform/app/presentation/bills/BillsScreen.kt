package com.platform.app.presentation.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.platform.app.presentation.bills.components.BillInstallmentItemCard
import com.platform.app.presentation.bills.components.BillsFilterBottomSheet
import com.platform.app.presentation.bills.components.BillsInstallmentList
import com.platform.app.presentation.bills.components.BillsMiniKpiBar
import com.platform.app.presentation.bills.components.BillsMonthSectionHeader
import com.platform.app.presentation.bills.components.EmptyBillsState
import com.platform.app.presentation.bills.components.BatchDeleteDialog
import com.platform.app.presentation.bills.components.BatchSetActualPaymentDateDialog
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import androidx.compose.foundation.verticalScroll
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.ConfirmPaymentDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.ExpenseItem
import java.util.Calendar
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformEmptyState
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.components.PlatformSearchTopBar
import com.platform.app.presentation.components.PlatformBatchActionBar
import kotlinx.coroutines.flow.Flow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    uiState: BillsUiState,
    uiEffect: Flow<BillsUiEffect>,
    onAction: (BillsUiAction) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {},
    onNavigateToNewExpense: () -> Unit = {},
    onNavigateToDuplicate: (String) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    // Estado do modo de seleção múltipla em lote
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedInstallmentIds by remember { mutableStateOf(setOf<String>()) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showBatchSetActualPaymentDialog by remember { mutableStateOf(false) }
    var installmentToConfirmPayment by remember { mutableStateOf<BillInstallment?>(null) }
    val currentMonthLabel = remember { DateUtils.formatMonthYear(System.currentTimeMillis()) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val hasActiveFilters = uiState.statusFilter != null ||
            uiState.periodFilter != BillPeriodFilter.ALL ||
            (uiState.selectedYear != null && uiState.selectedYear != currentYear)
    var expandedMonths by rememberSaveable { mutableStateOf(setOf(DateUtils.formatMonthYear(System.currentTimeMillis()))) }

    LaunchedEffect(uiEffect) {
        uiEffect.collect { effect ->
            when (effect) {
                is BillsUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PlatformSearchTopBar(
                title = if (isSelectionMode) "${selectedInstallmentIds.size} selecionado(s)" else "Contas",
                searchQuery = uiState.searchQuery,
                isSearchActive = isSearchExpanded,
                onSearchQueryChange = { onAction(BillsUiAction.SearchQueryChanged(it)) },
                onSearchActiveChange = { isSearchExpanded = it },
                placeholder = "Buscar conta ou categoria...",
                navigationIcon = if (isSelectionMode) {
                    {
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedInstallmentIds = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar seleção",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else null,
                onOpenDrawer = null,
                actions = {
                    if (!isSelectionMode) {
                        IconButton(onClick = { showFilterBottomSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (hasActiveFilters) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtrar contas",
                                    tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            isSelectionMode = !isSelectionMode
                            if (!isSelectionMode) {
                                selectedInstallmentIds = emptySet()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Modo de seleção múltipla",
                            tint = if (isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onNavigateToNewExpense,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Despesa")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Segmented Tabs: Todas | Recorrentes | Parceladas
            val totalCount = remember(uiState.installments) { uiState.installments.size }
            val recurringCount = remember(uiState.installments) { uiState.installments.count { it.type == BillType.RECURRING } }
            val installmentCount = remember(uiState.installments) { uiState.installments.count { it.type == BillType.INSTALLMENT } }

            val tabItems = remember(totalCount, recurringCount, installmentCount) {
                listOf(
                    SegmentedTabItem("Todas", totalCount),
                    SegmentedTabItem("Recorrentes", recurringCount),
                    SegmentedTabItem("Parceladas", installmentCount)
                )
            }

            val selectedTabIndex = when (uiState.typeFilter) {
                null -> 0
                BillType.RECURRING -> 1
                BillType.INSTALLMENT -> 2
                else -> 0
            }

            PlatformSegmentedTabs(
                items = tabItems,
                selectedIndex = selectedTabIndex,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onAction(BillsUiAction.TypeFilterChanged(null))
                        1 -> onAction(BillsUiAction.TypeFilterChanged(BillType.RECURRING))
                        2 -> onAction(BillsUiAction.TypeFilterChanged(BillType.INSTALLMENT))
                    }
                },
                modifier = Modifier.padding(horizontal = Dimens.spacingNormal, vertical = 6.dp)
            )

            // Resumo de filtros ativos (quando houver)
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterDesc = buildList {
                        uiState.statusFilter?.let { add(if (it == BillStatus.PAID) "Pagas" else "A Pagar") }
                        if (uiState.periodFilter != BillPeriodFilter.ALL) add(uiState.periodFilter.label)
                        if (uiState.selectedYear != null && uiState.selectedYear != currentYear) add("${uiState.selectedYear}")
                    }.joinToString(" • ")

                    Text(
                        text = "Filtros: $filterDesc",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onAction(BillsUiAction.ResetFilters) },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = "Limpar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showFilterBottomSheet) {
                BillsFilterBottomSheet(
                    selectedYear = uiState.selectedYear,
                    availableYears = uiState.availableYears,
                    periodFilter = uiState.periodFilter,
                    statusFilter = uiState.statusFilter,
                    onYearChange = { onAction(BillsUiAction.YearChanged(it)) },
                    onPeriodChange = { onAction(BillsUiAction.PeriodFilterChanged(it)) },
                    onStatusChange = { onAction(BillsUiAction.StatusFilterChanged(it)) },
                    onResetFilters = { onAction(BillsUiAction.ResetFilters) },
                    onDismissRequest = { showFilterBottomSheet = false }
                )
            }

            // 3. Mini KPI Strip Proporcional e Elegante
            BillsMiniKpiBar(
                totalCents = uiState.totalPeriodCents,
                paidCents = uiState.paidPeriodCents,
                pendingCents = uiState.pendingPeriodCents,
                overdueCents = uiState.overduePeriodCents,
                count = uiState.filteredInstallments.size,
                year = uiState.selectedYear
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.installments.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.filteredInstallments.isEmpty() -> {
                        EmptyBillsState()
                    }

                    else -> {
                        val groupedByMonth = remember(uiState.filteredInstallments) {
                            uiState.filteredInstallments.groupBy { inst ->
                                DateUtils.formatMonthYear(inst.dueDate)
                            }
                        }

                        BillsInstallmentList(
                            groupedByMonth = groupedByMonth,
                            expandedMonths = expandedMonths,
                            currentMonthLabel = currentMonthLabel,
                            isSelectionMode = isSelectionMode,
                            selectedInstallmentIds = selectedInstallmentIds,
                            onToggleMonthExpand = { monthLabel ->
                                expandedMonths = if (monthLabel in expandedMonths) {
                                    expandedMonths - monthLabel
                                } else {
                                    expandedMonths + monthLabel
                                }
                            },
                            onToggleAllMonths = { allExpanded ->
                                expandedMonths = if (allExpanded) emptySet() else groupedByMonth.keys.toSet()
                            },
                            onToggleSelect = { id ->
                                selectedInstallmentIds = if (id in selectedInstallmentIds) {
                                    selectedInstallmentIds - id
                                } else {
                                    selectedInstallmentIds + id
                                }
                            },
                            onLongClickSelect = { id ->
                                isSelectionMode = true
                                selectedInstallmentIds = selectedInstallmentIds + id
                            },
                            onTogglePayment = { installment ->
                                if (installment.isPaid) {
                                    onAction(BillsUiAction.TogglePayment(installment))
                                } else {
                                    installmentToConfirmPayment = installment
                                }
                            },
                            onSelectInstallment = { installment ->
                                if (isSelectionMode) {
                                    selectedInstallmentIds = if (installment.id in selectedInstallmentIds) {
                                        selectedInstallmentIds - installment.id
                                    } else {
                                        selectedInstallmentIds + installment.id
                                    }
                                } else {
                                    onAction(BillsUiAction.OpenEditInstallment(installment))
                                }
                            }
                        )
                    }
                }

                // Barra Flutuante de Ações em Lote
                val selectedTotalCents = remember(selectedInstallmentIds, uiState.installments) {
                    uiState.installments
                        .filter { it.id in selectedInstallmentIds }
                        .sumOf { it.amountCents }
                }

                PlatformBatchActionBar(
                    visible = isSelectionMode && selectedInstallmentIds.isNotEmpty(),
                    selectedCount = selectedInstallmentIds.size,
                    totalCents = selectedTotalCents,
                    onPayBatch = {
                        val idsToPay = selectedInstallmentIds.toList()
                        onAction(BillsUiAction.PayBatch(idsToPay))
                        selectedInstallmentIds = emptySet()
                        isSelectionMode = false
                    },
                    onDeleteBatch = {
                        showBatchDeleteDialog = true
                    },
                    onSetActualPaymentDate = if (selectedInstallmentIds.any { id ->
                            uiState.installments.find { it.id == id }?.isPaid == true
                        }) {
                        { showBatchSetActualPaymentDialog = true }
                    } else null,
                    onClearSelection = {
                        selectedInstallmentIds = emptySet()
                        isSelectionMode = false
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // ModalBottomSheet para Edição Completa ao Tocar no Registro (Ponto 2)
                uiState.editingInstallment?.let { inst ->
                    EditInstallmentBottomSheet(
                        installment = inst,
                        categories = uiState.categories,
                        contacts = uiState.contacts,
                        financialAccounts = uiState.financialAccounts,
                        paymentMethods = uiState.paymentMethods,
                        onDismiss = { onAction(BillsUiAction.DismissEditInstallment) },
                        onSave = { title, desc, amount, due, catId, itemId, contId, accId, methId ->
                            onAction(
                                BillsUiAction.SaveInstallmentEdit(
                                    installmentId = inst.id,
                                    billId = inst.billId,
                                    title = title,
                                    description = desc,
                                    amountCents = amount,
                                    dueDate = due,
                                    categoryId = catId,
                                    itemId = itemId,
                                    contactId = contId,
                                    financialAccountId = accId,
                                    paymentMethodId = methId
                                )
                            )
                        },
                        onDelete = { billId ->
                            onAction(BillsUiAction.DismissEditInstallment)
                            onAction(BillsUiAction.DeleteBill(billId))
                        },
                        onTogglePayment = {
                            if (inst.isPaid) {
                                onAction(BillsUiAction.TogglePayment(inst))
                            } else {
                                installmentToConfirmPayment = inst
                            }
                        }
                    )
                }
            }
        }
    }

    if (showBatchDeleteDialog) {
        BatchDeleteDialog(
            selectedCount = selectedInstallmentIds.size,
            onConfirm = {
                val billIds = uiState.installments
                    .filter { it.id in selectedInstallmentIds }
                    .map { it.billId }
                onAction(BillsUiAction.DeleteBatch(billIds))
                selectedInstallmentIds = emptySet()
                isSelectionMode = false
                showBatchDeleteDialog = false
            },
            onDismiss = { showBatchDeleteDialog = false }
        )
    }

    if (showBatchSetActualPaymentDialog) {
        BatchSetActualPaymentDateDialog(
            selectedCount = selectedInstallmentIds.size,
            onConfirm = { actualPaymentDate ->
                onAction(
                    BillsUiAction.BatchSetActualPaymentDate(
                        installmentIds = selectedInstallmentIds.toList(),
                        actualPaymentDate = actualPaymentDate
                    )
                )
                selectedInstallmentIds = emptySet()
                isSelectionMode = false
                showBatchSetActualPaymentDialog = false
            },
            onDismiss = { showBatchSetActualPaymentDialog = false }
        )
    }

    installmentToConfirmPayment?.let { inst ->
        ConfirmPaymentDialog(
            installmentTitle = "${inst.billTitle} (${inst.installmentNumber}/${inst.totalInstallments})",
            amountCents = inst.amountCents,
            dueDate = inst.dueDate,
            onConfirm = { actualPaymentDate ->
                onAction(BillsUiAction.TogglePayment(inst, actualPaymentDate))
                installmentToConfirmPayment = null
            },
            onDismiss = {
                installmentToConfirmPayment = null
            }
        )
    }
}
