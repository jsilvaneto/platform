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
import androidx.compose.foundation.rememberScrollState
import com.platform.app.presentation.theme.Dimens
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
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
    onNavigateToNewExpense: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var installmentToViewDetails by remember { mutableStateOf<BillInstallment?>(null) }

    // Estado do modo de seleção múltipla em lote
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedInstallmentIds by remember { mutableStateOf(setOf<String>()) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            focusRequester.requestFocus()
        }
    }

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
            TopAppBar(
                title = {
                    if (!isSearchExpanded) {
                        Text(
                            text = if (isSelectionMode) "${selectedInstallmentIds.size} selecionado(s)" else "Registros",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                isSelectionMode = false
                                selectedInstallmentIds = emptySet()
                            } else {
                                onOpenDrawer()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = if (isSelectionMode) "Fechar seleção" else "Menu lateral"
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        AnimatedVisibility(
                            visible = isSearchExpanded,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { onAction(BillsUiAction.SearchQueryChanged(it)) },
                                placeholder = {
                                    Text(
                                        text = "Buscar conta ou categoria...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { onAction(BillsUiAction.SearchQueryChanged("")) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Limpar busca",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(210.dp)
                                    .height(46.dp)
                                    .focusRequester(focusRequester)
                                    .padding(end = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) {
                                    onAction(BillsUiAction.SearchQueryChanged(""))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchExpanded) "Fechar busca" else "Buscar contas",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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

            // 2. Filtros Secundários Refinados: Período e Tipo
            FilterChipsRow(
                periodFilter = uiState.periodFilter,
                typeFilter = uiState.typeFilter,
                onPeriodFilterChange = { onAction(BillsUiAction.PeriodFilterChanged(it)) },
                onTypeFilterChange = { onAction(BillsUiAction.TypeFilterChanged(it)) }
            )

            // 3. Banner de Totais Filtrados
            val filteredTotalCents = remember(uiState.filteredInstallments) {
                uiState.filteredInstallments.sumOf { it.amountCents }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingNormal, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.filteredInstallments.size} ${if (uiState.filteredInstallments.size == 1) "registro" else "registros"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ${CurrencyUtils.formatCentsToCurrency(filteredTotalCents)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.filteredInstallments, key = { it.id }) { installment ->
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
                                    onTogglePayment = { onAction(BillsUiAction.TogglePayment(installment)) },
                                    onSelectInstallment = {
                                        if (isSelectionMode) {
                                            selectedInstallmentIds = if (isSelected) {
                                                selectedInstallmentIds - installment.id
                                            } else {
                                                selectedInstallmentIds + installment.id
                                            }
                                        } else {
                                            installmentToViewDetails = installment
                                        }
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(84.dp))
                            }
                        }
                    }
                }

                // 4. Barra Flutuante de Ações em Lote
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

                installmentToViewDetails?.let { inst ->
                    val currentInstallment = uiState.installments.find { it.id == inst.id } ?: inst
                    BillInstallmentDetailBottomSheet(
                        installment = currentInstallment,
                        onDismiss = { installmentToViewDetails = null },
                        onTogglePayment = {
                            onAction(BillsUiAction.TogglePayment(currentInstallment))
                        },
                        onDelete = { billId ->
                            installmentToViewDetails = null
                            onAction(BillsUiAction.DeleteBill(billId))
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
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun FilterChipsRow(
    periodFilter: BillPeriodFilter,
    typeFilter: BillType?,
    onPeriodFilterChange: (BillPeriodFilter) -> Unit,
    onTypeFilterChange: (BillType?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Período
        item {
            FilterChip(
                selected = periodFilter == BillPeriodFilter.THIS_MONTH,
                onClick = { onPeriodFilterChange(BillPeriodFilter.THIS_MONTH) },
                label = { Text("Este Mês") }
            )
        }
        item {
            FilterChip(
                selected = periodFilter == BillPeriodFilter.NEXT_30_DAYS,
                onClick = { onPeriodFilterChange(BillPeriodFilter.NEXT_30_DAYS) },
                label = { Text("Próximos 30d") }
            )
        }
        item {
            FilterChip(
                selected = periodFilter == BillPeriodFilter.OVERDUE,
                onClick = { onPeriodFilterChange(BillPeriodFilter.OVERDUE) },
                label = { Text("Atrasadas") }
            )
        }
        item {
            FilterChip(
                selected = periodFilter == BillPeriodFilter.ALL,
                onClick = { onPeriodFilterChange(BillPeriodFilter.ALL) },
                label = { Text("Todo Período") }
            )
        }

        // Tipo
        item {
            FilterChip(
                selected = typeFilter == BillType.SINGLE,
                onClick = { onTypeFilterChange(if (typeFilter == BillType.SINGLE) null else BillType.SINGLE) },
                label = { Text("Avulsas") }
            )
        }
        item {
            FilterChip(
                selected = typeFilter == BillType.INSTALLMENT,
                onClick = { onTypeFilterChange(if (typeFilter == BillType.INSTALLMENT) null else BillType.INSTALLMENT) },
                label = { Text("Parceladas") }
            )
        }
        item {
            FilterChip(
                selected = typeFilter == BillType.RECURRING,
                onClick = { onTypeFilterChange(if (typeFilter == BillType.RECURRING) null else BillType.RECURRING) },
                label = { Text("Recorrentes") }
            )
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
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis()

    val fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    val catColor = remember(installment.categoryColorHex, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            fallbackColor
        }
    }

    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(Dimens.cardBorderWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    val cardColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        installment.isPaid -> MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.defaultElevation),
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
                .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                Checkbox(
                    checked = installment.isPaid,
                    onCheckedChange = { onTogglePayment() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SuccessGreen,
                        uncheckedColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = installment.billTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (installment.isPaid) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (installment.isPaid)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    InstallmentBadge(installment)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(catColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = installment.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = when {
                            installment.isPaid -> "Pago"
                            isOverdue -> "Venceu em ${DateUtils.formatDate(installment.dueDate)}"
                            else -> "Vence em ${DateUtils.formatDate(installment.dueDate)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            installment.isPaid -> SuccessGreen
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (installment.isPaid)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillInstallmentDetailBottomSheet(
    installment: BillInstallment,
    onDismiss: () -> Unit,
    onTogglePayment: () -> Unit,
    onDelete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis()

    val fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    val catColor = remember(installment.categoryColorHex, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            fallbackColor
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Avatar com cor da categoria, Título, Badges e Menu de 3 Pontos no Canto Superior Direito
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(catColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = installment.billTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (installment.isPaid) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InstallmentBadge(installment)

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                installment.isPaid -> SuccessGreen.copy(alpha = 0.12f)
                                isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            }
                        ) {
                            Text(
                                text = when {
                                    installment.isPaid -> "Pago"
                                    isOverdue -> "Atrasado"
                                    else -> "A Pagar"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    installment.isPaid -> SuccessGreen
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Canto Superior Direito: 3 Pontos com Opção de Alternar Pagamento e Excluir
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (installment.isPaid) "Marcar como Pendente" else "Marcar como Pago")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (installment.isPaid) Icons.Default.Schedule else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (installment.isPaid) MaterialTheme.colorScheme.tertiary else SuccessGreen
                                )
                            },
                            onClick = {
                                showMenu = false
                                onTogglePayment()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Excluir Lançamento", color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // Hero Card: Valor e Situação do Vencimento
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Valor do Lançamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (installment.isPaid)
                            SuccessGreen
                        else if (isOverdue)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vencimento: ${DateUtils.formatDate(installment.dueDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        if (installment.isPaid && installment.paidAt != null) {
                            Text(
                                text = "Liquidado em ${DateUtils.formatDate(installment.paidAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen
                            )
                        }
                    }

                    if (installment.type == BillType.INSTALLMENT && installment.totalInstallments > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = (installment.installmentNumber.toFloat() / installment.totalInstallments.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progresso do Parcelamento",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${installment.installmentNumber} de ${installment.totalInstallments}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Card: Detalhes e Relações Vinculadas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Vínculos & Classificação",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Categoria & Subcategoria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoria",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(catColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!installment.itemName.isNullOrBlank())
                                    "${installment.categoryName} (${installment.itemName})"
                                else
                                    installment.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Conta Financeira
                    if (!installment.financialAccountName.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Conta Vinculada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = installment.financialAccountName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Forma de Pagamento
                    if (!installment.paymentMethodName.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Forma de Pagamento",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = installment.paymentMethodName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Favorecido / Contato
                    if (!installment.contactName.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Favorecido / Fornecedor",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = installment.contactName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Tipo de Operação
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modalidade",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = when (installment.type) {
                                BillType.SINGLE -> "Lançamento Avulso"
                                BillType.INSTALLMENT -> "Parcelado (${installment.totalInstallments}x)"
                                BillType.RECURRING -> "Assinatura / Recorrente"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Ação Rápida no Rodapé: Marcar Pago ou Reabrir
            Button(
                onClick = {
                    onTogglePayment()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (installment.isPaid)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        SuccessGreen,
                    contentColor = if (installment.isPaid)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (installment.isPaid) Icons.Default.Schedule else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (installment.isPaid) "Reabrir Pagamento" else "Confirmar Pagamento",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Excluir Lançamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir o lançamento '${installment.billTitle}'? Esta ação removerá a conta e suas parcelas associadas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDismiss()
                        onDelete(installment.billId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InstallmentBadge(installment: BillInstallment) {
    val (label, bg, fg) = when (installment.type) {
        BillType.SINGLE -> Triple("Avulsa", MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), MaterialTheme.colorScheme.secondary)
        BillType.INSTALLMENT -> Triple("${installment.installmentNumber}/${installment.totalInstallments}", WarningAmber.copy(alpha = 0.15f), WarningAmber)
        BillType.RECURRING -> Triple("Recorrente", MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.primary)
    }

    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
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
