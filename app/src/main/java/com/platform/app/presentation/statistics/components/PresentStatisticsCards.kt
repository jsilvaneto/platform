package com.platform.app.presentation.statistics.components

import com.platform.app.presentation.theme.PlatformShapes

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
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
import androidx.core.graphics.toColorInt
import com.platform.app.domain.model.AccountSpend
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.FinancialAccountType
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.ErrorRed
import com.platform.app.presentation.theme.ErrorRedContainer
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.SuccessGreenContainer
import java.util.Locale

@Composable
fun PresentHeroCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val total = metrics.totalDueMonthCents
    val paidRate = if (total > 0) ((metrics.totalPaidMonthCents.toFloat() / total.toFloat()) * 100).toInt() else 0

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    icon = Icons.Default.PieChart,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Execução do Mês",
                    subtitle = metrics.currentMonthLabel
                )

                Surface(
                    shape = PlatformShapes.pill,
                    color = if (paidRate == 100) SuccessGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$paidRate% liquidado",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (paidRate == 100) SuccessGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Total Comprometido no Mês",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatValueOrPrivate(total, isPrivate),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            PlatformProgressBar(
                progress = if (total > 0) metrics.totalPaidMonthCents.toFloat() / total.toFloat() else 0f,
                height = 8.dp,
                progressColor = SuccessGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBreakdownBox(
                    label = AppStrings.Status.PAID,
                    value = formatValueOrPrivate(metrics.totalPaidMonthCents, isPrivate),
                    color = SuccessGreen,
                    containerColor = SuccessGreenContainer,
                    modifier = Modifier.weight(1f)
                )
                StatusBreakdownBox(
                    label = AppStrings.Status.PENDING,
                    value = formatValueOrPrivate(metrics.totalPendingMonthCents, isPrivate),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )
                if (metrics.totalOverdueMonthCents > 0L) {
                    StatusBreakdownBox(
                        label = AppStrings.Status.OVERDUE_PAST,
                        value = formatValueOrPrivate(metrics.totalOverdueMonthCents, isPrivate),
                        color = ErrorRed,
                        containerColor = ErrorRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDistributionCard(
    categories: List<CategorySpend>,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gastos por Categoria",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.forEach { cat ->
                    val color = try { Color(cat.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${formatValueOrPrivate(cat.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", cat.percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        PlatformProgressBar(
                            progress = (cat.percentage / 100f).coerceIn(0f, 1f),
                            height = 6.dp,
                            progressColor = color,
                            trackColor = color.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsDistributionCard(
    accounts: List<AccountSpend>,
    isPrivate: Boolean
) {
    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = AppStrings.Glossary.SPENDING_BY_ACCOUNT,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Onde seus pagamentos foram realizados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                accounts.forEach { acc ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val icon = when (acc.accountType) {
                                FinancialAccountType.CORRENTE -> Icons.Default.AccountBalance
                                FinancialAccountType.CARTEIRA -> Icons.Default.Payments
                                FinancialAccountType.POUPANCA -> Icons.Default.Savings
                                FinancialAccountType.INVESTIMENTO -> Icons.AutoMirrored.Filled.TrendingUp
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = acc.accountType.displayName,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = acc.accountName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${acc.accountType.displayName} • ${acc.pendingBillsCount} pagamentos vinculados",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = formatValueOrPrivate(acc.totalAmountCents, isPrivate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
