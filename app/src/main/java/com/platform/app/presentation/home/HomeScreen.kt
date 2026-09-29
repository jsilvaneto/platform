package com.platform.app.presentation.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.usecase.MonthlyForecastResult
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformPrivacyToggle
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToNewExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPrivate by rememberSaveable { mutableStateOf(false) }
    var showMonthPickerSheet by remember { mutableStateOf(false) }
    val monthPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            HomeTopBar(
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
            if (uiState.isLoading && uiState.forecastResult == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val forecast = uiState.forecastResult
                if (forecast != null) {
                    val isMonthFullyPaid = forecast.totalItemsCount > 0 &&
                            forecast.overdueCount == 0 &&
                            forecast.dueTodayCount == 0 &&
                            forecast.next7DaysCount == 0 &&
                            forecast.laterInMonthItems.isEmpty() &&
                            forecast.paidCount > 0

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = Dimens.spacingNormal,
                            vertical = Dimens.spacingSmall
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                    ) {
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

                        // 4. Seções de Urgência Semafórica
                        // 4.1 🔴 Atrasadas
                        if (forecast.overdueItems.isNotEmpty()) {
                            item {
                                UrgencySectionHeader(
                                    title = "Atrasadas",
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
                                    onPay = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (item) {
                                            is PayableItem.BillPayable -> onAction(HomeUiAction.PayBill(item.id))
                                            is PayableItem.InvoicePayable -> onAction(HomeUiAction.PayInvoice(item.id))
                                        }
                                    }
                                )
                            }
                        }

                        // 4.2 🟡 Vence Hoje
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
                                    onPay = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (item) {
                                            is PayableItem.BillPayable -> onAction(HomeUiAction.PayBill(item.id))
                                            is PayableItem.InvoicePayable -> onAction(HomeUiAction.PayInvoice(item.id))
                                        }
                                    }
                                )
                            }
                        }

                        // 4.3 ⚪ Próximos 7 Dias
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
                                    onPay = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (item) {
                                            is PayableItem.BillPayable -> onAction(HomeUiAction.PayBill(item.id))
                                            is PayableItem.InvoicePayable -> onAction(HomeUiAction.PayInvoice(item.id))
                                        }
                                    }
                                )
                            }
                        }

                        // 4.4 Mais Adiante no Mês (se houver)
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
                                    onPay = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (item) {
                                            is PayableItem.BillPayable -> onAction(HomeUiAction.PayBill(item.id))
                                            is PayableItem.InvoicePayable -> onAction(HomeUiAction.PayInvoice(item.id))
                                        }
                                    }
                                )
                            }
                        }

                        // 4.5 🟢 Pagas no Mês (Seção Colapsável)
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

                        // Estado vazio geral do mês
                        if (forecast.totalItemsCount == 0) {
                            item {
                                EmptyForecastCard(onAddExpense = onNavigateToNewExpense)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
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
}

@Composable
fun HomeTopBar(
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

                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mês Anterior",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cápsula Clicável do Mês (Abre BottomSheet em Grade)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                PlatformPrivacyToggle(
                    isPrivate = isPrivate,
                    onToggle = onTogglePrivacy
                )

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

    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Previsto no Mês",
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
                    color = if (percentage == 100 && totalMonthCents > 0) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$percentage% quitado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (percentage == 100 && totalMonthCents > 0) SuccessGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de Progresso de Quitação Elegante
            PlatformProgressBar(
                progress = progress,
                height = 7.dp,
                progressColor = if (percentage == 100 && totalMonthCents > 0) SuccessGreen else MaterialTheme.colorScheme.primary
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
    val functionalIcon = getFunctionalIcon(item.categoryName)
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
                // Ícone Funcional Material
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
                        Text(
                            text = "Vence ${DateUtils.formatDate(item.dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Badge da Natureza do Gasto
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
                    // Ação de Baixa com 1 Toque e feedback háptico
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
                text = "Nenhum Compromisso no Mês",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cadastre suas contas a pagar para planejar desembolsos.",
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

fun getFunctionalIcon(categoryName: String): ImageVector {
    val lower = categoryName.lowercase()
    return when {
        lower.contains("restaurante") || lower.contains("alimenta") || lower.contains("comida") || lower.contains("mercado") -> Icons.Default.Restaurant
        lower.contains("moradia") || lower.contains("aluguel") || lower.contains("casa") || lower.contains("condom") -> Icons.Default.Home
        lower.contains("transporte") || lower.contains("carro") || lower.contains("combust") || lower.contains("uber") -> Icons.Default.DirectionsCar
        lower.contains("cart") || lower.contains("fatura") || lower.contains("crédito") -> Icons.Default.CreditCard
        lower.contains("saúde") || lower.contains("médic") || lower.contains("remédio") || lower.contains("farm") -> Icons.Default.MedicalServices
        lower.contains("educa") || lower.contains("curso") || lower.contains("faculdade") -> Icons.Default.School
        lower.contains("compra") || lower.contains("shopping") -> Icons.Default.ShoppingCart
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }
}
