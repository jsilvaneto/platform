package com.platform.app.presentation.bills.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.presentation.bills.RecurringFilter
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusType
import com.platform.app.presentation.theme.line
import com.platform.app.presentation.theme.surface2
import com.platform.app.presentation.theme.text2

@Composable
fun RecurringBillsList(
    bills: List<Bill>,
    installments: List<BillInstallment>,
    selectedFilter: RecurringFilter,
    onFilterSelected: (RecurringFilter) -> Unit,
    onSelectBill: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBills = remember(bills) {
        bills.filter { !it.isPaused && it.endedAt == null && it.recurrenceEndType != RecurrenceEndType.UNTIL_DATE && it.deletedAt == null }
    }
    val pausedBills = remember(bills) {
        bills.filter { it.isPaused && it.deletedAt == null }
    }
    val endedBills = remember(bills) {
        bills.filter { (it.endedAt != null || it.recurrenceEndType == RecurrenceEndType.UNTIL_DATE) && it.deletedAt == null }
    }

    val tabItems = remember(activeBills.size, pausedBills.size, endedBills.size) {
        listOf(
            SegmentedTabItem(RecurringFilter.ACTIVE.label, activeBills.size),
            SegmentedTabItem(RecurringFilter.PAUSED.label, pausedBills.size),
            SegmentedTabItem(RecurringFilter.ENDED.label, endedBills.size)
        )
    }

    val selectedIndex = when (selectedFilter) {
        RecurringFilter.ACTIVE -> 0
        RecurringFilter.PAUSED -> 1
        RecurringFilter.ENDED -> 2
    }

    val displayedBills = when (selectedFilter) {
        RecurringFilter.ACTIVE -> activeBills
        RecurringFilter.PAUSED -> pausedBills
        RecurringFilter.ENDED -> endedBills
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Filtros em abas: Ativas | Pausadas | Encerradas
        PlatformSegmentedTabs(
            items = tabItems,
            selectedIndex = selectedIndex,
            onTabSelected = { index ->
                val newFilter = when (index) {
                    0 -> RecurringFilter.ACTIVE
                    1 -> RecurringFilter.PAUSED
                    2 -> RecurringFilter.ENDED
                    else -> RecurringFilter.ACTIVE
                }
                onFilterSelected(newFilter)
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (displayedBills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma recorrência ${selectedFilter.label.lowercase()}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.text2
                )
            }
        } else {
            val now = remember { System.currentTimeMillis() }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedBills, key = { it.id }) { bill ->
                    val billInsts = remember(installments, bill.id) {
                        installments.filter { it.billId == bill.id }
                    }
                    val nextPending = remember(billInsts) {
                        billInsts.filter { !it.isPaid && !it.isSkipped }.minByOrNull { it.dueDate }
                    }
                    val overdueCount = remember(billInsts, now) {
                        billInsts.count { !it.isPaid && !it.isSkipped && it.dueDateMillis < now }
                    }

                    RecurringBillRow(
                        bill = bill,
                        nextDueDate = nextPending?.dueDateMillis,
                        overdueCount = overdueCount,
                        onClick = { onSelectBill(bill) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecurringBillRow(
    bill: Bill,
    nextDueDate: Long?,
    overdueCount: Int,
    onClick: () -> Unit
) {
    PlatformCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container neutro de 36dp com raio 12
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(PlatformShapes.medium)
                    .background(MaterialTheme.colorScheme.surface2)
                    .border(1.dp, MaterialTheme.colorScheme.line, PlatformShapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))

                val subtitle = when {
                    bill.isPaused -> {
                        if (bill.pausedUntil != null) "Pausada até ${DateUtils.formatDate(bill.pausedUntil)}" else "Pausada sem data"
                    }
                    bill.endedAt != null || bill.recurrenceEndType == RecurrenceEndType.UNTIL_DATE -> {
                        if (bill.endedAt != null) "Encerrada em ${DateUtils.formatDate(bill.endedAt)}" else "Encerrada"
                    }
                    nextDueDate != null -> {
                        "Próx: ${DateUtils.formatDate(nextDueDate)}"
                    }
                    else -> {
                        bill.recurrenceFrequency?.label ?: "Mensal"
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.text2
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                PlatformMoneyText(
                    amountCents = bill.totalAmountCents,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                when {
                    overdueCount > 0 -> {
                        PlatformStatusChip(
                            text = "$overdueCount vencida(s)",
                            type = StatusChipType.ERROR
                        )
                    }
                    bill.isPaused -> {
                        PlatformNeutralChip(text = "Pausada")
                    }
                    bill.endedAt != null || bill.recurrenceEndType == RecurrenceEndType.UNTIL_DATE -> {
                        PlatformNeutralChip(text = "Encerrada")
                    }
                    else -> {
                        PlatformNeutralChip(text = bill.recurrenceFrequency?.label ?: "Mensal")
                    }
                }
            }
        }
    }
}
