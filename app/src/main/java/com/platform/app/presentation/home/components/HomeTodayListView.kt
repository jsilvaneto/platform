package com.platform.app.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformListRow
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.home.HomeUiAction
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

/**
 * Reconstrução minimalista da visualização de Lista da tela Hoje (Prompt 59):
 * 1. Hero plano: restante do mês em 40sp, barra de progresso verde de 8dp, linha "Faltam N contas · a próxima vence dia X".
 * 2. Linha de atalhos rápidos (Contas, Cartões, Análises).
 * 3. "Precisa de atenção" (atrasadas e vence em até 7 dias, com chip de status semafórico e swipe para pagar).
 * 4. "Próximos pagamentos" (máx. 5 com PlatformListRow).
 * 5. Seção colapsável de pagas no mês.
 * 6. Vazio positivo compacto sem ilustrações ou banners intrusivos.
 */
fun LazyListScope.renderTodayListView(
    uiState: HomeUiState,
    isPrivate: Boolean,
    onPayItem: (PayableItem) -> Unit,
    onAddExpense: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
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

    // 1. Hero plano (restante em 40sp, barra de 8dp, linha de status)
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

    // 2. Linha de atalhos rápidos (Contas, Cartões, Análises)
    item {
        TodayShortcutsRow(
            onNavigateToBills = onNavigateToBills,
            onNavigateToCards = onNavigateToCards,
            onNavigateToAnalytics = onNavigateToAnalytics
        )
    }

    // 3. "Precisa de atenção" (atrasadas e vence em até 7 dias, com chip de status)
    val attentionItems = forecast.overdueItems + forecast.dueTodayItems + forecast.next7DaysItems
    if (attentionItems.isNotEmpty()) {
        item {
            PlatformSectionTitle(
                text = "${AppStrings.Home.ATTENTION_NEEDED} (${attentionItems.size})",
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(attentionItems, key = { it.id }) { item ->
            val now = System.currentTimeMillis()
            val isOverdue = item.urgency == PayableUrgency.OVERDUE || (item.dueDate < now && !DateUtils.isToday(item.dueDate))
            val isToday = item.urgency == PayableUrgency.DUE_TODAY || DateUtils.isToday(item.dueDate)

            val (statusText, statusType) = when {
                isOverdue -> {
                    val daysLate = ((now - item.dueDate) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(1)
                    (if (daysLate == 1) "Atrasada 1 dia" else "Atrasada $daysLate dias") to PlatformStatusType.OVERDUE
                }
                isToday -> "Vence hoje" to PlatformStatusType.ATTENTION
                else -> {
                    val daysUntil = ((item.dueDate - now) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(1)
                    (if (daysUntil == 1) "Vence amanhã" else "Em $daysUntil dias") to PlatformStatusType.ATTENTION
                }
            }

            PlatformListRow(
                title = item.title,
                subtitle = "Vencimento: ${DateUtils.formatDayMonth(item.dueDate)}",
                amountCents = item.amountCents,
                icon = PlatformIconCatalog.getIcon(item.categoryIconName),
                statusText = statusText,
                statusType = statusType,
                onClick = { onPayItem(item) },
                onSwipeToPay = { onPayItem(item) }
            )
        }
    } else if (forecast.totalItemsCount > 0) {
        // Vazio positivo compacto quando não há pendências de atenção
        item {
            TodayAllCaughtUpCard()
        }
    }

    // 4. "Próximos pagamentos" (máx. 5)
    val upcomingItems = forecast.laterInMonthItems.take(5)
    if (upcomingItems.isNotEmpty()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformSectionTitle(
                    text = "${AppStrings.Home.UPCOMING_PAYMENTS_SECTION} (${forecast.laterInMonthItems.size})"
                )
                if (forecast.laterInMonthItems.size > 5) {
                    Text(
                        text = "${AppStrings.Home.SEE_ALL_BILLS} (${forecast.laterInMonthItems.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onNavigateToBills)
                    )
                }
            }
        }

        items(upcomingItems, key = { it.id }) { item ->
            PlatformListRow(
                title = item.title,
                subtitle = "Vencimento: ${DateUtils.formatDayMonth(item.dueDate)}",
                amountCents = item.amountCents,
                icon = PlatformIconCatalog.getIcon(item.categoryIconName),
                statusText = null,
                statusType = null,
                onClick = { onPayItem(item) },
                onSwipeToPay = { onPayItem(item) }
            )
        }
    }

    // 5. Pagas no Mês (Seção Colapsável no rodapé)
    if (forecast.paidItems.isNotEmpty()) {
        item {
            TodayCollapsiblePaidHeader(
                isExpanded = uiState.isPaidSectionExpanded,
                count = forecast.paidCount,
                totalCents = forecast.paidTotalCents,
                isPrivate = isPrivate,
                onToggle = { onAction(HomeUiAction.TogglePaidSection(!uiState.isPaidSectionExpanded)) }
            )
        }
        if (uiState.isPaidSectionExpanded) {
            items(forecast.paidItems, key = { it.id }) { item ->
                PlatformListRow(
                    title = item.title,
                    subtitle = "Pago em ${DateUtils.formatDayMonth(item.dueDate)}",
                    amountCents = item.amountCents,
                    icon = PlatformIconCatalog.getIcon(item.categoryIconName),
                    statusText = AppStrings.Status.PAID,
                    statusType = PlatformStatusType.PAID,
                    onClick = null,
                    onSwipeToPay = null
                )
            }
        }
    }

    // 6. Vazio Compacto se nenhum lançamento existir no mês
    if (forecast.totalItemsCount == 0) {
        item {
            TodayEmptyMonthCard(onAddExpense = onAddExpense)
        }
    }
}

/**
 * Hero plano com restante em 40sp, barra de progresso verde de 8dp e linha informativa.
 */
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
    val paidGreen = PlatformStatusStyle.getColors(PlatformStatusType.PAID).contentColor

    PlatformCard(
        shape = PlatformShapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal)
        ) {
            Text(
                text = AppStrings.Home.FORECAST_REMAINING,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    letterSpacing = 0.08.em,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isPrivate) {
                Text(
                    text = "R$ ••••••",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                PlatformMoneyText(
                    amountCents = totalForecastCents,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isFullyPaid) paidGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PlatformProgressBar(
                progress = progress,
                height = 8.dp,
                progressColor = paidGreen,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            val statusText = when {
                isFullyPaid -> AppStrings.Status.ALL_PAID
                pendingCount > 0 && nextDueDay != null -> {
                    if (pendingCount == 1) {
                        "${AppStrings.Home.REMAINING_SINGLE_BILL}$nextDueDay"
                    } else {
                        "${AppStrings.Home.REMAINING_BILLS_PREFIX}$pendingCount${AppStrings.Home.REMAINING_BILLS_SUFFIX}$nextDueDay"
                    }
                }
                pendingCount > 0 -> {
                    if (pendingCount == 1) AppStrings.Home.REMAINING_NO_DATE_SINGLE else "$pendingCount${AppStrings.Home.REMAINING_NO_DATE_PLURAL}"
                }
                else -> AppStrings.Home.REMAINING_NO_BILLS
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isFullyPaid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = paidGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFullyPaid) paidGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Estado positivo compacto exibido quando não há contas atrasadas ou vencendo nos próximos 7 dias.
 */
@Composable
private fun TodayAllCaughtUpCard() {
    val paidGreen = PlatformStatusStyle.getColors(PlatformStatusType.PAID).contentColor

    PlatformCard(
        shape = PlatformShapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = paidGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = AppStrings.Home.ALL_CAUGHT_UP,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = paidGreen
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

/**
 * Estado vazio compacto exibido quando o mês não possui nenhum lançamento cadastrado.
 */
@Composable
private fun TodayEmptyMonthCard(onAddExpense: () -> Unit) {
    PlatformCard(
        shape = PlatformShapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.Home.EMPTY_MONTH,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onAddExpense,
                shape = PlatformShapes.small
            ) {
                Text(
                    text = AppStrings.Home.EMPTY_MONTH_ACTION,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Cabeçalho colapsável padronizado para as contas quitadas no mês.
 */
@Composable
private fun TodayCollapsiblePaidHeader(
    isExpanded: Boolean,
    count: Int,
    totalCents: Long,
    isPrivate: Boolean,
    onToggle: () -> Unit
) {
    val paidGreen = PlatformStatusStyle.getColors(PlatformStatusType.PAID).contentColor

    PlatformCard(
        shape = PlatformShapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = paidGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${AppStrings.Filters.PAID} ($count) · ${formatValueOrPrivate(totalCents, isPrivate)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
