package com.platform.app.presentation.recurring.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformProgressBar
import com.platform.app.presentation.recurring.BillWithInstallments
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringDetailBottomSheet(
    item: BillWithInstallments,
    onDismiss: () -> Unit,
    onTogglePayment: (String, Boolean) -> Unit,
    onOpenAdjust: (BillInstallment) -> Unit = {},
    onTogglePause: ((String, Boolean) -> Unit)? = null,
    onStopRecurring: ((String) -> Unit)? = null,
    onUpdateMonthlyAmount: ((billId: String, newAmountCents: Long) -> Unit)? = null,
    onDelete: () -> Unit
) {
    val bill = item.bill
    val isInstallment = bill.type == BillType.INSTALLMENT
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showStopConfirmDialog by remember { mutableStateOf(false) }
    var showEditAmountDialog by remember { mutableStateOf(false) }

    val isAllPaid = item.paidInstallmentsCount == bill.totalInstallments && bill.totalInstallments > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Avatar, Título, Badges e 3 Pontos no Canto Superior Direito
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isInstallment)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isInstallment) Icons.Default.CreditCard else Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = if (isInstallment) MaterialTheme.colorScheme.primary else SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = PlatformShapes.extraSmall,
                            color = if (isInstallment)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isInstallment) "Parcelamento" else "Recorrente",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isInstallment) MaterialTheme.colorScheme.primary else SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (item.isPaused) {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = WarningAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Pausada",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (isInstallment) {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = if (isAllPaid) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (isAllPaid) "Totalmente Quitado" else "${item.paidInstallmentsCount}/${bill.totalInstallments} pagas",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAllPaid) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Contato em destaque
                        if (!item.contactName.isNullOrBlank()) {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.contactName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Categoria
                        if (item.categoryName.isNotBlank() && item.categoryName != "Geral") {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!isInstallment) {
                            DropdownMenuItem(
                                text = {
                                    Text("Alterar Valor Mensal", color = MaterialTheme.colorScheme.onSurface)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showEditAmountDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(if (item.isPaused) "Retomar Assinatura" else "Pausar Assinatura")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (item.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = if (item.isPaused) SuccessGreen else WarningAmber
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onTogglePause?.invoke(bill.id, item.isPaused)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Encerrar Recorrência", color = MaterialTheme.colorScheme.onSurface)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.StopCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showStopConfirmDialog = true
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text("Excluir Tudo (Inclusive Histórico)", color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // Banner se assinatura estiver pausada
            if (item.isPaused) {
                Surface(
                    shape = PlatformShapes.medium,
                    color = WarningAmber.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PauseCircle,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assinatura pausada. As cobranças futuras estão suspensas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = { onTogglePause?.invoke(bill.id, true) }
                        ) {
                            Text("Retomar", fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }

            // Hero Card: Resumo dos Valores com PlatformProgressBar
            PlatformCard(
                shape = PlatformShapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isInstallment) "Valor Total Financiado" else "Mensalidade Regular",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isInstallment)
                            CurrencyUtils.formatCentsToCurrency(bill.totalAmountCents)
                        else
                            "${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)} / mês",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isInstallment) MaterialTheme.colorScheme.onSurface else SuccessGreen
                    )

                    if (isInstallment) {
                        PlatformProgressBar(
                            progress = item.progress,
                            height = 7.dp,
                            progressColor = if (isAllPaid) SuccessGreen else MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Pago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatCentsToCurrency(item.totalPaidCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Restante",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyUtils.formatCentsToCurrency(item.remainingCents),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAllPaid) SuccessGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (item.estimatedPayoffDate != null && !isAllPaid) {
                            Text(
                                text = "Previsão de quitação: ${DateUtils.formatMonthYear(item.estimatedPayoffDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Seção explicativa para assinaturas / recorrências
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Projeção Anual: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents * 12)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            item.nextInstallment?.let { next ->
                                Text(
                                    text = "Próx: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)} em ${DateUtils.formatDate(next.dueDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (item.hasVariableFirstInstallment) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1ª parcela diferenciada (Entrada/Adesão): ${CurrencyUtils.formatCentsToCurrency(item.firstInstallmentAmountCents)}. Demais cobranças: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)}/mês.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Cronograma de Parcelas / Cobranças
            if (item.installments.isNotEmpty()) {
                Text(
                    text = if (isInstallment)
                        "Cronograma de Parcelas (${item.installments.size})"
                    else
                        "Histórico e Próximas Cobranças (${item.installments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.installments.forEach { inst ->
                        InstallmentRow(
                            installment = inst,
                            isVariableFirst = item.hasVariableFirstInstallment,
                            onTogglePayment = { onTogglePayment(inst.id, inst.isPaid) },
                            onOpenAdjust = { onOpenAdjust(inst) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Excluir Lançamento") },
            text = { Text("Deseja realmente excluir '${bill.title}' e todas as suas parcelas?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text(AppStrings.Actions.DELETE, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(AppStrings.Actions.CANCEL)
                }
            }
        )
    }

    if (showStopConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showStopConfirmDialog = false },
            title = { Text("Encerrar Recorrência?") },
            text = {
                Text(
                    "Todas as cobranças futuras pendentes serão canceladas. As parcelas já pagas continuarão salvas com total integridade no seu histórico financeiro."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopConfirmDialog = false
                        onStopRecurring?.invoke(bill.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Encerrar Recorrência")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmDialog = false }) {
                    Text(AppStrings.Actions.CANCEL)
                }
            }
        )
    }

    if (showEditAmountDialog) {
        EditBillMonthlyAmountDialog(
            currentAmountCents = item.regularAmountCents,
            billTitle = bill.title,
            onDismiss = { showEditAmountDialog = false },
            onConfirm = { newAmountCents ->
                showEditAmountDialog = false
                onUpdateMonthlyAmount?.invoke(bill.id, newAmountCents)
            }
        )
    }
}
