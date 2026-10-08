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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.platform.app.presentation.components.ConfirmPaymentDialog
import com.platform.app.presentation.home.components.GlobalOverdueAlertBanner
import com.platform.app.presentation.home.components.HomeTopBar
import com.platform.app.presentation.home.components.HomeViewModeSelector
import com.platform.app.presentation.home.components.renderCalendarView
import com.platform.app.presentation.home.components.renderMonthlyView
import com.platform.app.presentation.home.components.renderPanoramaView
import com.platform.app.presentation.theme.Dimens
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToNewExpense: () -> Unit,
    modifier: Modifier = Modifier,
    uiEffect: SharedFlow<HomeUiEffect>? = null
) {
    var isPrivate by rememberSaveable { mutableStateOf(false) }
    var showMonthPickerSheet by remember { mutableStateOf(false) }
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova Despesa"
                )
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
            // Seletor de Modo: [ Panorama Geral | Calendário | Visão Mensal ]
            HomeViewModeSelector(
                selectedMode = uiState.viewMode,
                onModeSelected = { mode -> onAction(HomeUiAction.ChangeViewMode(mode)) }
            )

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
                    // Item 5: Banner Global de Contas Atrasadas (visível em todas as telas se houver atrasos)
                    if (uiState.globalOverdueItems.isNotEmpty()) {
                        item {
                            GlobalOverdueAlertBanner(
                                overdueItems = uiState.globalOverdueItems,
                                totalOverdueCents = uiState.globalOverdueTotalCents,
                                isExpanded = uiState.isOverdueBannerExpanded,
                                isPrivate = isPrivate,
                                onToggleExpanded = {
                                    onAction(HomeUiAction.ToggleOverdueBanner(!uiState.isOverdueBannerExpanded))
                                },
                                onPayItem = handlePayItem
                            )
                        }
                    }

                    // Renderização de acordo com o modo selecionado
                    when (uiState.viewMode) {
                        HomeViewMode.PANORAMA -> {
                            renderPanoramaView(
                                uiState = uiState,
                                isPrivate = isPrivate,
                                onPay = handlePayItem,
                                onAddExpense = onNavigateToNewExpense
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
                        HomeViewMode.MONTHLY -> {
                            renderMonthlyView(
                                uiState = uiState,
                                isPrivate = isPrivate,
                                onPayItem = handlePayItem,
                                onAddExpense = onNavigateToNewExpense,
                                onAction = onAction
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
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
