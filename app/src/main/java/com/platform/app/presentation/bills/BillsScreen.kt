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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import com.platform.app.domain.model.Subcategory
import java.util.Calendar
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.presentation.components.PlatformAppBar
import kotlinx.coroutines.flow.Flow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
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
    onOpenDrawer: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var installmentToViewDetails by remember { mutableStateOf<BillInstallment?>(null) }

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
                            text = "Registros",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu lateral")
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
                                    .width(230.dp)
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
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Conta")
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
            // Filtros de Período, Tipo e Status
            FilterChipsRow(
                periodFilter = uiState.periodFilter,
                typeFilter = uiState.typeFilter,
                statusFilter = uiState.statusFilter,
                onPeriodFilterChange = { onAction(BillsUiAction.PeriodFilterChanged(it)) },
                onTypeFilterChange = { onAction(BillsUiAction.TypeFilterChanged(it)) },
                onStatusFilterChange = { onAction(BillsUiAction.StatusFilterChanged(it)) }
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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.filteredInstallments, key = { it.id }) { installment ->
                                BillInstallmentItemCard(
                                    installment = installment,
                                    onTogglePayment = { onAction(BillsUiAction.TogglePayment(installment)) },
                                    onSelectInstallment = { installmentToViewDetails = installment }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }

                if (showAddSheet) {
                    AddBillBottomSheet(
                        categories = uiState.categories,
                        subcategories = uiState.subcategories,
                        contacts = uiState.contacts,
                        financialAccounts = uiState.financialAccounts,
                        paymentMethods = uiState.paymentMethods,
                        sheetState = sheetState,
                        onDismiss = { showAddSheet = false },
                        onConfirm = { title, desc, type, totalCents, catId, subcatId, contactId, accountId, paymentMethodId, totalInst, dueDate ->
                            onAction(
                                BillsUiAction.CreateBill(
                                    title = title,
                                    description = desc,
                                    type = type,
                                    totalAmountCents = totalCents,
                                    categoryId = catId,
                                    subcategoryId = subcatId,
                                    contactId = contactId,
                                    financialAccountId = accountId,
                                    paymentMethodId = paymentMethodId,
                                    totalInstallments = totalInst,
                                    firstDueDate = dueDate
                                )
                            )
                            showAddSheet = false
                        }
                    )
                }

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
}

@Composable
fun FilterChipsRow(
    periodFilter: BillPeriodFilter,
    typeFilter: BillType?,
    statusFilter: BillStatus?,
    onPeriodFilterChange: (BillPeriodFilter) -> Unit,
    onTypeFilterChange: (BillType?) -> Unit,
    onStatusFilterChange: (BillStatus?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                label = { Text("Todas as Contas") }
            )
        }

        // Status
        item {
            FilterChip(
                selected = statusFilter == BillStatus.PENDING,
                onClick = { onStatusFilterChange(if (statusFilter == BillStatus.PENDING) null else BillStatus.PENDING) },
                label = { Text("A Pagar") }
            )
        }
        item {
            FilterChip(
                selected = statusFilter == BillStatus.PAID,
                onClick = { onStatusFilterChange(if (statusFilter == BillStatus.PAID) null else BillStatus.PAID) },
                label = { Text("Pagas") }
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

@Composable
fun BillInstallmentItemCard(
    installment: BillInstallment,
    onTogglePayment: () -> Unit,
    onSelectInstallment: () -> Unit
) {
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis()

    val catColor = remember(installment.categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            Color(0xFF64748B)
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (installment.isPaid)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (installment.isPaid) 0.dp else 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectInstallment() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = installment.isPaid,
                onCheckedChange = { onTogglePayment() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    uncheckedColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

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

    val catColor = remember(installment.categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            Color(0xFF64748B)
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
                        imageVector = Icons.Default.ReceiptLong,
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
                                text = if (!installment.subcategoryName.isNullOrBlank())
                                    "${installment.categoryName} (${installment.subcategoryName})"
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
        BillType.INSTALLMENT -> Triple("${installment.installmentNumber}/${installment.totalInstallments}", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706))
        BillType.RECURRING -> Triple("Recorrente", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillBottomSheet(
    categories: List<Category>,
    subcategories: List<Subcategory>,
    contacts: List<Contact>,
    financialAccounts: List<FinancialAccount>,
    paymentMethods: List<PaymentMethod>,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        type: BillType,
        amountCents: Long,
        categoryId: String?,
        subcategoryId: String?,
        contactId: String?,
        accountId: String?,
        paymentMethodId: String?,
        totalInstallments: Int,
        dueDate: Long
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BillType.SINGLE) }
    var installmentsCountText by remember { mutableStateOf("2") }
    var selectedCategoryId by remember { mutableStateOf<String?>(categories.firstOrNull()?.id) }
    var selectedSubcategoryId by remember { mutableStateOf<String?>(null) }
    var selectedContactId by remember { mutableStateOf<String?>(null) }
    var selectedAccountId by remember { mutableStateOf<String?>(financialAccounts.firstOrNull()?.id) }
    var selectedPaymentMethodId by remember { mutableStateOf<String?>(paymentMethods.firstOrNull()?.id) }
    var dueDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showValidationError by remember { mutableStateOf(false) }

    val filteredSubcategories = remember(selectedCategoryId, subcategories) {
        if (selectedCategoryId == null) emptyList()
        else subcategories.filter { it.categoryId == selectedCategoryId }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Elegante
            Column {
                Text(
                    text = "Nova Despesa",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Preencha as informações para organizar seus pagamentos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // CARD 1: Identificação
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. Identificação",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            showValidationError = false
                        },
                        label = { Text("Nome da Despesa *") },
                        placeholder = { Text("Ex: Aluguel, Internet, Supermercado...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        isError = showValidationError && title.isBlank(),
                        supportingText = if (showValidationError && title.isBlank()) {
                            { Text("O nome da conta é obrigatório", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Observações (opcional)") },
                        placeholder = { Text("Notas, código de barras ou detalhes adicionais...") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // CARD 2: Valor e Condição de Cobrança
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. Valor & Tipo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = rawAmount,
                        onValueChange = {
                            rawAmount = it
                            showValidationError = false
                        },
                        label = { Text("Valor Total (R$) *") },
                        placeholder = { Text("Ex: 150,00 ou 2500") },
                        prefix = {
                            Text(
                                text = "R$ ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = showValidationError && CurrencyUtils.parseInputToCents(rawAmount) <= 0L,
                        supportingText = if (showValidationError && CurrencyUtils.parseInputToCents(rawAmount) <= 0L) {
                            { Text("Informe um valor maior que zero", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Tipo de Despesa:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BillType.values().forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (selectedType == BillType.INSTALLMENT) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Número de Parcelas:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                            val commonInstallments = listOf("2", "3", "6", "10", "12")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                commonInstallments.forEach { count ->
                                    FilterChip(
                                        selected = installmentsCountText == count,
                                        onClick = { installmentsCountText = count },
                                        label = { Text("${count}x") }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = installmentsCountText,
                                onValueChange = { installmentsCountText = it.filter { c -> c.isDigit() } },
                                label = { Text("Outra quantidade de parcelas (ex: 18)") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // CARD 3: Vencimento & Planejamento
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "3. Vencimento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Primeiro Vencimento",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = DateUtils.formatDate(dueDateMillis),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Atalhos rápidos
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val now = System.currentTimeMillis()
                        item {
                            FilterChip(
                                selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(now),
                                onClick = { dueDateMillis = now },
                                label = { Text("Hoje") }
                            )
                        }
                        item {
                            val plus7 = now + 7L * 24 * 3600 * 1000
                            FilterChip(
                                selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(plus7),
                                onClick = { dueDateMillis = plus7 },
                                label = { Text("+7 dias") }
                            )
                        }
                        item {
                            val plus15 = now + 15L * 24 * 3600 * 1000
                            FilterChip(
                                selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(plus15),
                                onClick = { dueDateMillis = plus15 },
                                label = { Text("+15 dias") }
                            )
                        }
                        item {
                            val plus30 = now + 30L * 24 * 3600 * 1000
                            FilterChip(
                                selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(plus30),
                                onClick = { dueDateMillis = plus30 },
                                label = { Text("+30 dias") }
                            )
                        }
                    }
                }
            }

            // CARD 4: Vínculos e Classificação
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "4. Classificação & Origem",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Categoria
                    Text(
                        text = "Categoria:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories, key = { it.id }) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = {
                                    selectedCategoryId = cat.id
                                    selectedSubcategoryId = null
                                },
                                label = { Text(cat.name) }
                            )
                        }
                    }

                    // Subcategoria
                    if (filteredSubcategories.isNotEmpty()) {
                        Text(
                            text = "Subcategoria:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedSubcategoryId == null,
                                    onClick = { selectedSubcategoryId = null },
                                    label = { Text("Nenhuma") }
                                )
                            }
                            items(filteredSubcategories, key = { it.id }) { sub ->
                                FilterChip(
                                    selected = selectedSubcategoryId == sub.id,
                                    onClick = { selectedSubcategoryId = sub.id },
                                    label = { Text(sub.name) }
                                )
                            }
                        }
                    }

                    // Contato
                    if (contacts.isNotEmpty()) {
                        Text(
                            text = "Contato / Favorecido:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedContactId == null,
                                    onClick = { selectedContactId = null },
                                    label = { Text("Nenhum") }
                                )
                            }
                            items(contacts, key = { it.id }) { c ->
                                FilterChip(
                                    selected = selectedContactId == c.id,
                                    onClick = { selectedContactId = c.id },
                                    label = { Text(c.name) }
                                )
                            }
                        }
                    }

                    // Conta de Saída
                    if (financialAccounts.isNotEmpty()) {
                        Text(
                            text = "Conta de Débito:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedAccountId == null,
                                    onClick = { selectedAccountId = null },
                                    label = { Text("Nenhuma") }
                                )
                            }
                            items(financialAccounts, key = { it.id }) { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name) }
                                )
                            }
                        }
                    }

                    // Forma de Pagamento
                    if (paymentMethods.isNotEmpty()) {
                        Text(
                            text = "Forma de Pagamento:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedPaymentMethodId == null,
                                    onClick = { selectedPaymentMethodId = null },
                                    label = { Text("Nenhuma") }
                                )
                            }
                            items(paymentMethods, key = { it.id }) { pm ->
                                FilterChip(
                                    selected = selectedPaymentMethodId == pm.id,
                                    onClick = { selectedPaymentMethodId = pm.id },
                                    label = { Text(pm.name) }
                                )
                            }
                        }
                    }
                }
            }

            // Ações / Botões
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Cancelar", style = MaterialTheme.typography.titleMedium)
                }

                Button(
                    onClick = {
                        val amountCents = CurrencyUtils.parseInputToCents(rawAmount)
                        val totalInst = if (selectedType == BillType.INSTALLMENT)
                            installmentsCountText.toIntOrNull()?.coerceAtLeast(2) ?: 2
                        else
                            1

                        if (title.isBlank() || amountCents <= 0L) {
                            showValidationError = true
                        } else {
                            onConfirm(
                                title.trim(),
                                description.trim(),
                                selectedType,
                                amountCents,
                                selectedCategoryId,
                                selectedSubcategoryId,
                                selectedContactId,
                                selectedAccountId,
                                selectedPaymentMethodId,
                                totalInst,
                                dueDateMillis
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Cadastrar Conta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun EmptyBillsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "💳", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhuma conta encontrada",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Toque no botão '+' abaixo para cadastrar sua primeira conta avulsa, parcelada ou recorrente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
