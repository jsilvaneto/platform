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
import com.platform.app.domain.model.BillInstallment

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
                val monthTotal = remember(monthItems) { monthItems.sumOf { it.amountCents } }
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

                    BillInstallmentItemCard(
                        installment = installment,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onToggleSelect = { onToggleSelect(installment.id) },
                        onLongClick = { onLongClickSelect(installment.id) },
                        onTogglePayment = { onTogglePayment(installment) },
                        onSelectInstallment = { onSelectInstallment(installment) }
                    )
                }
            }
        }
    }
}
