package com.platform.app.presentation.statistics.components

import com.platform.app.presentation.theme.PlatformShapes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.ContactSpend
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.PastMonthHistory
import com.platform.app.presentation.components.PlatformAvatar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.ErrorRed
import com.platform.app.presentation.theme.ErrorRedContainer
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.SuccessGreenContainer
import com.platform.app.presentation.theme.WarningAmber
import java.util.Locale

@Composable
fun PastHeroCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.History,
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Histórico & Desempenho",
                subtitle = "Visão consolidada do comportamento financeiro passado"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiBox(
                    label = "Média Mensal",
                    value = formatValueOrPrivate(metrics.historicalMonthlyAverageCents, isPrivate),
                    sublabel = "Custo base apurado",
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Total Liquidado",
                    value = formatValueOrPrivate(metrics.totalHistoricalPaidCents, isPrivate),
                    sublabel = "Histórico pago",
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                val onTimeRate = metrics.onTimePaymentRate
                MetricKpiBox(
                    label = "Pontualidade",
                    value = if (onTimeRate != null) "$onTimeRate%" else "Sem dados",
                    sublabel = "Pagas no prazo",
                    valueColor = when {
                        onTimeRate == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        onTimeRate >= 90 -> SuccessGreen
                        else -> WarningAmber
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PreviousMonthComparisonCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val diffCents = metrics.totalDueMonthCents - metrics.previousMonthDueCents
    val hasPrevData = metrics.previousMonthDueCents > 0L
    val isHigher = diffCents > 0L
    val pctDiff = if (hasPrevData) {
        (diffCents.toFloat() / metrics.previousMonthDueCents.toFloat()) * 100f
    } else 0f

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comparativo com Mês Anterior",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (hasPrevData) {
                    Surface(
                        shape = PlatformShapes.pill,
                        color = if (isHigher) ErrorRedContainer else SuccessGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isHigher) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isHigher) ErrorRed else SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%+.1f%%", pctDiff),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isHigher) ErrorRed else SuccessGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Mês Anterior",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatValueOrPrivate(metrics.previousMonthDueCents, isPrivate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Variação Absoluta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (!hasPrevData) "Sem histórico anterior"
                        else if (isHigher) "+${formatValueOrPrivate(diffCents, isPrivate)}"
                        else "-${formatValueOrPrivate(-diffCents, isPrivate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!hasPrevData) MaterialTheme.colorScheme.onSurfaceVariant else if (isHigher) ErrorRed else SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
fun PastHistoryChartCard(
    pastHistory: List<PastMonthHistory>,
    isPrivate: Boolean,
    onSelectMonth: (Long) -> Unit
) {
    if (pastHistory.isEmpty()) return

    val maxDueCents = pastHistory.maxOfOrNull { it.totalDueCents }?.coerceAtLeast(1L) ?: 1L

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Evolução dos Últimos 6 Meses",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Pagamentos devidos e taxa de liquidação de cada período",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pastHistory.forEach { item ->
                    val progress = (item.totalDueCents.toFloat() / maxDueCents.toFloat()).coerceIn(0f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PlatformShapes.small)
                            .clickable { onSelectMonth(item.monthMillis) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.monthLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatValueOrPrivate(item.totalDueCents, isPrivate),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = PlatformShapes.extraSmall,
                                    color = if (item.paidRate >= 95) SuccessGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${item.paidRate}% pago",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.paidRate >= 95) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = progress,
                            height = 6.dp,
                            progressColor = if (item.paidRate == 100) SuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PastExtremesCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    if (metrics.highestSpendMonthCents <= 0L && metrics.lowestSpendMonthCents <= 0L) return

    PlatformCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = PlatformShapes.medium,
                color = ErrorRedContainer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Mês Mais Pesado",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = metrics.highestSpendMonthLabel.ifEmpty { "N/A" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatValueOrPrivate(metrics.highestSpendMonthCents, isPrivate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = PlatformShapes.medium,
                color = SuccessGreenContainer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Mês Mais Econômico",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = metrics.lowestSpendMonthLabel.ifEmpty { "N/A" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatValueOrPrivate(metrics.lowestSpendMonthCents, isPrivate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
fun TopContactsCard(
    contacts: List<ContactSpend>,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Maiores Destinatários / Fornecedores",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Onde os maiores volumes financeiros foram direcionados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                contacts.forEach { contact ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            PlatformAvatar(
                                name = contact.contactName,
                                size = 28.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = contact.contactName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${formatValueOrPrivate(contact.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", contact.percentage)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
