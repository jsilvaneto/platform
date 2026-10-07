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
                                    val rotationState by animateFloatAsState(
                                        targetValue = if (isExpanded) 180f else 0f,
                                        label = "monthCollapseRotation_$monthLabel"
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrentMonth) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        border = if (isCurrentMonth) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp, bottom = 2.dp)
                                            .clickable {
                                                expandedMonths = if (isExpanded) {
                                                    expandedMonths - monthLabel
                                                } else {
                                                    expandedMonths + monthLabel
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = if (isExpanded) "Recolher mês" else "Expandir mês",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .graphicsLayer(rotationZ = rotationState)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = monthLabel,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isCurrentMonth) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "Mês Atual",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = "${monthItems.size}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Subtotal: ${CurrencyUtils.formatCentsToCurrency(monthTotal)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
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
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = {
                Text(
                    text = "Excluir Selecionadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Tem certeza de que deseja excluir as ${selectedInstallmentIds.size} contas selecionadas?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val billIds = uiState.installments
                            .filter { it.id in selectedInstallmentIds }
                            .map { it.billId }
                        onAction(BillsUiAction.DeleteBatch(billIds))
                        selectedInstallmentIds = emptySet()
                        isSelectionMode = false
                        showBatchDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(AppStrings.Actions.DELETE)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBatchDeleteDialog = false }) {
                    Text(AppStrings.Actions.CANCEL)
                }
            }
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

@Composable
fun BillsFilterBar(
    selectedYear: Int?,
    availableYears: List<Int>,
    periodFilter: BillPeriodFilter,
    typeFilter: BillType?,
    onYearChange: (Int?) -> Unit,
    onPeriodChange: (BillPeriodFilter) -> Unit,
    onTypeChange: (BillType?) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var yearMenuExpanded by remember { mutableStateOf(false) }
    var periodMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val isYearFiltered = selectedYear != currentYear
    val isPeriodFiltered = periodFilter != BillPeriodFilter.ALL
    val isTypeFiltered = typeFilter != null
    val hasActiveFilters = isYearFiltered || isPeriodFiltered || isTypeFiltered

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Dropdown de Ano
        item {
            FilterDropdownChip(
                label = if (selectedYear != null) "$selectedYear" else "Ano: Todos",
                isSelected = isYearFiltered,
                onClick = { yearMenuExpanded = true }
            ) {
                DropdownMenu(
                    expanded = yearMenuExpanded,
                    onDismissRequest = { yearMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os Anos") },
                        onClick = {
                            onYearChange(null)
                            yearMenuExpanded = false
                        }
                    )
                    availableYears.forEach { year ->
                        val isCurrent = year == currentYear
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isCurrent) "$year (Atual)" else "$year",
                                    fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (year == selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onYearChange(year)
                                yearMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Dropdown de Período
        item {
            FilterDropdownChip(
                label = periodFilter.label,
                isSelected = isPeriodFiltered,
                onClick = { periodMenuExpanded = true }
            ) {
                DropdownMenu(
                    expanded = periodMenuExpanded,
                    onDismissRequest = { periodMenuExpanded = false }
                ) {
                    BillPeriodFilter.values().forEach { filter ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = filter.label,
                                    fontWeight = if (filter == periodFilter) FontWeight.Bold else FontWeight.Normal,
                                    color = if (filter == periodFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onPeriodChange(filter)
                                periodMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 3. Dropdown de Tipo
        item {
            val typeLabel = when (typeFilter) {
                BillType.SINGLE -> "Avulsas"
                BillType.INSTALLMENT -> "Parceladas"
                BillType.RECURRING -> "Recorrentes"
                null -> "Todos os Tipos"
            }

            FilterDropdownChip(
                label = typeLabel,
                isSelected = isTypeFiltered,
                onClick = { typeMenuExpanded = true }
            ) {
                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Todos os Tipos",
                                fontWeight = if (typeFilter == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(null)
                            typeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Avulsas",
                                fontWeight = if (typeFilter == BillType.SINGLE) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(BillType.SINGLE)
                            typeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Parceladas",
                                fontWeight = if (typeFilter == BillType.INSTALLMENT) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(BillType.INSTALLMENT)
                            typeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Recorrentes",
                                fontWeight = if (typeFilter == BillType.RECURRING) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(BillType.RECURRING)
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }

        // 4. Botão Limpar Filtros Ativos
        if (hasActiveFilters) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable(onClick = onResetFilters)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar filtros",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Limpar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDropdownChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        content()
    }
}

@Composable
fun BillsMiniKpiBar(
    totalCents: Long,
    paidCents: Long,
    pendingCents: Long,
    overdueCents: Long,
    count: Int,
    year: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (year != null) "Total no ano ($year):" else "Total geral:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(totalCents),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$count ${if (count == 1) "registro" else "registros"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pago
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(SuccessGreen, CircleShape))
                    Text(
                        text = "Pago: ${CurrencyUtils.formatCentsToCurrency(paidCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                }

                // Pendente
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Text(
                        text = "Pendente: ${CurrencyUtils.formatCentsToCurrency(pendingCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Atrasado (se houver)
                if (overdueCents > 0L) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                        Text(
                            text = "Atrasado: ${CurrencyUtils.formatCentsToCurrency(overdueCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillInstallmentItemCard(
    installment: BillInstallment,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onTogglePayment: () -> Unit,
    onSelectInstallment: () -> Unit
) {
    val isToday = !installment.isPaid && DateUtils.isToday(installment.dueDate)
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis() && !isToday

    val fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    val catColor = remember(installment.categoryColorHex, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            fallbackColor
        }
    }

    val cardBorder = when {
        isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        installment.isPaid -> BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
        isOverdue -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
        isToday -> BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val cardColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        installment.isPaid -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSelectInstallment,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox ou Botão Circular de Pagamento
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(34.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(onClick = onTogglePayment),
                    contentAlignment = Alignment.Center
                ) {
                    if (installment.isPaid) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(SuccessGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = AppStrings.Status.PAID,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color.Transparent,
                                    CircleShape
                                )
                                .border(
                                    width = 1.6.dp,
                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informações Centrais
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = installment.billTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Indicador de parcelamento se for parcelamento real (totalInstallments > 1)
                    if (installment.type == BillType.INSTALLMENT && installment.totalInstallments > 1) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${installment.installmentNumber}/${installment.totalInstallments}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Categoria
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = PlatformIconCatalog.getIcon(installment.categoryIconName),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = installment.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Lado Direito: Valor e Vencimento / Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                if (installment.isPaid) {
                    // Badge moderna e positiva "Pago" com micro ícone
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = AppStrings.Status.PAID,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                } else {
                    Text(
                        text = when {
                            isOverdue -> "Venceu ${DateUtils.formatDate(installment.dueDate)}"
                            isToday -> "Vence hoje"
                            else -> "Vence ${DateUtils.formatDate(installment.dueDate)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isOverdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isOverdue -> MaterialTheme.colorScheme.error
                            isToday -> WarningAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBillsState() {
    PlatformEmptyState(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = "Nenhuma conta encontrada",
        message = "Toque no botão '+' abaixo para cadastrar sua primeira conta avulsa, parcelada ou recorrente."
    )
}
