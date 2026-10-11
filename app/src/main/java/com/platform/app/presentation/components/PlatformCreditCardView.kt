package com.platform.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

/**
 * Cartão de crédito virtual plano e minimalista (Prompt 61):
 * - Container: superfície plana (sup2), borda 1dp na cor de linha, raio 20dp, altura 170dp.
 * - Informações: Nome do cartão, "Fecha dia X" e "Vence dia Y".
 * - Barra de limite de 8dp com "Usado R$ X de R$ Y":
 *   - verde até 70% (PAID)
 *   - âmbar de 70% a 90% (ATTENTION)
 *   - vermelho acima de 90% (OVERDUE)
 *   - sempre com percentual em texto.
 */
@Composable
fun PlatformCreditCardView(
    summary: CreditCardWithInvoiceSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = summary.card
    val isDark = isSystemInDarkTheme()

    val usageRatio = if (card.totalLimitCents > 0) {
        (summary.usedLimitCents.toFloat() / card.totalLimitCents.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val usagePercent = (usageRatio * 100).toInt()

    val limitStatusType = when {
        usageRatio <= 0.70f -> PlatformStatusType.PAID
        usageRatio <= 0.90f -> PlatformStatusType.ATTENTION
        else -> PlatformStatusType.OVERDUE
    }
    val limitGaugeColor = PlatformStatusStyle.getColors(limitStatusType, isDark).contentColor

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    PlatformSurface(
        shape = PlatformShapes.card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .width(280.dp)
            .height(170.dp)
            .clip(PlatformShapes.card)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linha Superior: Nome do Cartão + Ações de Editar/Excluir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Meio: Ciclo de faturamento "Fecha dia X · Vence dia Y"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Fecha dia ${card.closingDay} · Vence dia ${card.dueDay}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Limite: Barra de 8dp + "Usado R$ X de R$ Y (Z%)"
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Usado ${CurrencyUtils.formatCentsToCurrency(summary.usedLimitCents)} de ${CurrencyUtils.formatCentsToCurrency(card.totalLimitCents)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$usagePercent%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Barra de limite de 8dp plana
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(PlatformShapes.small)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (usageRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(usageRatio)
                                .height(8.dp)
                                .clip(PlatformShapes.small)
                                .background(limitGaugeColor)
                        )
                    }
                }

                // Disponível
                Text(
                    text = "Disponível: ${CurrencyUtils.formatCentsToCurrency(summary.availableLimitCents)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
