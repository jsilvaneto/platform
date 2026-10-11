package com.platform.app.presentation.bills.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.calculateTotalCents
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformListRow
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.PlatformStatusType

/**
 * Lista de parcelas e lançamentos de contas padronizada com [PlatformListRow] (Prompt 56 e 60):
 * - Agrupamento por mês expansível.
 * - Linhas com ícone 36dp (fundo sup2), título, subtítulo, valor tabular e chip de status.
 * - Swipe para pagar em lote ou individual.
 * - Long-press para seleção múltipla.
 */
@Composable
fun BillsInstallmentList(
    groupedByMonth: Map<String, List<BillInstallment>>,
    expandedMonths: Set<String>,
    currentMonthLabel: String,
    isSelectionMode: Boolean,
    selectedInstallmentIds: Set<String>,
    onToggleMonthExpand: (String) -> Unit,
    onToggleAllMonths: (Boolean) -> Unit,
    onToggleSelect: (String) -> Unit,
    onLongClickSelect: (String) -> Unit,
    onTogglePayment: (BillInstallment) -> Unit,
    onSelectInstallment: (BillInstallment) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 88.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (groupedByMonth.size > 1) {
            item(key = "toggle_all_months") {
                val allExpanded = groupedByMonth.keys.isNotEmpty() && groupedByMonth.keys.all { it in expandedMonths }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${groupedByMonth.size} meses listados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { onToggleAllMonths(allExpanded) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (allExpanded) "Recolher todos" else "Expandir todos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        groupedByMonth.forEach { (monthLabel, monthItems) ->
            val isExpanded = monthLabel in expandedMonths
            val isCurrentMonth = monthLabel.equals(currentMonthLabel, ignoreCase = true)

            item(key = "header_$monthLabel") {
                val monthTotal = remember(monthItems) { monthItems.calculateTotalCents() }
                BillsMonthSectionHeader(
                    monthLabel = monthLabel,
                    monthTotal = monthTotal,
                    itemCount = monthItems.size,
                    isExpanded = isExpanded,
                    isCurrentMonth = isCurrentMonth,
                    onToggleExpand = { onToggleMonthExpand(monthLabel) }
                )
            }

            if (isExpanded) {
                items(monthItems, key = { it.id }) { installment ->
                    val isSelected = installment.id in selectedInstallmentIds
                    val now = System.currentTimeMillis()
                    val isToday = !installment.isPaid && DateUtils.isToday(installment.dueDate)
                    val isOverdue = !installment.isPaid && installment.dueDateMillis < now && !isToday

                    val (statusText, statusType) = when {
                        installment.isPaid -> AppStrings.Status.PAID to PlatformStatusType.PAID
                        isOverdue -> {
                            val daysLate = ((now - installment.dueDateMillis) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(1)
                            (if (daysLate == 1) "Atrasada 1 dia" else "Atrasada $daysLate dias") to PlatformStatusType.OVERDUE
                        }
                        isToday -> "Vence hoje" to PlatformStatusType.ATTENTION
                        else -> {
                            val daysUntil = ((installment.dueDateMillis - now) / (24 * 3600 * 1000L)).toInt().coerceAtLeast(1)
                            if (daysUntil <= 7) {
                                (if (daysUntil == 1) "Vence amanhã" else "Em $daysUntil dias") to PlatformStatusType.ATTENTION
                            } else {
                                null to null
                            }
                        }
                    }

                    val subtitle = when {
                        installment.isPaid -> "Pago em ${DateUtils.formatDayMonth(installment.paidAt ?: installment.dueDateMillis)}"
                        installment.totalInstallments > 1 -> "Parcela ${installment.installmentNumber}/${installment.totalInstallments} · Vence ${DateUtils.formatDayMonth(installment.dueDate)}"
                        else -> "Vence dia ${DateUtils.formatDayMonth(installment.dueDate)}"
                    }

                    PlatformListRow(
                        title = installment.billTitle,
                        subtitle = subtitle,
                        amountCents = installment.amountCents,
                        icon = PlatformIconCatalog.getIcon(installment.categoryIconName),
                        statusText = statusText,
                        statusType = statusType,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelect(installment.id)
                            } else {
                                onSelectInstallment(installment)
                            }
                        },
                        onLongClick = { onLongClickSelect(installment.id) },
                        onSwipeToPay = if (!isSelectionMode && !installment.isPaid) { { onTogglePayment(installment) } } else null
                    )
                }
            }
        }
    }
}
