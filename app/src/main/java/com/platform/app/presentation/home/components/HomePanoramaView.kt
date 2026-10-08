package com.platform.app.presentation.home.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.platform.app.domain.model.PayableItem
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.home.HomeUiState
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

fun LazyListScope.renderPanoramaView(
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
