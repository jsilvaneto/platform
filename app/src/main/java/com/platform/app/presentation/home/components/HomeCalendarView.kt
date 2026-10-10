package com.platform.app.presentation.home.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PayableItem
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.home.CalendarDayItem
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber
import java.util.Calendar

fun LazyListScope.renderCalendarView(
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
            PlatformCard(shape = PlatformShapes.large) {
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
                                shape = PlatformShapes.extraSmall,
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

        PlatformCard(shape = PlatformShapes.large) {
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
                    shape = PlatformShapes.medium,
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
        shape = PlatformShapes.medium,
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
        shape = PlatformShapes.medium,
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
