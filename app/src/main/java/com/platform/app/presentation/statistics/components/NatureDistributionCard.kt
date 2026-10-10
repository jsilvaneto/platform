package com.platform.app.presentation.statistics.components

import com.platform.app.presentation.theme.PlatformShapes

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.domain.model.BudgetRigidityAnalysis
import com.platform.app.domain.model.BudgetRigidityCalculator
import com.platform.app.domain.model.BudgetRigidityStatus
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.NatureSpend
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.InfoCyan
import com.platform.app.presentation.theme.InfoCyanContainer
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.SuccessGreenContainer
import com.platform.app.presentation.theme.WarningAmber
import com.platform.app.presentation.theme.WarningAmberContainer
import java.util.Locale

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

    val isAlert = analysis.status == BudgetRigidityStatus.ENGESSADO ||
            analysis.status == BudgetRigidityStatus.SOBRECARREGADO ||
            analysis.status == BudgetRigidityStatus.SEM_POUPANCA
    val isLifestyle = analysis.status == BudgetRigidityStatus.ESTILO_DE_VIDA_ELEVADO
    val isExcellent = analysis.status == BudgetRigidityStatus.EXCELENTE

    PlatformCard {
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

            // Diagnóstico de Rigidez Inteligente
            Surface(
                shape = PlatformShapes.medium,
                color = when {
                    isAlert -> WarningAmberContainer.copy(alpha = 0.4f)
                    isLifestyle -> InfoCyanContainer.copy(alpha = 0.4f)
                    isExcellent -> SuccessGreenContainer.copy(alpha = 0.4f)
                    analysis.status == BudgetRigidityStatus.EQUILIBRADO -> SuccessGreenContainer.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isAlert -> Icons.Default.Warning
                            isLifestyle -> Icons.Default.Info
                            isExcellent -> Icons.Default.CheckCircle
                            analysis.status == BudgetRigidityStatus.EQUILIBRADO -> Icons.Default.Lightbulb
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when {
                            isAlert -> WarningAmber
                            isLifestyle -> InfoCyan
                            isExcellent || analysis.status == BudgetRigidityStatus.EQUILIBRADO -> SuccessGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                natureDistribution.forEach { item ->
                    val color = try { Color(item.nature.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
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
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = (item.percentage / 100f).coerceIn(0f, 1f),
                            height = 6.dp,
                            progressColor = color,
                            trackColor = color.copy(alpha = 0.15f)
                        )
                    }
                }

                // Perna de Poupança (Metas Financeiras)
                val savingsColor = SuccessGreen
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(savingsColor, CircleShape)
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
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    PlatformProgressBar(
                        progress = (savingsPercentage / 100f).coerceIn(0f, 1f),
                        height = 6.dp,
                        progressColor = savingsColor,
                        trackColor = savingsColor.copy(alpha = 0.15f)
                    )
                }

                val displayTotalCents = if (analysis.totalBudgetCents > 0L) analysis.totalBudgetCents else totalDueCents
                if (displayTotalCents > 0L) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                }
            }
        }
    }
}
