package com.platform.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PayableItem
import com.platform.app.presentation.bills.components.QuickExpenseBottomSheet
import com.platform.app.presentation.components.ConfirmPaymentDialog
import com.platform.app.presentation.components.PlatformFab
import com.platform.app.presentation.home.components.HomeTopBar
import com.platform.app.presentation.home.components.renderCalendarView
import com.platform.app.presentation.home.components.renderTodayListView
import com.platform.app.presentation.theme.Dimens
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    onNavigateToNewExpense: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToBills: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    uiEffect: SharedFlow<HomeUiEffect>? = null
) {
    var isPrivate by rememberSaveable { mutableStateOf(false) }
    var showMonthPickerSheet by remember { mutableStateOf(false) }
    var showQuickExpenseSheet by remember { mutableStateOf(false) }
    val monthPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    var itemToConfirmPayment by remember { mutableStateOf<PayableItem.BillPayable?>(null) }

    val handlePayItem: (PayableItem) -> Unit = { item ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        when (item) {
            is PayableItem.BillPayable -> itemToConfirmPayment = item
            is PayableItem.InvoicePayable -> onAction(HomeUiAction.PayInvoice(item.id))
        }
    }

    LaunchedEffect(uiEffect) {
        uiEffect?.collect { effect ->
            when (effect) {
                is HomeUiEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onAction(effect.undoAction)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                viewMode = uiState.viewMode,
                monthLabel = uiState.forecastResult?.monthLabel ?: DateUtils.formatMonthYear(uiState.selectedMonthMillis),
                isPrivate = isPrivate,
                onTogglePrivacy = { isPrivate = !isPrivate },
                onOpenMonthPicker = { showMonthPickerSheet = true },
                onPreviousMonth = { onAction(HomeUiAction.PreviousMonth) },
                onNextMonth = { onAction(HomeUiAction.NextMonth) },
                onCurrentMonth = { onAction(HomeUiAction.CurrentMonth) },
                onToggleViewMode = { mode -> onAction(HomeUiAction.ChangeViewMode(mode)) },
                onOpenDrawer = onOpenDrawer
            )
        },
        floatingActionButton = {
            PlatformFab(
                onClick = { showQuickExpenseSheet = true },
                icon = Icons.Default.Add,
                contentDescription = "Nova Despesa"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading && uiState.forecastResult == null && uiState.dashboardMetrics == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = Dimens.spacingNormal,
                        vertical = Dimens.spacingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                ) {
                    // Renderização de acordo com o modo selecionado
                    when (uiState.viewMode) {
                        HomeViewMode.LIST -> {
                            renderTodayListView(
                                uiState = uiState,
                                isPrivate = isPrivate,
                                onPayItem = handlePayItem,
                                onAddExpense = onNavigateToNewExpense,
                                onNavigateToBills = onNavigateToBills,
                                onNavigateToCards = onNavigateToCards,
                                onNavigateToAnalytics = onNavigateToAnalytics,
                                onAction = onAction
                            )
                        }
                        HomeViewMode.CALENDAR -> {
                            renderCalendarView(
                                uiState = uiState,
                                isPrivate = isPrivate,
                                onSelectDay = { dayMillis -> onAction(HomeUiAction.SelectCalendarDay(dayMillis)) },
                                onPay = handlePayItem
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    if (showMonthPickerSheet) {
        MonthPickerBottomSheet(
            currentSelectedMillis = uiState.selectedMonthMillis,
            sheetState = monthPickerSheetState,
            onDismiss = { showMonthPickerSheet = false },
            onMonthSelected = { monthMillis ->
                onAction(HomeUiAction.SelectMonth(monthMillis))
            }
        )
    }

    if (showQuickExpenseSheet) {
        QuickExpenseBottomSheet(
            expenseItems = uiState.allExpenseItems,
            onDismiss = { showQuickExpenseSheet = false },
            onConfirm = { amountCents, expenseItemId, isPaid, dueDate ->
                onAction(HomeUiAction.SaveQuickExpense(amountCents, expenseItemId, isPaid, dueDate))
                showQuickExpenseSheet = false
            },
            onMoreDetails = {
                showQuickExpenseSheet = false
                onNavigateToNewExpense()
            }
        )
    }

    itemToConfirmPayment?.let { payable ->
        ConfirmPaymentDialog(
            installmentTitle = payable.title,
            amountCents = payable.amountCents,
            dueDate = payable.dueDate,
            onConfirm = { actualPaymentDate ->
                onAction(HomeUiAction.PayBill(payable.id, actualPaymentDate))
                itemToConfirmPayment = null
            },
            onDismiss = {
                itemToConfirmPayment = null
            }
        )
    }
}
