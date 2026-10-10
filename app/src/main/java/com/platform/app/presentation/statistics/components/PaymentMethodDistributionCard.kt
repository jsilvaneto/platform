package com.platform.app.presentation.statistics.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.BrandPrimary
import com.platform.app.presentation.theme.InfoCyan
import com.platform.app.presentation.theme.PlatformColorPalette
import java.util.Locale

@Composable
fun PaymentMethodDistributionCard(
    metrics: FinancialDashboardMetrics,
    isPrivate: Boolean
) {
    val total = metrics.totalDueMonthCents
    if (total <= 0L) return

    val distribution = metrics.paymentMethodsDistribution
    val cardPct = metrics.creditCardPercentage
    val nonCardPct = (100f - cardPct).coerceAtLeast(0f)

    val palette = remember { PlatformColorPalette.chartPalette }

    PlatformCard {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho
            Text(
                text = AppStrings.Glossary.HOW_YOU_PAY,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Distribuição dos pagamentos realizados no mês",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Barra Segmentada Proporcional no Topo
            if (distribution.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(PlatformShapes.pill)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    distribution.forEachIndexed { index, item ->
                        if (item.percentage > 0f) {
                            val color = palette[index % palette.size]
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(item.percentage.coerceAtLeast(0.5f))
                                    .background(color)
                            )
                        }
                    }
                }
            } else {
                // Fallback bifurcado Cartão vs À Vista
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(PlatformShapes.pill)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (cardPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(cardPct.coerceAtLeast(1f))
                                .background(BrandPrimary)
                        )
                    }
                    if (nonCardPct > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(nonCardPct.coerceAtLeast(1f))
                                .background(InfoCyan)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Detalhamento por Forma de Pagamento com Porcentagens e Barras Individuais
            if (distribution.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    distribution.forEachIndexed { index, item ->
                        val color = palette[index % palette.size]
                        val icon = when {
                            item.methodName.contains("Pix", ignoreCase = true) -> Icons.Default.QrCode
                            item.methodName.contains("Cartão", ignoreCase = true) || item.methodName.contains("Crédito", ignoreCase = true) -> Icons.Default.CreditCard
                            item.methodName.contains("Boleto", ignoreCase = true) -> Icons.AutoMirrored.Filled.ReceiptLong
                            item.methodName.contains("Dinheiro", ignoreCase = true) || item.methodName.contains("Espécie", ignoreCase = true) -> Icons.Default.Payments
                            item.methodName.contains("Conta", ignoreCase = true) || item.methodName.contains("Transferência", ignoreCase = true) -> Icons.Default.AccountBalance
                            else -> Icons.Default.Payments
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(color.copy(alpha = 0.14f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.methodName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.count > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${item.count})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Text(
                                    text = "${formatValueOrPrivate(item.amountCents, isPrivate)} (${String.format(Locale.getDefault(), "%.1f", item.percentage)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            PlatformProgressBar(
                                progress = (item.percentage / 100f).coerceIn(0f, 1f),
                                height = 5.dp,
                                progressColor = color,
                                trackColor = color.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Cartão de Crédito",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatValueOrPrivate(metrics.creditCardSpendCents, isPrivate)} (${String.format(Locale.getDefault(), "%.0f", cardPct)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = InfoCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Débito / Pix / Conta",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatValueOrPrivate(metrics.nonCardSpendCents, isPrivate)} (${String.format(Locale.getDefault(), "%.0f", nonCardPct)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
