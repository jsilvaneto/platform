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
import com.platform.app.presentation.bills.components.BillsFilterBar
import com.platform.app.presentation.bills.components.BillsMiniKpiBar
import com.platform.app.presentation.bills.components.EmptyBillsState
import com.platform.app.presentation.bills.components.BatchDeleteDialog
import com.platform.app.presentation.bills.components.BatchSetActualPaymentDateDialog
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

    // Estado do modo de seleção múltipla em lote
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedInstallmentIds by remember { mutableStateOf(setOf<String>()) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showBatchSetActualPaymentDialog by remember { mutableStateOf(false) }
    var installmentToConfirmPayment by remember { mutableStateOf<BillInstallment?>(null) }
    val currentMonthLabel = remember { DateUtils.formatMonthYear(System.currentTimeMillis()) }
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
                title = if (isSelectionMode) "${selectedInstallmentIds.size} selecionado(s)" else "Registros",
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
                onOpenDrawer = onOpenDrawer,
                actions = {
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
            // 1. Segmented Tabs Superiores: A Pagar | Pagas | Todas
            val pendingCount = remember(uiState.installments) { uiState.installments.count { !it.isPaid } }
            val paidCount = remember(uiState.installments) { uiState.installments.count { it.isPaid } }
            val totalCount = remember(uiState.installments) { uiState.installments.size }

            val tabItems = remember(pendingCount, paidCount, totalCount) {
                listOf(
                    SegmentedTabItem("A Pagar", pendingCount),
                    SegmentedTabItem("Pagas", paidCount),
                    SegmentedTabItem("Todas", totalCount)
                )
            }

            val selectedTabIndex = when (uiState.statusFilter) {
                BillStatus.PENDING -> 0
                BillStatus.PAID -> 1
                null -> 2
                else -> 0
            }

            PlatformSegmentedTabs(
                items = tabItems,
                selectedIndex = selectedTabIndex,
                onTabSelected = { index ->
                    when (index) {
                        0 -> onAction(BillsUiAction.StatusFilterChanged(BillStatus.PENDING))
                        1 -> onAction(BillsUiAction.StatusFilterChanged(BillStatus.PAID))
                        2 -> onAction(BillsUiAction.StatusFilterChanged(null))
                    }
                },
                modifier = Modifier.padding(horizontal = Dimens.spacingNormal, vertical = 6.dp)
            )

            // 2. Barra de Filtros Compactos e Interativos (Ano, Período, Tipo + Limpar)
            BillsFilterBar(
                selectedYear = uiState.selectedYear,
                availableYears = uiState.availableYears,
                periodFilter = uiState.periodFilter,
                typeFilter = uiState.typeFilter,
                onYearChange = { onAction(BillsUiAction.YearChanged(it)) },
                onPeriodChange = { onAction(BillsUiAction.PeriodFilterChanged(it)) },
                onTypeChange = { onAction(BillsUiAction.TypeFilterChanged(it)) },
                onResetFilters = { onAction(BillsUiAction.ResetFilters) }
            )

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

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (groupedByMonth.size > 1) {
                                item(key = "toggle_all_months") {
                                    val allExpanded = groupedByMonth.keys.isNotEmpty() && groupedByMonth.keys.all { it in expandedMonths }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${groupedByMonth.size} meses listados",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TextButton(
                                            onClick = {
                                                expandedMonths = if (allExpanded) {
                                                    emptySet()
                                                } else {
                                                    groupedByMonth.keys.toSet()
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (allExpanded) "Recolher todos" else "Expandir todos",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            groupedByMonth.forEach { (monthLabel, monthItems) ->
                                val isExpanded = monthLabel in expandedMonths
                                val isCurrentMonth = monthLabel.equals(currentMonthLabel, ignoreCase = true)

                                item(key = "header_$monthLabel") {
                                    val monthTotal = remember(monthItems) { monthItems.sumOf { it.amountCents } }
                                    BillsMonthSectionHeader(
                                        monthLabel = monthLabel,
                                        monthTotal = monthTotal,
                                        itemCount = monthItems.size,
                                        isExpanded = isExpanded,
                                        isCurrentMonth = isCurrentMonth,
                                        onToggleExpand = {
                                            expandedMonths = if (isExpanded) {
                                                expandedMonths - monthLabel
                                            } else {
                                                expandedMonths + monthLabel
                                            }
                                        }
                                    )
                                }

                                if (isExpanded) {
                                    items(monthItems, key = { it.id }) { installment ->
                                        val isSelected = installment.id in selectedInstallmentIds

                                        BillInstallmentItemCard(
                                            installment = installment,
                                            isSelectionMode = isSelectionMode,
                                            isSelected = isSelected,
                                            onToggleSelect = {
                                                selectedInstallmentIds = if (isSelected) {
                                                    selectedInstallmentIds - installment.id
                                                } else {
                                                    selectedInstallmentIds + installment.id
                                                }
                                            },
                                            onLongClick = {
                                                isSelectionMode = true
                                                selectedInstallmentIds = selectedInstallmentIds + installment.id
                                            },
                                            onTogglePayment = {
                                                if (installment.isPaid) {
                                                    onAction(BillsUiAction.TogglePayment(installment))
                                                } else {
                                                    installmentToConfirmPayment = installment
                                                }
                                            },
                                            onSelectInstallment = {
                                                if (isSelectionMode) {
                                                    selectedInstallmentIds = if (isSelected) {
                                                        selectedInstallmentIds - installment.id
                                                    } else {
                                                        selectedInstallmentIds + installment.id
                                                    }
                                                } else {
                                                    // 2- Ao tocar em um registro: abrir janela para editar
                                                    onAction(BillsUiAction.OpenEditInstallment(installment))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(84.dp))
                            }
                        }
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
