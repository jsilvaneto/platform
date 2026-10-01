package com.platform.app.presentation.recurring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformSearchTopBar
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringInstallmentsScreen(
    viewModel: RecurringInstallmentsViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToNewExpense: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var planToViewDetails by remember { mutableStateOf<BillWithInstallments?>(null) }

    // 0: Cronograma, 1: Compras Parceladas, 2: Assinaturas
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    var isSearchExpanded by remember { mutableStateOf(false) }

    val installmentItems = remember(uiState.filteredItems) {
        uiState.filteredItems.filter { it.bill.type == BillType.INSTALLMENT }
    }
    val recurringItems = remember(uiState.filteredItems) {
        uiState.filteredItems.filter { it.bill.type == BillType.RECURRING }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is RecurringInstallmentsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PlatformSearchTopBar(
                title = "Pagamentos Planejados",
                searchQuery = uiState.searchQuery,
                isSearchActive = isSearchExpanded,
                onSearchQueryChange = { viewModel.onAction(RecurringInstallmentsUiAction.SearchQueryChanged(it)) },
                onSearchActiveChange = { isSearchExpanded = it },
                placeholder = "Buscar plano...",
                onOpenDrawer = onOpenDrawer
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Despesa")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Segmented Tabs Superiores: Cronograma, Parcelados, Assinaturas
            val tabItems = remember(uiState.futureTimeline.size, installmentItems.size, recurringItems.size) {
                listOf(
                    SegmentedTabItem("Cronograma", uiState.futureTimeline.size),
                    SegmentedTabItem("Parcelados", installmentItems.size),
                    SegmentedTabItem("Assinaturas", recurringItems.size)
                )
            }

            PlatformSegmentedTabs(
                items = tabItems,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: CRONOGRAMA FUTURO
                    val totalTimelinePendingCents = remember(uiState.futureTimeline) {
                        uiState.futureTimeline.sumOf { it.pendingCents }
                    }
                    val totalTimelinePaidCents = remember(uiState.futureTimeline) {
                        uiState.futureTimeline.sumOf { it.paidCents }
                    }

                    PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Compromissos Futuros (A Pagar)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CurrencyUtils.formatCentsToCurrency(totalTimelinePendingCents),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${uiState.futureTimeline.size} meses mapeados",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (totalTimelinePaidCents > 0L) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Já quitado no período: ${CurrencyUtils.formatCentsToCurrency(totalTimelinePaidCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isLoading && uiState.futureTimeline.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (uiState.futureTimeline.isEmpty()) {
                        EmptyRecurringView(mode = 0)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(uiState.futureTimeline, key = { it.timestamp }) { month ->
                                TimelineMonthCard(
                                    month = month,
                                    allItems = uiState.items,
                                    onTogglePayment = { instId, paid ->
                                        viewModel.onAction(RecurringInstallmentsUiAction.TogglePayment(instId, paid))
                                    },
                                    onSelectPlan = { plan ->
                                        planToViewDetails = plan
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: COMPRAS PARCELADAS
                    // Filtros de Status (Todas, Em Andamento, Concluídas)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(RecurringStatusFilter.values()) { filter ->
                            FilterChip(
                                selected = uiState.statusFilter == filter,
                                onClick = { viewModel.onAction(RecurringInstallmentsUiAction.StatusFilterChanged(filter)) },
                                label = { Text(filter.label) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Resumo de Compras Parceladas com Verdade Financeira
                    val amortizedProgress = if (uiState.totalOriginalFinancedCents > 0L) {
                        uiState.totalPaidInstallmentsCents.toFloat() / uiState.totalOriginalFinancedCents.toFloat()
                    } else 0f

                    PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Saldo Devedor Restante",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CurrencyUtils.formatCentsToCurrency(uiState.totalActiveInstallmentsCents),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${(amortizedProgress * 100).toInt()}% amortizado",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            PlatformProgressBar(
                                progress = amortizedProgress,
                                height = 6.dp,
                                progressColor = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Já quitado: ${CurrencyUtils.formatCentsToCurrency(uiState.totalPaidInstallmentsCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Total financiado: ${CurrencyUtils.formatCentsToCurrency(uiState.totalOriginalFinancedCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isLoading && uiState.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (installmentItems.isEmpty()) {
                        EmptyRecurringView(mode = 1)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(installmentItems, key = { it.bill.id }) { item ->
                                BillPlanCard(
                                    item = item,
                                    onClick = { planToViewDetails = item }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: ASSINATURAS & CUSTOS FIXOS
                    PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Compromisso Mensal Fixo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CurrencyUtils.formatCentsToCurrency(uiState.totalMonthlyRecurringCents),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Projeção Anual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = CurrencyUtils.formatCentsToCurrency(uiState.totalMonthlyRecurringCents * 12),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Pago este mês: ${CurrencyUtils.formatCentsToCurrency(uiState.paidThisMonthRecurringCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Pendente este mês: ${CurrencyUtils.formatCentsToCurrency(uiState.pendingThisMonthRecurringCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isLoading && uiState.items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (recurringItems.isEmpty()) {
                        EmptyRecurringView(mode = 2)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(recurringItems, key = { it.bill.id }) { item ->
                                RecurringBillCard(
                                    item = item,
                                    onClick = { planToViewDetails = item }
                                )
                            }
                        }
                    }
                }
            }
        }

        planToViewDetails?.let { plan ->
            val currentPlan = uiState.items.find { it.bill.id == plan.bill.id } ?: plan
            RecurringDetailBottomSheet(
                item = currentPlan,
                onDismiss = { planToViewDetails = null },
                onTogglePayment = { instId, paid ->
                    viewModel.onAction(RecurringInstallmentsUiAction.TogglePayment(instId, paid))
                },
                onOpenAdjust = { inst ->
                    viewModel.onAction(RecurringInstallmentsUiAction.OpenAdjustInstallment(inst))
                },
                onDelete = {
                    planToViewDetails = null
                    viewModel.onAction(RecurringInstallmentsUiAction.DeleteBill(plan.bill.id))
                }
            )
        }

        uiState.installmentToAdjust?.let { instToAdjust ->
            AdjustInstallmentDialog(
                installment = instToAdjust,
                onDismiss = { viewModel.onAction(RecurringInstallmentsUiAction.DismissAdjustInstallment) },
                onSave = { instId, amount, due ->
                    viewModel.onAction(
                        RecurringInstallmentsUiAction.SaveAdjustInstallment(
                            installmentId = instId,
                            newAmountCents = amount,
                            newDueDate = due
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun BillPlanCard(
    item: BillWithInstallments,
    onClick: () -> Unit
) {
    val bill = item.bill
    val lastDueDate = item.installments.maxByOrNull { it.dueDate }?.dueDate
    val lastDueLabel = lastDueDate?.let { "Término em ${DateUtils.formatMonthYear(it)}" }

    PlatformCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Ícone, Título e Valor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!item.contactName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.contactName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${item.paidInstallmentsCount}/${bill.totalInstallments} pagas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(bill.totalAmountCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Resta: ${CurrencyUtils.formatCentsToCurrency(item.remainingCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Amortização com PlatformProgressBar
            Spacer(modifier = Modifier.height(10.dp))
            PlatformProgressBar(
                progress = item.progress,
                height = 6.dp,
                progressColor = if (item.progress >= 1f) SuccessGreen else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lastDueLabel != null) {
                    Text(
                        text = lastDueLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                item.nextInstallment?.let { next ->
                    val isNextToday = DateUtils.isToday(next.dueDate)
                    val isNextOverdue = next.dueDate < System.currentTimeMillis() && !isNextToday
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isNextOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            isNextToday -> WarningAmber.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        }
                    ) {
                        Text(
                            text = when {
                                isNextOverdue -> "Atrasada: ${DateUtils.formatDate(next.dueDate)}"
                                isNextToday -> "Vence hoje"
                                else -> "Próx: ${DateUtils.formatDate(next.dueDate)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isNextOverdue -> MaterialTheme.colorScheme.error
                                isNextToday -> WarningAmber
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringBillCard(
    item: BillWithInstallments,
    onClick: () -> Unit
) {
    val bill = item.bill

    PlatformCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Ícone, Título, Contato e Valor Recorrente
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Contato em destaque
                        if (!item.contactName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.contactName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Categoria
                        if (item.categoryName.isNotBlank() && item.categoryName != "Geral") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Coluna de Valor: Mensalidade Regular
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(item.regularAmountCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/mês",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Banner explicativo se houver primeira parcela diferenciada (ex: Adesão de 2000 vs 350)
            if (item.hasVariableFirstInstallment) {
                Spacer(modifier = Modifier.height(10.dp))
                val isFirstPending = item.installments.firstOrNull()?.isPaid == false
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFirstPending) WarningAmber.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.1f),
                    border = BorderStroke(
                        1.dp,
                        if (isFirstPending) WarningAmber.copy(alpha = 0.35f) else SuccessGreen.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFirstPending) Icons.Default.Info else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isFirstPending) WarningAmber else SuccessGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFirstPending) {
                                "1ª parcela (Entrada/Adesão): ${CurrencyUtils.formatCentsToCurrency(item.firstInstallmentAmountCents)} • Demais parcelas: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)}/mês"
                            } else {
                                "1ª parcela (${CurrencyUtils.formatCentsToCurrency(item.firstInstallmentAmountCents)}) já quitada • Mensalidade: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)}/mês"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isFirstPending) MaterialTheme.colorScheme.onSurface else SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Próximo Débito e Projeção Anual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.nextInstallment?.let { next ->
                    val isNextToday = DateUtils.isToday(next.dueDate)
                    val isNextOverdue = next.dueDate < System.currentTimeMillis() && !isNextToday
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            isNextOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            isNextToday -> WarningAmber.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ) {
                        Text(
                            text = when {
                                isNextOverdue -> "Atrasado: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)} em ${DateUtils.formatDate(next.dueDate)}"
                                isNextToday -> "Débito hoje: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)}"
                                else -> "Próximo débito: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)} em ${DateUtils.formatDate(next.dueDate)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isNextOverdue -> MaterialTheme.colorScheme.error
                                isNextToday -> WarningAmber
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } ?: run {
                    Text(
                        text = "Todas as cobranças quitadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Projeção: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents * 12)}/ano",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringDetailBottomSheet(
    item: BillWithInstallments,
    onDismiss: () -> Unit,
    onTogglePayment: (String, Boolean) -> Unit,
    onOpenAdjust: (BillInstallment) -> Unit = {},
    onDelete: () -> Unit
) {
    val bill = item.bill
    val isInstallment = bill.type == BillType.INSTALLMENT
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isAllPaid = item.paidInstallmentsCount == bill.totalInstallments && bill.totalInstallments > 0

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
            // Header: Avatar, Título, Badges e 3 Pontos no Canto Superior Direito
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isInstallment)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isInstallment) Icons.Default.CreditCard else Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = if (isInstallment) MaterialTheme.colorScheme.primary else SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isInstallment)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isInstallment) "Parcelamento" else "Recorrente",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isInstallment) MaterialTheme.colorScheme.primary else SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isInstallment) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isAllPaid) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (isAllPaid) "Totalmente Quitado" else "${item.paidInstallmentsCount}/${bill.totalInstallments} pagas",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAllPaid) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Contato em destaque
                        if (!item.contactName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.contactName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Categoria
                        if (item.categoryName.isNotBlank() && item.categoryName != "Geral") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
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

            // Hero Card: Resumo dos Valores com PlatformProgressBar
            PlatformCard(
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isInstallment) "Valor Total Financiado" else "Mensalidade Regular",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isInstallment)
                            CurrencyUtils.formatCentsToCurrency(bill.totalAmountCents)
                        else
                            "${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)} / mês",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isInstallment) MaterialTheme.colorScheme.onSurface else SuccessGreen
                    )

                    if (isInstallment) {
                        PlatformProgressBar(
                            progress = item.progress,
                            height = 7.dp,
                            progressColor = if (isAllPaid) SuccessGreen else MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Pago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatCentsToCurrency(item.totalPaidCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Restante",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatCentsToCurrency(item.remainingCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAllPaid) SuccessGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (item.estimatedPayoffDate != null && !isAllPaid) {
                            Text(
                                text = "Previsão de quitação: ${DateUtils.formatMonthYear(item.estimatedPayoffDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Seção explicativa para assinaturas / recorrências
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Projeção Anual: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents * 12)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            item.nextInstallment?.let { next ->
                                Text(
                                    text = "Próx: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)} em ${DateUtils.formatDate(next.dueDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (item.hasVariableFirstInstallment) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1ª parcela diferenciada (Entrada/Adesão): ${CurrencyUtils.formatCentsToCurrency(item.firstInstallmentAmountCents)}. Demais cobranças: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)}/mês.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Cronograma de Parcelas / Cobranças
            if (item.installments.isNotEmpty()) {
                Text(
                    text = if (isInstallment)
                        "Cronograma de Parcelas (${item.installments.size})"
                    else
                        "Histórico e Próximas Cobranças (${item.installments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.installments.forEach { inst ->
                        InstallmentRow(
                            installment = inst,
                            isVariableFirst = item.hasVariableFirstInstallment,
                            onTogglePayment = { onTogglePayment(inst.id, inst.isPaid) },
                            onOpenAdjust = { onOpenAdjust(inst) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir Lançamento") },
            text = { Text("Deseja realmente excluir '${bill.title}' e todas as suas parcelas?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InstallmentRow(
    installment: BillInstallment,
    isVariableFirst: Boolean = false,
    onTogglePayment: () -> Unit,
    onOpenAdjust: (() -> Unit)? = null
) {
    val isToday = !installment.isPaid && DateUtils.isToday(installment.dueDate)
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis() && !isToday

    val rowTitle = when (installment.type) {
        BillType.RECURRING -> if (isVariableFirst && installment.installmentNumber == 1) {
            "1ª Cobrança (Adesão/Entrada)"
        } else {
            "Cobrança ${installment.installmentNumber}"
        }
        else -> "Parcela ${installment.installmentNumber}"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onTogglePayment,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (installment.isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (installment.isPaid) "Marcar como pendente" else "Marcar como paga",
                        tint = if (installment.isPaid) SuccessGreen else if (isOverdue) MaterialTheme.colorScheme.error else if (isToday) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rowTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isVariableFirst && installment.installmentNumber == 1 && installment.type == BillType.RECURRING) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = WarningAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Entrada",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = when {
                            installment.isPaid -> "Pago"
                            isOverdue -> "Venceu ${DateUtils.formatDate(installment.dueDate)}"
                            isToday -> "Vence hoje"
                            else -> "Vence ${DateUtils.formatDate(installment.dueDate)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isOverdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            installment.isPaid -> SuccessGreen
                            isOverdue -> MaterialTheme.colorScheme.error
                            isToday -> WarningAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (installment.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )

                if (onOpenAdjust != null) {
                    IconButton(
                        onClick = onOpenAdjust,
                        modifier = Modifier.size(32.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ajustar parcela",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineMonthCard(
    month: TimelineMonthSummary,
    allItems: List<BillWithInstallments>,
    onTogglePayment: (String, Boolean) -> Unit,
    onSelectPlan: (BillWithInstallments) -> Unit
) {
    val currentMonthLabel = remember { DateUtils.formatMonthYear(System.currentTimeMillis()) }
    val isCurrentMonth = remember(month.monthLabel, currentMonthLabel) {
        month.monthLabel.equals(currentMonthLabel, ignoreCase = true)
    }
    var isExpanded by rememberSaveable(month.monthLabel) { mutableStateOf(isCurrentMonth) }

    PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header do Mês
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isCurrentMonth) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = month.monthLabel,
                                style = MaterialTheme.typography.titleMedium,
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
                        }
                        Text(
                            text = "${month.installmentsCount} ${if (month.installmentsCount == 1) "parcela" else "parcelas"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(month.totalCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (month.pendingCents > 0L && month.paidCents > 0L) {
                            Text(
                                text = "Resta: ${CurrencyUtils.formatCentsToCurrency(month.pendingCents)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (month.pendingCents == 0L && month.totalCents > 0L) {
                            Text(
                                text = "Mês Quitado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Recolher mês" else "Expandir mês",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Parcelas individuais do mês
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                month.items.forEach { inst ->
                    val parentPlan = allItems.find { it.bill.id == inst.billId }
                    val title = parentPlan?.bill?.title ?: "Parcela"
                    val contact = inst.contactName ?: parentPlan?.contactName
                    val isToday = !inst.isPaid && DateUtils.isToday(inst.dueDate)
                    val isOverdue = !inst.isPaid && inst.dueDate < System.currentTimeMillis() && !isToday

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (parentPlan != null) onSelectPlan(parentPlan)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = { onTogglePayment(inst.id, inst.isPaid) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (inst.isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = if (inst.isPaid) "Paga" else "Marcar como paga",
                                        tint = if (inst.isPaid) SuccessGreen else if (isOverdue) MaterialTheme.colorScheme.error else if (isToday) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (!contact.isNullOrBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = contact,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        val installmentLabel = when {
                                            inst.type == BillType.RECURRING -> {
                                                if (parentPlan?.hasVariableFirstInstallment == true && inst.installmentNumber == 1) {
                                                    "1ª Recorrência (Adesão)"
                                                } else {
                                                    "Recorrência"
                                                }
                                            }
                                            else -> "Parcela ${inst.installmentNumber}/${inst.totalInstallments}"
                                        }

                                        Text(
                                            text = when {
                                                inst.isPaid -> "$installmentLabel • Paga"
                                                isOverdue -> "$installmentLabel • Venceu ${DateUtils.formatDate(inst.dueDate)}"
                                                isToday -> "$installmentLabel • Vence hoje"
                                                else -> "$installmentLabel • ${DateUtils.formatDate(inst.dueDate)}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isOverdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                            color = when {
                                                inst.isPaid -> SuccessGreen
                                                isOverdue -> MaterialTheme.colorScheme.error
                                                isToday -> WarningAmber
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyUtils.formatCentsToCurrency(inst.amountCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inst.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                )
                                if (parentPlan?.hasVariableFirstInstallment == true && inst.installmentNumber == 1 && inst.type == BillType.RECURRING) {
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = WarningAmber.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Adesão/Entrada",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WarningAmber,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
    }
}


@Composable
fun EmptyRecurringView(mode: Int = 0) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (mode) {
                        0 -> Icons.Default.CalendarMonth
                        1 -> Icons.Default.CreditCard
                        else -> Icons.Default.Autorenew
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when (mode) {
                    0 -> "Nenhum Pagamento Futuro"
                    1 -> "Nenhuma Compra Parcelada"
                    else -> "Nenhuma Assinatura Cadastrada"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (mode) {
                    0 -> "Todas as suas parcelas já foram quitadas ou ainda não há compromissos futuros programados."
                    1 -> "Cadastre compras parceladas para acompanhar o cronograma e amortização de débitos."
                    else -> "Cadastre despesas contínuas como streaming, condomínio e serviços recorrentes."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
