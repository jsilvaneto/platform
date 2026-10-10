package com.platform.app.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.usecase.MonthlyForecastResult
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.home.HomeUiAction
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber

fun LazyListScope.renderTodayListView(
    uiState: HomeUiState,
    isPrivate: Boolean,
    onPayItem: (PayableItem) -> Unit,
    onAddExpense: () -> Unit,
    onNavigateToBills: () -> Unit,
    onAction: (HomeUiAction) -> Unit
) {
    val forecast = uiState.forecastResult ?: return
    val totalMonthCents = forecast.totalForecastCents + forecast.totalPaidCents
    val isMonthFullyPaid = forecast.totalItemsCount > 0 &&
            forecast.overdueCount == 0 &&
            forecast.dueTodayCount == 0 &&
            forecast.next7DaysCount == 0 &&
            forecast.laterInMonthItems.isEmpty() &&
            forecast.paidCount > 0

    val pendingItems = forecast.overdueItems + forecast.dueTodayItems + forecast.next7DaysItems + forecast.laterInMonthItems
    val pendingCount = pendingItems.size
    val nextDueItem = pendingItems.minByOrNull { it.dueDate }
    val nextDueDay = nextDueItem?.let { DateUtils.getDayOfMonth(it.dueDate) }

    // 1. Hero Card: Valor Restante, Barra de Progresso e Linha de Status
    item {
        TodayHeroCard(
            totalForecastCents = forecast.totalForecastCents,
            totalMonthCents = totalMonthCents,
            totalPaidCents = forecast.totalPaidCents,
            isFullyPaid = isMonthFullyPaid,
            pendingCount = pendingCount,
            nextDueDay = nextDueDay,
            isPrivate = isPrivate
        )
    }

    // 2. KPIs em Grid 2x2 (sem corte ou rolagem horizontal)
    item {
        TodayKpiGrid(
            totalMonthCents = totalMonthCents,
            totalPaidCents = forecast.totalPaidCents,
            dueTodayCents = forecast.dueTodayTotalCents,
            dueTodayCount = forecast.dueTodayCount,
            next7DaysCents = forecast.next7DaysTotalCents,
            next7DaysCount = forecast.next7DaysCount,
            isPrivate = isPrivate
        )
    }

    // 3. Precisa de Atenção (Atrasadas, Vencem Hoje e em 7 Dias)
    val hasAttentionItems = forecast.overdueItems.isNotEmpty() ||
            forecast.dueTodayItems.isNotEmpty() ||
            forecast.next7DaysItems.isNotEmpty()

    if (hasAttentionItems) {
        val totalAttentionCount = forecast.overdueCount + forecast.dueTodayCount + forecast.next7DaysCount
        val totalAttentionCents = forecast.overdueTotalCents + forecast.dueTodayTotalCents + forecast.next7DaysTotalCents

        item {
            UrgencySectionHeader(
                title = AppStrings.Home.ATTENTION_NEEDED,
                count = totalAttentionCount,
                badgeColor = if (forecast.overdueCount > 0) UrgentRed else WarningAmber,
                totalCents = totalAttentionCents,
                isPrivate = isPrivate
            )
        }

        if (forecast.overdueItems.isNotEmpty()) {
            items(forecast.overdueItems, key = { it.id }) { item ->
                PayableItemCard(
                    item = item,
                    isPrivate = isPrivate,
                    onPay = { onPayItem(item) }
                )
            }
        }

        if (forecast.dueTodayItems.isNotEmpty()) {
            items(forecast.dueTodayItems, key = { it.id }) { item ->
                PayableItemCard(
                    item = item,
                    isPrivate = isPrivate,
                    onPay = { onPayItem(item) }
                )
            }
        }

        if (forecast.next7DaysItems.isNotEmpty()) {
            items(forecast.next7DaysItems, key = { it.id }) { item ->
                PayableItemCard(
                    item = item,
                    isPrivate = isPrivate,
                    onPay = { onPayItem(item) }
                )
            }
        }
    } else if (forecast.totalItemsCount > 0) {
        // Estado positivo compacto quando nada precisa de atenção
        item {
            TodayAllCaughtUpCard()
        }
    }

    // 4. Próximos Pagamentos (Máximo de 5)
    if (forecast.laterInMonthItems.isNotEmpty()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacingSmall, bottom = Dimens.spacingExtraSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AppStrings.Home.UPCOMING_PAYMENTS_SECTION} (${forecast.laterInMonthItems.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onNavigateToBills,
                    shape = PlatformShapes.small
                ) {
                    Text(
                        text = AppStrings.Home.SEE_ALL_BILLS,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        items(forecast.laterInMonthItems.take(5), key = { it.id }) { item ->
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

    // 6. Estado Vazio se nenhum lançamento existir
    if (forecast.totalItemsCount == 0) {
        item {
            EmptyForecastCard(onAddExpense = onAddExpense)
        }
    }
}

@Composable
private fun TodayHeroCard(
    totalForecastCents: Long,
    totalMonthCents: Long,
    totalPaidCents: Long,
    isFullyPaid: Boolean,
    pendingCount: Int,
    nextDueDay: Int?,
    isPrivate: Boolean
) {
    val progress = if (totalMonthCents > 0L) {
        (totalPaidCents.toFloat() / totalMonthCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    PlatformCard(shape = PlatformShapes.large) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal)
        ) {
            Text(
                text = AppStrings.Home.FORECAST_REMAINING,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formatValueOrPrivate(totalForecastCents, isPrivate),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFullyPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            PlatformProgressBar(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1 Linha de Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isFullyPaid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.Status.ALL_PAID,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                } else if (pendingCount > 0 && nextDueDay != null) {
                    Text(
                        text = "Faltam $pendingCount contas, a próxima vence dia $nextDueDay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (pendingCount > 0) {
                    Text(
                        text = "Faltam $pendingCount contas a pagar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Nenhuma conta agendada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayKpiGrid(
    totalMonthCents: Long,
    totalPaidCents: Long,
    dueTodayCents: Long,
    dueTodayCount: Int,
    next7DaysCents: Long,
    next7DaysCount: Int,
    isPrivate: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            KpiGridCard(
                title = AppStrings.Home.MONTH_TOTAL,
                value = formatValueOrPrivate(totalMonthCents, isPrivate),
                subtitle = null,
                modifier = Modifier.weight(1f)
            )
            KpiGridCard(
                title = AppStrings.Home.PAID_SO_FAR,
                value = formatValueOrPrivate(totalPaidCents, isPrivate),
                subtitle = null,
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            KpiGridCard(
                title = AppStrings.Home.DUE_TODAY,
                value = formatValueOrPrivate(dueTodayCents, isPrivate),
                subtitle = "$dueTodayCount conta(s)",
                valueColor = if (dueTodayCount > 0) WarningAmber else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            KpiGridCard(
                title = AppStrings.Home.NEXT_7_DAYS,
                value = formatValueOrPrivate(next7DaysCents, isPrivate),
                subtitle = "$next7DaysCount conta(s)",
                valueColor = if (next7DaysCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiGridCard(
    title: String,
    value: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    PlatformCard(
        shape = PlatformShapes.medium,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TodayAllCaughtUpCard() {
    Surface(
        shape = PlatformShapes.medium,
        color = SuccessGreen.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = AppStrings.Home.ALL_CAUGHT_UP,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = AppStrings.Home.ALL_CAUGHT_UP_DESC,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
