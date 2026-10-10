package com.platform.app.presentation.recurring.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.recurring.BillWithInstallments
import com.platform.app.presentation.recurring.TimelineMonthSummary
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@Composable
fun TimelineMonthCard(
    month: TimelineMonthSummary,
    allItems: List<BillWithInstallments>,
    onTogglePayment: (String, Boolean) -> Unit,
    onSelectPlan: (BillWithInstallments) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthLabel = remember { DateUtils.formatMonthYear(System.currentTimeMillis()) }
    val isCurrentMonth = remember(month.monthLabel, currentMonthLabel) {
        month.monthLabel.equals(currentMonthLabel, ignoreCase = true)
    }
    var isExpanded by rememberSaveable(month.monthLabel) { mutableStateOf(isCurrentMonth) }

    PlatformCard(
        modifier = modifier,
        shape = PlatformShapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header do Mês
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isCurrentMonth) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = month.monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrentMonth) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = PlatformShapes.extraSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Mês Atual",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${month.installmentsCount} ${if (month.installmentsCount == 1) "parcela" else "parcelas"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(month.totalCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (month.pendingCents > 0L && month.paidCents > 0L) {
                            Text(
                                text = "Resta: ${CurrencyUtils.formatCentsToCurrency(month.pendingCents)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (month.pendingCents == 0L && month.totalCents > 0L) {
                            Text(
                                text = "Mês Quitado",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Recolher mês" else "Expandir mês",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Parcelas individuais do mês
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        month.items.forEach { inst ->
                            val parentPlan = allItems.find { it.bill.id == inst.billId }
                            val title = parentPlan?.bill?.title ?: "Parcela"
                            val contact = inst.contactName ?: parentPlan?.contactName
                            val isToday = !inst.isPaid && DateUtils.isToday(inst.dueDate)
                            val isOverdue = !inst.isPaid && inst.dueDate < System.currentTimeMillis() && !isToday

                            Surface(
                                shape = PlatformShapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (parentPlan != null) onSelectPlan(parentPlan)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(
                                            onClick = { onTogglePayment(inst.id, inst.isPaid) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (inst.isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                                contentDescription = if (inst.isPaid) "Paga" else "Marcar como paga",
                                                tint = if (inst.isPaid) SuccessGreen else if (isOverdue) MaterialTheme.colorScheme.error else if (isToday) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (!contact.isNullOrBlank()) {
                                                    Surface(
                                                        shape = PlatformShapes.extraSmall,
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Person,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Text(
                                                                text = contact,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }

                                                val installmentLabel = when {
                                                    inst.type == BillType.RECURRING -> {
                                                        if (parentPlan?.hasVariableFirstInstallment == true && inst.installmentNumber == 1) {
                                                            "1ª Recorrência (Adesão)"
                                                        } else {
                                                            "Recorrência"
                                                        }
                                                    }
                                                    else -> "Parcela ${inst.installmentNumber}/${inst.totalInstallments}"
                                                }

                                                Text(
                                                    text = when {
                                                        inst.isPaid -> "$installmentLabel • Paga"
                                                        isOverdue -> "$installmentLabel • Venceu ${DateUtils.formatDate(inst.dueDate)}"
                                                        isToday -> "$installmentLabel • Vence hoje"
                                                        else -> "$installmentLabel • ${DateUtils.formatDate(inst.dueDate)}"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isOverdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = when {
                                                        inst.isPaid -> SuccessGreen
                                                        isOverdue -> MaterialTheme.colorScheme.error
                                                        isToday -> WarningAmber
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyUtils.formatCentsToCurrency(inst.amountCents),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inst.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (parentPlan?.hasVariableFirstInstallment == true && inst.installmentNumber == 1 && inst.type == BillType.RECURRING) {
                                            Surface(
                                                shape = PlatformShapes.extraSmall,
                                                color = WarningAmber.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Adesão/Entrada",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = WarningAmber,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
