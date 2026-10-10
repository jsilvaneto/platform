package com.platform.app.presentation.contacts.components

import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.PlatformSurfaceVariant

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber


@Composable
fun LinkedBillsTab(bills: List<Bill>) {
    if (bills.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum registro de conta vinculado a este contato.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bills, key = { it.id }) { bill ->
                PlatformSurface(
                    variant = PlatformSurfaceVariant.Tonal,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (bill.description.isNotBlank()) {
                                Text(
                                    text = bill.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${bill.type.label} • Cadastrado em ${DateUtils.formatDate(bill.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyUtils.formatCentsToCurrency(bill.totalAmountCents),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (bill.type == BillType.INSTALLMENT) {
                                Text(
                                    text = "${bill.totalInstallments} parcelas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlannedInstallmentsTab(
    installments: List<BillInstallment>,
    onTogglePayment: (BillInstallment) -> Unit
) {
    if (installments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Não há pagamentos futuros ou pendentes planejados para este contato.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        val now = System.currentTimeMillis()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(installments, key = { it.id }) { inst ->
                val isToday = !inst.isPaid && DateUtils.isToday(inst.dueDate)
                val isOverdue = !inst.isPaid && inst.dueDate < now && !isToday

                PlatformSurface(
                    variant = PlatformSurfaceVariant.Tonal,
                    shape = PlatformShapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onTogglePayment(inst) }) {
                            Icon(
                                imageVector = if (inst.isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = if (inst.isPaid) AppStrings.Status.PAID else AppStrings.Status.PENDING,
                                tint = if (inst.isPaid) SuccessGreen else if (isOverdue) MaterialTheme.colorScheme.error else if (isToday) WarningAmber else MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inst.billTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val statusLabel = when {
                                inst.isPaid -> AppStrings.Status.PAID
                                isOverdue -> "Venceu ${DateUtils.formatDate(inst.dueDate)}"
                                isToday -> "Vence hoje"
                                else -> "Vence ${DateUtils.formatDate(inst.dueDate)}"
                            }
                            Text(
                                text = "$statusLabel" +
                                        if (inst.type == BillType.INSTALLMENT) " • Parcela ${inst.installmentNumber}/${inst.totalInstallments}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    inst.isPaid -> SuccessGreen
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    isToday -> WarningAmber
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                },
                                fontWeight = if (isOverdue || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(inst.amountCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
