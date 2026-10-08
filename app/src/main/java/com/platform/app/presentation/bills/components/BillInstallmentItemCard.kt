package com.platform.app.presentation.bills.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillInstallmentItemCard(
    installment: BillInstallment,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onTogglePayment: () -> Unit,
    onSelectInstallment: () -> Unit
) {
    val isToday = !installment.isPaid && DateUtils.isToday(installment.dueDate)
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis() && !isToday

    val fallbackColor = MaterialTheme.colorScheme.onSurfaceVariant
    val catColor = remember(installment.categoryColorHex, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            fallbackColor
        }
    }

    val cardBorder = when {
        isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        installment.isPaid -> BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
        isOverdue -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
        isToday -> BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val cardColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        installment.isPaid -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSelectInstallment,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox ou Botão Circular de Pagamento
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.size(34.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(onClick = onTogglePayment),
                    contentAlignment = Alignment.Center
                ) {
                    if (installment.isPaid) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(SuccessGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = AppStrings.Status.PAID,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else Color.Transparent,
                                    CircleShape
                                )
                                .border(
                                    width = 1.6.dp,
                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informações Centrais
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = installment.billTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Indicador de parcelamento se for parcelamento real (totalInstallments > 1)
                    if (installment.type == BillType.INSTALLMENT && installment.totalInstallments > 1) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${installment.installmentNumber}/${installment.totalInstallments}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Categoria
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = PlatformIconCatalog.getIcon(installment.categoryIconName),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = installment.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Lado Direito: Valor e Vencimento / Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                if (installment.isPaid) {
                    // Badge moderna e positiva "Pago" com micro ícone
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = AppStrings.Status.PAID,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                } else {
                    Text(
                        text = when {
                            isOverdue -> "Venceu ${DateUtils.formatDate(installment.dueDate)}"
                            isToday -> "Vence hoje"
                            else -> "Vence ${DateUtils.formatDate(installment.dueDate)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isOverdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isOverdue -> MaterialTheme.colorScheme.error
                            isToday -> WarningAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
