package com.platform.app.presentation.bills.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformListRow
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.components.PlatformPrimaryButton
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusType
import com.platform.app.presentation.theme.line
import com.platform.app.presentation.theme.surface1
import com.platform.app.presentation.theme.surface2
import com.platform.app.presentation.theme.text2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringDetailBottomSheet(
    bill: Bill,
    installments: List<BillInstallment>,
    onDismiss: () -> Unit,
    onPayNextInstallment: (BillInstallment) -> Unit,
    onPauseRecurring: (pausedUntil: Long?) -> Unit,
    onResumeRecurring: () -> Unit,
    onStopRecurring: (cutoffDueDate: Long?) -> Unit,
    onReactivateRecurring: () -> Unit,
    onUpdateMonthlyAmount: (fromDueDate: Long, newAmountCents: Long) -> Unit,
    onDeleteRecurring: (keepPaidHistory: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showPauseDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditAmountDialog by remember { mutableStateOf(false) }

    val isEnded = bill.endedAt != null || bill.recurrenceEndType == RecurrenceEndType.UNTIL_DATE
    val isPaused = bill.isPaused

    val now = System.currentTimeMillis()
    val nextPendingInstallment = remember(installments) {
        installments
            .filter { !it.isPaid && !it.isSkipped }
            .minByOrNull { it.dueDate }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = PlatformShapes.bottomSheet,
        containerColor = MaterialTheme.colorScheme.surface1,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Ícone 36dp neutro, Título e Chip de status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bill.recurrenceFrequency?.label ?: "Mensal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.text2
                    )
                }

                when {
                    isPaused -> {
                        val pausedText = if (bill.pausedUntil != null) {
                            "Pausada até ${DateUtils.formatDate(bill.pausedUntil)}"
                        } else {
                            "Pausada"
                        }
                        PlatformNeutralChip(text = pausedText)
                    }
                    isEnded -> {
                        PlatformNeutralChip(text = "Encerrada")
                    }
                    else -> {
                        PlatformNeutralChip(text = "Ativa")
                    }
                }
            }

            // Card Hero do Valor Mensal e Próximo Pagamento
            PlatformCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Valor por ciclo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.text2
                            )
                            PlatformMoneyText(
                                amountCents = bill.totalAmountCents,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        if (nextPendingInstallment != null && !isPaused && !isEnded) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Próximo vencimento",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.text2
                                )
                                Text(
                                    text = DateUtils.formatDate(nextPendingInstallment.dueDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (nextPendingInstallment != null && !isPaused && !isEnded) {
                        PlatformPrimaryButton(
                            text = "Pagar próxima cobrança",
                            onClick = { onPayNextInstallment(nextPendingInstallment) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Ações de Ciclo de Vida da Recorrência
            PlatformSectionTitle(text = "Ações da Recorrência")

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isPaused -> {
                        PlatformPrimaryButton(
                            text = "Retomar recorrência",
                            onClick = onResumeRecurring,
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = { showStopDialog = true },
                            shape = PlatformShapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppStrings.RecurringActions.END_RECURRING, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    isEnded -> {
                        PlatformPrimaryButton(
                            text = AppStrings.RecurringActions.REACTIVATE_RECURRING,
                            onClick = onReactivateRecurring,
                            leadingIcon = { Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        // Ativa: Pausar ou Encerrar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showPauseDialog = true },
                                shape = PlatformShapes.medium,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.RecurringActions.PAUSE, color = MaterialTheme.colorScheme.onSurface)
                            }
                            OutlinedButton(
                                onClick = { showStopDialog = true },
                                shape = PlatformShapes.medium,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.RecurringActions.END, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Alterar valor futuro
                OutlinedButton(
                    onClick = { showEditAmountDialog = true },
                    shape = PlatformShapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppStrings.RecurringActions.EDIT_AMOUNT_FROM, color = MaterialTheme.colorScheme.onSurface)
                }

                // Excluir
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Excluir lançamento...",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Histórico e Próximas Cobranças
            if (installments.isNotEmpty()) {
                PlatformSectionTitle(text = "Cobranças (${installments.size})")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sortedInstallments = remember(installments) {
                        installments.sortedByDescending { it.dueDate }
                    }
                    val now = remember { System.currentTimeMillis() }
                    sortedInstallments.take(12).forEach { inst ->
                        val isOverdue = !inst.isPaid && !inst.isSkipped && inst.dueDateMillis < now
                        val statusText = when {
                            inst.isPaid -> "Paga"
                            inst.isSkipped -> "Pulada"
                            isOverdue -> "Atrasada"
                            else -> "Pendente"
                        }
                        val statusType = when {
                            inst.isPaid -> PlatformStatusType.PAID
                            isOverdue -> PlatformStatusType.OVERDUE
                            else -> PlatformStatusType.ATTENTION
                        }
                        PlatformListRow(
                            title = "Cobrança de ${DateUtils.formatDate(inst.dueDate)}",
                            subtitle = statusText,
                            amountCents = inst.amountCents,
                            icon = if (inst.isPaid) Icons.Default.Check else Icons.Default.Autorenew,
                            statusText = statusText,
                            statusType = if (inst.isSkipped) null else statusType,
                            onClick = {}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showPauseDialog) {
        PauseRecurringDialog(
            billTitle = bill.title,
            onDismiss = { showPauseDialog = false },
            onConfirm = { pausedUntil ->
                showPauseDialog = false
                onPauseRecurring(pausedUntil)
            }
        )
    }

    if (showStopDialog) {
        StopRecurringDialog(
            billTitle = bill.title,
            nextDueDate = nextPendingInstallment?.dueDateMillis,
            onDismiss = { showStopDialog = false },
            onConfirm = { cutoff ->
                showStopDialog = false
                onStopRecurring(cutoff)
            }
        )
    }

    if (showDeleteDialog) {
        DeleteRecurringDialog(
            billTitle = bill.title,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { keepPaid ->
                showDeleteDialog = false
                onDeleteRecurring(keepPaid)
                onDismiss()
            }
        )
    }

    if (showEditAmountDialog) {
        EditRecurringAmountDialog(
            currentAmountCents = bill.totalAmountCents,
            billTitle = bill.title,
            fromDueDate = nextPendingInstallment?.dueDateMillis,
            onDismiss = { showEditAmountDialog = false },
            onConfirm = { fromDueDate, newAmount ->
                showEditAmountDialog = false
                onUpdateMonthlyAmount(fromDueDate, newAmount)
            }
        )
    }
}
