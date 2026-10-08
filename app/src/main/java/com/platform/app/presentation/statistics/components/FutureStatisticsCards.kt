package com.platform.app.presentation.statistics.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.CompletingInstallmentSummary
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.ErrorRed
import com.platform.app.presentation.theme.ErrorRedContainer
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.SuccessGreenContainer
import com.platform.app.presentation.theme.WarningAmber
import com.platform.app.presentation.theme.WarningAmberContainer

@Composable
fun FutureHeroCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                icon = Icons.Default.Timeline,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "Visão Futura & Previsibilidade",
                subtitle = "Compromissos assumidos para os próximos 6 a 12 meses"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiBox(
                    label = "Total Contratado",
                    value = formatValueOrPrivate(metrics.totalCommittedFutureCents, isPrivate),
                    sublabel = "Obrigações futuras",
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Parcelas Futuras",
                    value = "${metrics.futureInstallmentsCount}",
                    sublabel = "Vencimentos",
                    modifier = Modifier.weight(1f)
                )
                MetricKpiBox(
                    label = "Custo Fixo Base",
                    value = formatValueOrPrivate(metrics.fixedMonthlyTotalCents, isPrivate),
                    sublabel = "Assinaturas/mês",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DeescalationCurveCard(
    projections: List<FutureMonthProjection>,
    isPrivate: Boolean
) {
    if (projections.isEmpty()) return

    val maxCommitted = projections.maxOfOrNull { it.totalCommittedCents }?.coerceAtLeast(1L) ?: 1L
    val highestProj = projections.maxByOrNull { it.totalCommittedCents }
    val lowestProj = projections.minByOrNull { it.totalCommittedCents }

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Curva de Desoneração (6 Meses)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Evolução decrescente dos pagamentos com amortização",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (lowestProj != null && lowestProj.totalCommittedCents < (highestProj?.totalCommittedCents ?: 0L)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreenContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Maior folga: ${lowestProj.monthLabel.take(7)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                projections.forEach { proj ->
                    val progress = (proj.totalCommittedCents.toFloat() / maxCommitted.toFloat()).coerceIn(0f, 1f)
                    val isPeak = proj == highestProj && (highestProj?.totalCommittedCents ?: 0L) > 0L
                    val isLowest = proj == lowestProj

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = proj.monthLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isPeak) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ErrorRedContainer.copy(alpha = 0.45f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                             verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                contentDescription = null,
                                                tint = ErrorRed,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Pico",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ErrorRed
                                            )
                                        }
                                    }
                                } else if (isLowest) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SuccessGreenContainer.copy(alpha = 0.45f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Folga",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatValueOrPrivate(proj.totalCommittedCents, isPrivate),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPeak) ErrorRed else if (isLowest) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${proj.installmentsCount}p)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = progress,
                            height = 6.dp,
                            progressColor = if (isPeak) ErrorRed else if (isLowest) SuccessGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletingInstallmentsCard(
    completingList: List<CompletingInstallmentSummary>,
    totalFreedFlowCents: Long,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Desoneração & Término de Parcelas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Compras parceladas que chegam ao fim nos próximos meses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = SuccessGreenContainer
                ) {
                    Text(
                        text = "+${formatValueOrPrivate(totalFreedFlowCents, isPrivate)}/mês livre",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                completingList.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Quita em ${item.completionMonthLabel} (${item.finalInstallmentNumber}/${item.totalInstallments})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Libera",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "+${formatValueOrPrivate(item.freedMonthlyAmountCents, isPrivate)}/mês",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DecisionAdvisorCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val avgSpend = metrics.historicalMonthlyAverageCents.coerceAtLeast(1L)
    val nextMonthCommitment = metrics.futureMonthsProjections.firstOrNull()?.totalCommittedCents ?: 0L
    val commitmentRatio = (nextMonthCommitment.toFloat() / avgSpend.toFloat())

    val canAffordNewInstallment = commitmentRatio < 0.75f

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Cockpit de Tomada de Decisão",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recomendações estratégicas antes de assumir novos gastos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (canAffordNewInstallment) SuccessGreenContainer.copy(alpha = 0.4f) else WarningAmberContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (canAffordNewInstallment) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (canAffordNewInstallment) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (canAffordNewInstallment) "Janela Favorável para Investimentos / Despesas"
                            else "Atenção: Margem Futura Comprometida",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (canAffordNewInstallment)
                            "Seu comprometimento projetado para o próximo mês está abaixo de 75% da média histórica. Você tem folga orçamentária para novas despesas ou aportes."
                        else
                            "Os próximos meses já possuem comprometimento próximo ou superior à sua média de gastos. Sugerido aguardar o término de parcelamentos em andamento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
