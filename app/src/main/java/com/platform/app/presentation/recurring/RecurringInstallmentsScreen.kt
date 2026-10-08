package com.platform.app.presentation.recurring

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.components.ConfirmPaymentDialog
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformSearchTopBar
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.recurring.components.BillPlanCard
import com.platform.app.presentation.recurring.components.EmptyRecurringView
import com.platform.app.presentation.recurring.components.RecurringBillCard
import com.platform.app.presentation.recurring.components.RecurringDetailBottomSheet
import com.platform.app.presentation.recurring.components.TimelineMonthCard
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
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
    var installmentToConfirmPayment by remember { mutableStateOf<BillInstallment?>(null) }

    val handleTogglePayment: (String, Boolean) -> Unit = { instId, paid ->
        if (paid) {
            viewModel.onAction(RecurringInstallmentsUiAction.TogglePayment(instId, paid))
        } else {
            val targetInst = uiState.items.flatMap { it.installments }.find { it.id == instId }
            if (targetInst != null) {
                installmentToConfirmPayment = targetInst
            } else {
                viewModel.onAction(RecurringInstallmentsUiAction.TogglePayment(instId, paid))
            }
        }
    }

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
                                    onTogglePayment = handleTogglePayment,
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
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val recurringFilters = listOf(RecurringStatusFilter.ALL, RecurringStatusFilter.ACTIVE, RecurringStatusFilter.PAUSED)
                        items(recurringFilters) { filter ->
                            FilterChip(
                                selected = uiState.statusFilter == filter,
                                onClick = { viewModel.onAction(RecurringInstallmentsUiAction.StatusFilterChanged(filter)) },
                                label = { Text(filter.label) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                onTogglePayment = handleTogglePayment,
                onOpenAdjust = { inst ->
                    viewModel.onAction(RecurringInstallmentsUiAction.OpenAdjustInstallment(inst))
                },
                onTogglePause = { billId, isPaused ->
                    viewModel.onAction(RecurringInstallmentsUiAction.TogglePauseBill(billId, isPaused))
                },
                onStopRecurring = { billId ->
                    planToViewDetails = null
                    viewModel.onAction(RecurringInstallmentsUiAction.StopRecurringBill(billId))
                },
                onUpdateMonthlyAmount = { billId, newAmountCents ->
                    viewModel.onAction(RecurringInstallmentsUiAction.UpdateBillMonthlyAmount(billId, newAmountCents))
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
                onSave = { instId, amount, due, applyToFuture ->
                    viewModel.onAction(
                        RecurringInstallmentsUiAction.SaveAdjustInstallment(
                            installmentId = instId,
                            newAmountCents = amount,
                            newDueDate = due,
                            applyToFuturePending = applyToFuture
                        )
                    )
                },
                onDeleteSingle = { instId ->
                    viewModel.onAction(RecurringInstallmentsUiAction.DeleteSingleInstallment(instId))
                },
                onDeleteFuture = { billId, fromDueDate ->
                    viewModel.onAction(RecurringInstallmentsUiAction.DeleteFutureInstallments(billId, fromDueDate))
                }
            )
        }

        installmentToConfirmPayment?.let { inst ->
            ConfirmPaymentDialog(
                installmentTitle = "${inst.billTitle} (${inst.installmentNumber}/${inst.totalInstallments})",
                amountCents = inst.amountCents,
                dueDate = inst.dueDate,
                onConfirm = { actualPaymentDate ->
                    viewModel.onAction(RecurringInstallmentsUiAction.TogglePayment(inst.id, inst.isPaid, actualPaymentDate))
                    installmentToConfirmPayment = null
                },
                onDismiss = {
                    installmentToConfirmPayment = null
                }
            )
        }
    }
}
