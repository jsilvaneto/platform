package com.platform.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber

/**
 * Componente visual executivo do Cartão de Crédito virtual (Apple Card / Revolut Style).
 * Respeita a proporção bancária oficial (aspectRatio 1.586f) com acabamento acetinado,
 * chip EMV metálico, termômetro de limite inteligente e informações de ciclo de corte.
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
    val baseColor = try {
        Color(android.graphics.Color.parseColor(card.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.95f),
            baseColor.copy(alpha = 0.75f),
            Color(0xFF121418)
        )
    )

    val usageRatio = if (card.totalLimitCents > 0) {
        (summary.usedLimitCents.toFloat() / card.totalLimitCents.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val limitGaugeColor = when {
        usageRatio > 0.75f -> UrgentRed
        usageRatio > 0.40f -> WarningAmber
        else -> SuccessGreen
    }

    val selectionBorder = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = selectionBorder,
        modifier = modifier
            .width(300.dp)
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Linha Superior: Nome do Cartão, Chip EMV, Contactless e Ações
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Chip EMV Metálico Estilizado
                        EmvChipGraphic()

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Contactless",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Excluir",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Centro: Nome do Cartão e Limite Disponível
                Column {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Disponível: ${CurrencyUtils.formatCentsToCurrency(summary.availableLimitCents)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Termômetro de Limite Inteligente com PlatformProgressBar
                    PlatformProgressBar(
                        progress = usageRatio,
                        height = 5.dp,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        progressColor = limitGaugeColor
                    )
                }

                // Rodapé do Cartão: Limite Total, Data de Fechamento e Vencimento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Limite: ${CurrencyUtils.formatCentsToCurrency(card.totalLimitCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Usado: ${CurrencyUtils.formatCentsToCurrency(summary.usedLimitCents)} (${(usageRatio * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = limitGaugeColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Fecha dia ${card.closingDay}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "Vence dia ${card.dueDay}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Desenho vetorial de um Chip EMV bancário com linhas de contato.
 */
@Composable
private fun EmvChipGraphic(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 30.dp, height = 22.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFD4AF37))
            .border(1.dp, Color(0xFFB8860B), RoundedCornerShape(4.dp))
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(0.5.dp, Color(0xFF8B7500), RoundedCornerShape(2.dp))
        )
    }
}
