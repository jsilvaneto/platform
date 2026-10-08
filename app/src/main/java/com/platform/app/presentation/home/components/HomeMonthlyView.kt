package com.platform.app.presentation.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.usecase.MonthlyForecastResult
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.home.HomeUiAction
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber

fun LazyListScope.renderMonthlyView(
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
                                text = AppStrings.Status.ALL_PAID,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Text(
                        text = AppStrings.Status.ALL_PAID_PERCENT,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = AppStrings.Home.FORECAST_REMAINING,
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
                            text = AppStrings.Home.FORECAST_REMAINING,
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
