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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.domain.model.BudgetRigidityAnalysis
import com.platform.app.domain.model.BudgetRigidityCalculator
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.NatureSpend
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.PlatformShapes
import java.util.Locale

/**
 * Card 'Equilíbrio do orçamento' minimalista (Prompt 62):
 * - Barras horizontais em tons estritamente neutros.
 * - Despesas necessárias, desejos e poupança NÃO são status e não usam cores semafóricas.
 */
@Composable
fun NatureDistributionCard(
    natureDistribution: List<NatureSpend>,
    savingsCents: Long = 0L,
    savingsPercentage: Float = 0f,
    budgetRigidity: BudgetRigidityAnalysis? = null,
    totalDueCents: Long,
    isPrivate: Boolean
) {
    val analysis = budgetRigidity ?: BudgetRigidityCalculator.calculate(
        mandatoryAmountCents = natureDistribution.firstOrNull { it.nature == ExpenseNature.OBRIGATORIO }?.amountCents ?: 0L,
        necessaryAmountCents = natureDistribution.firstOrNull { it.nature == ExpenseNature.NECESSARIO }?.amountCents ?: 0L,
        wantsAmountCents = natureDistribution.firstOrNull { it.nature == ExpenseNature.DESEJA }?.amountCents ?: 0L,
        noneAmountCents = natureDistribution.firstOrNull { it.nature == ExpenseNature.NENHUM }?.amountCents ?: 0L,
        savingsAmountCents = savingsCents
    )

    PlatformCard(
        shape = PlatformShapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = AppStrings.Glossary.BUDGET_BALANCE,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Divisão por essenciais, desejos e poupança",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnóstico de Rigidez em Container Neutro
            PlatformSurface(
                shape = PlatformShapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = analysis.badgeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = analysis.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
            val isLargeFont = fontScale > 1.25f

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                natureDistribution.forEach { item ->
                    // Tons neutros progressivos: não são cores de status
                    val neutralTone = when (item.nature) {
                        ExpenseNature.OBRIGATORIO -> MaterialTheme.colorScheme.onSurface
                        ExpenseNature.NECESSARIO -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        ExpenseNature.DESEJA -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)
                        ExpenseNature.NENHUM -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    }

                    Column {
                        if (isLargeFont) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(neutralTone, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.nature.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${formatValueOrPrivate(item.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", item.percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(neutralTone, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.nature.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${formatValueOrPrivate(item.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", item.percentage)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = (item.percentage / 100f).coerceIn(0f, 1f),
                            height = 6.dp,
                            progressColor = neutralTone,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Perna de Poupança (Metas Financeiras) em tom neutro
                val savingsTone = MaterialTheme.colorScheme.onSurfaceVariant
                Column {
                    if (isLargeFont) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(savingsTone, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Poupança (Metas)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${formatValueOrPrivate(savingsCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", savingsPercentage)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(savingsTone, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Poupança (Metas)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${formatValueOrPrivate(savingsCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", savingsPercentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    PlatformProgressBar(
                        progress = (savingsPercentage / 100f).coerceIn(0f, 1f),
                        height = 6.dp,
                        progressColor = savingsTone,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                val displayTotalCents = if (analysis.totalBudgetCents > 0L) analysis.totalBudgetCents else totalDueCents
                if (displayTotalCents > 0L) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isLargeFont) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Total Orçado (Despesas + Aportes)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatValueOrPrivate(displayTotalCents, isPrivate),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Orçado (Despesas + Aportes)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatValueOrPrivate(displayTotalCents, isPrivate),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
