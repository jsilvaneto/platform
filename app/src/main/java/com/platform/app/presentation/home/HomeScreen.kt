package com.platform.app.presentation.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.platform.app.presentation.components.ConfirmPaymentDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.domain.usecase.MonthlyForecastResult
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformPrivacyToggle
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber
import kotlinx.coroutines.flow.SharedFlow
import java.util.Calendar

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

// -------------------------------------------------------------
// Seletor de Modo de Exibição
// -------------------------------------------------------------
@Composable
fun HomeViewModeSelector(
    selectedMode: HomeViewMode,
    onModeSelected: (HomeViewMode) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingNormal, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModeChip(
                label = "Panorama Geral",
                icon = Icons.Default.Timeline,
                isSelected = selectedMode == HomeViewMode.PANORAMA,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(HomeViewMode.PANORAMA) }
            )
            ModeChip(
                label = "Cronograma",
                icon = Icons.Default.Schedule,
                isSelected = selectedMode == HomeViewMode.CALENDAR,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(HomeViewMode.CALENDAR) }
            )
            ModeChip(
                label = "Visão Mensal",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                isSelected = selectedMode == HomeViewMode.MONTHLY,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(HomeViewMode.MONTHLY) }
            )
        }
    }
}

@Composable
fun ModeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.height(34.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// Item 5: Banner Global de Contas Atrasadas
// -------------------------------------------------------------
@Composable
fun GlobalOverdueAlertBanner(
    overdueItems: List<PayableItem>,
    totalOverdueCents: Long,
    isExpanded: Boolean,
    isPrivate: Boolean,
    onToggleExpanded: () -> Unit,
    onPayItem: (PayableItem) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        color = UrgentRed.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, UrgentRed.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.spacingNormal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(UrgentRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = UrgentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "${overdueItems.size} Contas Atrasadas Acumuladas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = UrgentRed
                        )
                        Text(
                            text = "Total vencido: ${formatValueOrPrivate(totalOverdueCents, isPrivate)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                TextButton(
                    onClick = onToggleExpanded,
                    shape = RoundedCornerShape(Dimens.buttonCornerRadius)
                ) {
                    Text(
                        text = if (isExpanded) "Ocultar" else "Ver Todas",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = UrgentRed
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = UrgentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    overdueItems.forEach { item ->
                        val daysLate = ((System.currentTimeMillis() - item.dueDate) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(1)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, UrgentRed.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = UrgentRed.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Atrasada há $daysLate dia${if (daysLate > 1) "s" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = UrgentRed,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Venceu em ${DateUtils.formatDate(item.dueDate)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatValueOrPrivate(item.amountCents, isPrivate),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = UrgentRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onPayItem(item) },
                                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SuccessGreen.copy(alpha = 0.15f),
                                            contentColor = SuccessGreen
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Pagar",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
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

// -------------------------------------------------------------
// Item 3 & 7: Panorama Geral das Finanças
// -------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.renderPanoramaView(
    uiState: HomeUiState,
    isPrivate: Boolean,
    onPay: (PayableItem) -> Unit,
    onAddExpense: () -> Unit
) {
    val forecast = uiState.forecastResult
    val dashboard = uiState.dashboardMetrics

    // 1. Radar de Pagamentos / Liquidez Imediata
    item {
        PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
            Column(modifier = Modifier.padding(Dimens.spacingNormal)) {
                Text(
                    text = "Radar de Pagamentos & Liquidez",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vence Hoje",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber
                        )
                        Text(
                            text = formatValueOrPrivate(forecast?.dueTodayTotalCents ?: 0L, isPrivate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${forecast?.dueTodayCount ?: 0} contas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Próximos 7 Dias",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatValueOrPrivate(forecast?.next7DaysTotalCents ?: 0L, isPrivate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${forecast?.next7DaysCount ?: 0} contas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Próximos 30 Dias",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatValueOrPrivate(uiState.next30DaysTotalCents, isPrivate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.next30DaysItems.size} lançamentos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 2. Pilares Estruturais do Orçamento (Item 7: Recorrentes Perpétuas e Parcelamentos)
    item {
        Text(
            text = "Pilares Estruturais das Finanças",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    // Pilar 1: Custo Fixo Recorrente Contínuo ("Para Sempre")
    item {
        PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Despesas Fixas e Assinaturas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${dashboard?.activeRecurringCount ?: 0} contas e serviços cadastrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${formatValueOrPrivate(dashboard?.fixedMonthlyTotalCents ?: 0L, isPrivate)}/mês",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }
    }

    // Pilar 2: Passivo Total em Parcelamentos
    item {
        PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
            Column(modifier = Modifier.padding(Dimens.spacingNormal)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Passivo em Parcelamentos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${dashboard?.activeInstallmentsCount ?: 0} compras ativas em amortização",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatValueOrPrivate(dashboard?.totalInstallmentsRemainingCents ?: 0L, isPrivate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Saldo restante",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Liberação de Caixa Futura
                val nextCompleting = dashboard?.nextCompletingInstallments?.firstOrNull()
                if (nextCompleting != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Próxima quitação: '${nextCompleting.title}' em ${nextCompleting.completionMonthLabel} (+${formatValueOrPrivate(nextCompleting.freedMonthlyAmountCents, isPrivate)}/mês de alívio)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. Próximos Pagamentos
    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximos Pagamentos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${uiState.next30DaysItems.size} próximos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (uiState.next30DaysItems.isEmpty()) {
        item {
            EmptyForecastCard(onAddExpense = onAddExpense)
        }
    } else {
        items(uiState.next30DaysItems.take(8), key = { it.id }) { item ->
            PayableItemCard(
                item = item,
                isPrivate = isPrivate,
                onPay = { onPay(item) }
            )
        }
    }
}

// -------------------------------------------------------------
// Item 6: Visão de Calendário / Agenda de Vencimentos
// -------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.renderCalendarView(
    uiState: HomeUiState,
    isPrivate: Boolean,
    onSelectDay: (Long?) -> Unit,
    onPay: (PayableItem) -> Unit
) {
    val activeDays = uiState.calendarDays.filter { it.itemsCount > 0 }
    val peakDay = activeDays.maxByOrNull { it.totalAmountCents }

    // 1. Detector de Picos Financeiros (Stress de Caixa)
    if (peakDay != null && peakDay.totalAmountCents > 0L) {
        item {
            PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingNormal),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(WarningAmber.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pico de Pagamentos: Dia ${peakDay.dayOfMonth}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WarningAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Maior Volume",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${peakDay.itemsCount} conta(s) somando ${formatValueOrPrivate(peakDay.totalAmountCents, isPrivate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 2. Distribuição por Semanas do Mês (Ciclos Semanais de Caixa)
    item {
        val week1Cents = activeDays.filter { it.dayOfMonth in 1..7 }.sumOf { it.totalAmountCents }
        val week2Cents = activeDays.filter { it.dayOfMonth in 8..14 }.sumOf { it.totalAmountCents }
        val week3Cents = activeDays.filter { it.dayOfMonth in 15..21 }.sumOf { it.totalAmountCents }
        val week4Cents = activeDays.filter { it.dayOfMonth >= 22 }.sumOf { it.totalAmountCents }

        PlatformCard(shape = RoundedCornerShape(Dimens.cardCornerRadius)) {
            Column(modifier = Modifier.padding(Dimens.spacingNormal)) {
                Text(
                    text = "Demanda de Caixa por Semana",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeekDemandColumn(title = "Sem 1 (1-7)", amountCents = week1Cents, isPrivate = isPrivate, modifier = Modifier.weight(1f))
                    WeekDemandColumn(title = "Sem 2 (8-14)", amountCents = week2Cents, isPrivate = isPrivate, modifier = Modifier.weight(1f))
                    WeekDemandColumn(title = "Sem 3 (15-21)", amountCents = week3Cents, isPrivate = isPrivate, modifier = Modifier.weight(1f))
                    WeekDemandColumn(title = "Sem 4 (22+)", amountCents = week4Cents, isPrivate = isPrivate, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // 3. Carrossel Inteligente: Dias com Vencimento Agendado
    if (activeDays.isNotEmpty()) {
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dias com Vencimento (${activeDays.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.selectedCalendarDayMillis != null) {
                        TextButton(
                            onClick = { onSelectDay(null) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpar",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Ver Todas",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeDays, key = { it.dateMillis }) { dayItem ->
                        val isSelected = uiState.selectedCalendarDayMillis == dayItem.dateMillis
                        val isPeak = peakDay?.dateMillis == dayItem.dateMillis
                        ActiveDayCard(
                            dayItem = dayItem,
                            isSelected = isSelected,
                            isPeak = isPeak,
                            isPrivate = isPrivate,
                            onClick = {
                                if (isSelected) {
                                    onSelectDay(null)
                                } else {
                                    onSelectDay(dayItem.dateMillis)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // 2. Seção de Contas do Dia Selecionado ou Agenda do Mês
    if (uiState.selectedCalendarDayMillis != null) {
        val selectedDayDate = DateUtils.formatDate(uiState.selectedCalendarDayMillis)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vencimentos em $selectedDayDate (${uiState.daySelectedItems.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatValueOrPrivate(uiState.daySelectedItems.sumOf { it.amountCents }, isPrivate),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (uiState.daySelectedItems.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma conta agendada para este dia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(uiState.daySelectedItems, key = { it.id }) { item ->
                PayableItemCard(
                    item = item,
                    isPrivate = isPrivate,
                    onPay = if (!item.isPaid) { { onPay(item) } } else null
                )
            }
        }
    } else {
        // Exibe a Agenda Cronológica Completa
        val forecast = uiState.forecastResult
        if (forecast != null) {
            if (forecast.dueTodayItems.isNotEmpty()) {
                item {
                    UrgencySectionHeader(
                        title = "Vence Hoje",
                        count = forecast.dueTodayCount,
                        badgeColor = WarningAmber,
                        totalCents = forecast.dueTodayTotalCents,
                        isPrivate = isPrivate
                    )
                }
                items(forecast.dueTodayItems, key = { it.id }) { item ->
                    PayableItemCard(
                        item = item,
                        isPrivate = isPrivate,
                        onPay = { onPay(item) }
                    )
                }
            }

            if (forecast.next7DaysItems.isNotEmpty()) {
                item {
                    UrgencySectionHeader(
                        title = "Próximos 7 Dias",
                        count = forecast.next7DaysCount,
                        badgeColor = MaterialTheme.colorScheme.primary,
                        totalCents = forecast.next7DaysTotalCents,
                        isPrivate = isPrivate
                    )
                }
                items(forecast.next7DaysItems, key = { it.id }) { item ->
                    PayableItemCard(
                        item = item,
                        isPrivate = isPrivate,
                        onPay = { onPay(item) }
                    )
                }
            }

            if (forecast.laterInMonthItems.isNotEmpty()) {
                item {
                    UrgencySectionHeader(
                        title = "Mais Adiante no Mês",
                        count = forecast.laterInMonthItems.size,
                        badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        totalCents = forecast.laterInMonthItems.sumOf { it.amountCents },
                        isPrivate = isPrivate
                    )
                }
                items(forecast.laterInMonthItems, key = { it.id }) { item ->
                    PayableItemCard(
                        item = item,
                        isPrivate = isPrivate,
                        onPay = { onPay(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun WeekDemandColumn(
    title: String,
    amountCents: Long,
    isPrivate: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatValueOrPrivate(amountCents, isPrivate),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (amountCents > 0L) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ActiveDayCard(
    dayItem: CalendarDayItem,
    isSelected: Boolean,
    isPeak: Boolean,
    isPrivate: Boolean,
    onClick: () -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
    val isToday = dayItem.dayOfMonth == cal.get(Calendar.DAY_OF_MONTH) &&
            DateUtils.getMonth(dayItem.dateMillis) == cal.get(Calendar.MONTH) &&
            DateUtils.getYear(dayItem.dateMillis) == cal.get(Calendar.YEAR)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            isPeak -> WarningAmber.copy(alpha = 0.10f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isSelected -> MaterialTheme.colorScheme.primary
                isPeak -> WarningAmber.copy(alpha = 0.5f)
                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier
            .width(72.dp)
            .height(78.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Text(
                text = dayItem.dayOfWeekLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${dayItem.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formatValueOrPrivate(dayItem.totalAmountCents, isPrivate),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isPeak) WarningAmber else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CalendarDayCard(
    dayItem: CalendarDayItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
    val isToday = dayItem.dayOfMonth == cal.get(Calendar.DAY_OF_MONTH) &&
            DateUtils.getMonth(dayItem.dateMillis) == cal.get(Calendar.MONTH) &&
            DateUtils.getYear(dayItem.dateMillis) == cal.get(Calendar.YEAR)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier
            .width(52.dp)
            .height(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = dayItem.dayOfWeekLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${dayItem.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    dayItem.hasOverdue -> {
                        Box(modifier = Modifier.size(6.dp).background(UrgentRed, CircleShape))
                    }
                    dayItem.hasDueToday -> {
                        Box(modifier = Modifier.size(6.dp).background(WarningAmber, CircleShape))
                    }
                    dayItem.hasPending -> {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    }
                    dayItem.isFullyPaid -> {
                        Box(modifier = Modifier.size(6.dp).background(SuccessGreen, CircleShape))
                    }
                    else -> {
                        Spacer(modifier = Modifier.size(6.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Visão Mensal (Original Refinada)
// -------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.renderMonthlyView(
    uiState: HomeUiState,
    isPrivate: Boolean,
    onPayItem: (PayableItem) -> Unit,
    onAddExpense: () -> Unit,
    onAction: (HomeUiAction) -> Unit
) {
    val forecast = uiState.forecastResult ?: return
    val isMonthFullyPaid = forecast.totalItemsCount > 0 &&
            forecast.overdueCount == 0 &&
            forecast.dueTodayCount == 0 &&
            forecast.next7DaysCount == 0 &&
            forecast.laterInMonthItems.isEmpty() &&
            forecast.paidCount > 0

    // 1. Executive Hero Card: Previsibilidade & Quitação do Mês
    item {
        ForecastImpactCard(
            totalForecastCents = forecast.totalForecastCents,
            totalPaidCents = forecast.totalPaidCents,
            isPrivate = isPrivate
        )
    }

    // 2. Resumo Semafórico em Chips
    item {
        SemaphoricSummaryRow(
            forecast = forecast,
            isPrivate = isPrivate
        )
    }

    // 3. Card Triunfante quando todas as contas do mês estiverem pagas
    if (isMonthFullyPaid) {
        item {
            MonthVictoryCard(
                paidCount = forecast.paidCount,
                paidTotalCents = forecast.paidTotalCents,
                isPrivate = isPrivate
            )
        }
    }

    // 4. Seções de Urgência Semafórica do Mês
    if (forecast.overdueItems.isNotEmpty()) {
        item {
            UrgencySectionHeader(
                title = "Atrasadas no Mês",
                count = forecast.overdueCount,
                badgeColor = UrgentRed,
                totalCents = forecast.overdueTotalCents,
                isPrivate = isPrivate
            )
        }
        items(forecast.overdueItems, key = { it.id }) { item ->
            PayableItemCard(
                item = item,
                isPrivate = isPrivate,
                onPay = { onPayItem(item) }
            )
        }
    }

    if (forecast.dueTodayItems.isNotEmpty()) {
        item {
            UrgencySectionHeader(
                title = "Vence Hoje",
                count = forecast.dueTodayCount,
                badgeColor = WarningAmber,
                totalCents = forecast.dueTodayTotalCents,
                isPrivate = isPrivate
            )
        }
        items(forecast.dueTodayItems, key = { it.id }) { item ->
            PayableItemCard(
                item = item,
                isPrivate = isPrivate,
                onPay = { onPayItem(item) }
            )
        }
    }

    if (forecast.next7DaysItems.isNotEmpty()) {
        item {
            UrgencySectionHeader(
                title = "Próximos 7 Dias",
                count = forecast.next7DaysCount,
                badgeColor = MaterialTheme.colorScheme.primary,
                totalCents = forecast.next7DaysTotalCents,
                isPrivate = isPrivate
            )
        }
        items(forecast.next7DaysItems, key = { it.id }) { item ->
            PayableItemCard(
                item = item,
                isPrivate = isPrivate,
                onPay = { onPayItem(item) }
            )
        }
    }

    if (forecast.laterInMonthItems.isNotEmpty()) {
        item {
            UrgencySectionHeader(
                title = "Mais Adiante no Mês",
                count = forecast.laterInMonthItems.size,
                badgeColor = MaterialTheme.colorScheme.onSurfaceVariant,
                totalCents = forecast.laterInMonthItems.sumOf { it.amountCents },
                isPrivate = isPrivate
            )
        }
        items(forecast.laterInMonthItems, key = { it.id }) { item ->
            PayableItemCard(
                item = item,
                isPrivate = isPrivate,
                onPay = { onPayItem(item) }
            )
        }
    }

    // 5. Pagas no Mês (Seção Colapsável)
    if (forecast.paidItems.isNotEmpty()) {
        item {
            CollapsiblePaidHeader(
                isExpanded = uiState.isPaidSectionExpanded,
                count = forecast.paidCount,
                totalCents = forecast.paidTotalCents,
                isPrivate = isPrivate,
                onToggle = { onAction(HomeUiAction.TogglePaidSection(!uiState.isPaidSectionExpanded)) }
            )
        }
        if (uiState.isPaidSectionExpanded) {
            items(forecast.paidItems, key = { it.id }) { item ->
                PayableItemCard(
                    item = item,
                    isMuted = true,
                    isPrivate = isPrivate,
                    onPay = null
                )
            }
        }
    }

    if (forecast.totalItemsCount == 0) {
        item {
            EmptyForecastCard(onAddExpense = onAddExpense)
        }
    }
}

// -------------------------------------------------------------
// TopBar da Home
// -------------------------------------------------------------
@Composable
fun HomeTopBar(
    viewMode: HomeViewMode,
    monthLabel: String,
    isPrivate: Boolean,
    onTogglePrivacy: () -> Unit,
    onOpenMonthPicker: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Lateral",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (viewMode == HomeViewMode.PANORAMA) {
                    Text(
                        text = "Visão Panorâmica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mês Anterior",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Cápsula Clicável do Mês
                    Surface(
                        onClick = onOpenMonthPicker,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Selecionar mês",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onNextMonth) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Próximo Mês",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PlatformPrivacyToggle(
                    isPrivate = isPrivate,
                    onToggle = onTogglePrivacy
                )

                if (viewMode != HomeViewMode.PANORAMA) {
                    TextButton(
                        onClick = onCurrentMonth,
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius)
                    ) {
                        Text(
                            text = "Hoje",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastImpactCard(
    totalForecastCents: Long,
    totalPaidCents: Long,
    isPrivate: Boolean
) {
    val totalMonthCents = totalForecastCents + totalPaidCents
    val progress = if (totalMonthCents > 0L) {
        totalPaidCents.toFloat() / totalMonthCents.toFloat()
    } else {
        0f
    }
    val percentage = (progress * 100).toInt()
    val isFullyPaid = totalForecastCents == 0L && totalMonthCents > 0L

    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal)
        ) {
            if (isFullyPaid) {
                // Hierarquia reforçada: quando tudo estiver quitado, o badge de sucesso
                // aparece em destaque no topo, ANTES do valor R$ 0,00.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tudo quitado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Text(
                        text = "100% quitado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Restante a Pagar no Mês",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formatValueOrPrivate(totalForecastCents, isPrivate),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Restante a Pagar no Mês",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = formatValueOrPrivate(totalForecastCents, isPrivate),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$percentage% quitado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PlatformProgressBar(
                progress = progress,
                height = 7.dp,
                progressColor = if (isFullyPaid) SuccessGreen else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(SuccessGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Já Pago:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatValueOrPrivate(totalPaidCents, isPrivate),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Geral:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatValueOrPrivate(totalMonthCents, isPrivate),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MonthVictoryCard(
    paidCount: Int,
    paidTotalCents: Long,
    isPrivate: Boolean
) {
    Surface(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        color = SuccessGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tudo em dia para este mês!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$paidCount contas liquidadas · Total de ${formatValueOrPrivate(paidTotalCents, isPrivate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SemaphoricSummaryRow(
    forecast: MonthlyForecastResult,
    isPrivate: Boolean
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        item {
            SemaphoricChip(
                label = "Atrasadas",
                count = forecast.overdueCount,
                amountCents = forecast.overdueTotalCents,
                accentColor = UrgentRed,
                isPrivate = isPrivate
            )
        }
        item {
            SemaphoricChip(
                label = "Vence Hoje",
                count = forecast.dueTodayCount,
                amountCents = forecast.dueTodayTotalCents,
                accentColor = WarningAmber,
                isPrivate = isPrivate
            )
        }
        item {
            SemaphoricChip(
                label = "Próximos 7 Dias",
                count = forecast.next7DaysCount,
                amountCents = forecast.next7DaysTotalCents,
                accentColor = MaterialTheme.colorScheme.primary,
                isPrivate = isPrivate
            )
        }
        item {
            SemaphoricChip(
                label = "Pagas no Mês",
                count = forecast.paidCount,
                amountCents = forecast.paidTotalCents,
                accentColor = SuccessGreen,
                isPrivate = isPrivate
            )
        }
    }
}

@Composable
fun SemaphoricChip(
    label: String,
    count: Int,
    amountCents: Long,
    accentColor: Color,
    isPrivate: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.width(135.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatValueOrPrivate(amountCents, isPrivate),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UrgencySectionHeader(
    title: String,
    count: Int,
    badgeColor: Color,
    totalCents: Long,
    isPrivate: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spacingSmall, bottom = Dimens.spacingExtraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$title ($count)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = formatValueOrPrivate(totalCents, isPrivate),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CollapsiblePaidHeader(
    isExpanded: Boolean,
    count: Int,
    totalCents: Long,
    isPrivate: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(SuccessGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pagas no Mês ($count)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatValueOrPrivate(totalCents, isPrivate),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = SuccessGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PayableItemCard(
    item: PayableItem,
    isMuted: Boolean = false,
    isPrivate: Boolean = false,
    onPay: (() -> Unit)? = null
) {
    val alphaModifier = if (isMuted) Modifier.alpha(0.75f) else Modifier
    val functionalIcon = PlatformIconCatalog.getIcon(item.categoryIconName)
    val categoryColor = try {
        Color(item.categoryColorHex.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    PlatformCard(
        modifier = alphaModifier,
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = functionalIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isPaid) {
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
                                        text = "Pago",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        } else {
                            val isToday = item.urgency == PayableUrgency.DUE_TODAY || DateUtils.isToday(item.dueDate)
                            val isOverdue = item.urgency == PayableUrgency.OVERDUE || (item.dueDate < System.currentTimeMillis() && !isToday)
                            Text(
                                text = when {
                                    isOverdue -> "Venceu ${DateUtils.formatDate(item.dueDate)}"
                                    isToday -> "Vence hoje"
                                    else -> "Vence ${DateUtils.formatDate(item.dueDate)}"
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

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = item.nature.let {
                                try { Color(it.colorHex.toColorInt()).copy(alpha = 0.15f) }
                                catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }
                            }
                        ) {
                            Text(
                                text = item.nature.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = try { Color(item.nature.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatValueOrPrivate(item.amountCents, isPrivate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (onPay != null && !item.isPaid) {
                    Button(
                        onClick = onPay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pagar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (item.isPaid) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Pago",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyForecastCard(onAddExpense: () -> Unit) {
    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nenhum Compromisso Imediato",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cadastre suas contas a pagar para planejar seus pagamentos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddExpense,
                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nova Despesa",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getFunctionalIcon(categoryName: String, iconName: String? = null): ImageVector {
    if (!iconName.isNullOrBlank() && iconName != "category") {
        return PlatformIconCatalog.getIcon(iconName)
    }
    val lower = categoryName.lowercase()
    return when {
        lower.contains("restaurante") || lower.contains("alimenta") || lower.contains("comida") || lower.contains("mercado") -> Icons.Default.Restaurant
        lower.contains("moradia") || lower.contains("aluguel") || lower.contains("casa") || lower.contains("condom") -> Icons.Default.Home
        lower.contains("transporte") || lower.contains("carro") || lower.contains("combust") || lower.contains("uber") -> Icons.Default.DirectionsCar
        lower.contains("cart") || lower.contains("fatura") || lower.contains("crédito") -> Icons.Default.CreditCard
        lower.contains("saúde") || lower.contains("médic") || lower.contains("remédio") || lower.contains("farm") -> Icons.Default.MedicalServices
        lower.contains("educa") || lower.contains("curso") || lower.contains("faculdade") -> Icons.Default.School
        lower.contains("compra") || lower.contains("shopping") -> Icons.Default.ShoppingCart
        else -> PlatformIconCatalog.getIcon(iconName ?: "")
    }
}
