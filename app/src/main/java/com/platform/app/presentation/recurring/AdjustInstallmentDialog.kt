package com.platform.app.presentation.recurring

import com.platform.app.presentation.theme.PlatformShapes

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.platform.app.presentation.common.AppStrings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import java.util.Calendar

@Composable
fun AdjustInstallmentDialog(
    installment: BillInstallment,
    onDismiss: () -> Unit,
    onSave: (installmentId: String, newAmountCents: Long, newDueDate: Long, applyToFuturePending: Boolean) -> Unit,
    onDeleteSingle: ((installmentId: String) -> Unit)? = null,
    onDeleteFuture: ((billId: String, fromDueDate: Long) -> Unit)? = null
) {
    val context = LocalContext.current
    var amountCents by remember { mutableLongStateOf(installment.amountCents) }
    var dueDate by remember { mutableLongStateOf(installment.dueDate) }
    var applyToFuturePending by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteOptionSelected by remember { mutableStateOf<DeleteScope?>(null) }

    val isRecurring = installment.type == BillType.RECURRING

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isRecurring) "Ajustar Cobrança Recorrente" else "Ajustar Parcela",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${installment.billTitle} (Cobrança ${installment.installmentNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo de Valor da Parcela
                val displayCurrency = CurrencyUtils.formatCentsToCurrency(amountCents)
                OutlinedTextField(
                    value = displayCurrency,
                    onValueChange = { newValue ->
                        val cleanDigits = newValue.filter { it.isDigit() }
                        amountCents = cleanDigits.toLongOrNull() ?: 0L
                    },
                    label = { Text("Novo Valor *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = PlatformShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo de Vencimento
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            PlatformShapes.medium
                        )
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selectedCal = Calendar.getInstance().apply {
                                        set(year, month, day, 12, 0, 0)
                                    }
                                    dueDate = selectedCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vencimento Desta Cobrança",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.formatDate(dueDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Selecionar data",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Seletor de Escopo do Ajuste (Apenas esta vs Esta e Próximas)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            PlatformShapes.medium
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Aplicar novo valor para:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { applyToFuturePending = false }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !applyToFuturePending,
                            onClick = { applyToFuturePending = false }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Apenas esta parcela",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "As parcelas seguintes continuam com o valor anterior",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { applyToFuturePending = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = applyToFuturePending,
                            onClick = { applyToFuturePending = true }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Esta e todas as próximas pendentes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Atualiza a mensalidade futura sem alterar o histórico já pago",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Ações de Exclusão Granular
                if (onDeleteSingle != null || onDeleteFuture != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exclusão / Cancelamento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir...", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountCents > 0L) {
                        onSave(installment.id, amountCents, dueDate, applyToFuturePending)
                    }
                },
                enabled = amountCents > 0L,
                shape = PlatformShapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (applyToFuturePending) "Atualizar Próximas" else "Salvar Ajuste")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = PlatformShapes.medium
            ) {
                Text(AppStrings.Actions.CANCEL)
            }
        }
    )

    // Diálogo de Seleção de Exclusão
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                deleteOptionSelected = null
            },
            title = {
                Text(
                    text = "Opções de Exclusão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Escolha como deseja excluir esta cobrança:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = PlatformShapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (deleteOptionSelected == DeleteScope.THIS_ONLY)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deleteOptionSelected = DeleteScope.THIS_ONLY }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "1. Excluir apenas esta parcela",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Não haverá cobrança em ${DateUtils.formatDate(installment.dueDate)}. As cobranças dos próximos meses continuarão ativas normalmente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = PlatformShapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (deleteOptionSelected == DeleteScope.THIS_AND_FUTURE)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deleteOptionSelected = DeleteScope.THIS_AND_FUTURE }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "2. Excluir esta e todas as próximas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Interrompe e encerra o compromisso a partir de ${DateUtils.formatDate(installment.dueDate)}. Todas as parcelas anteriores já pagas serão mantidas salvas no seu histórico!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val option = deleteOptionSelected
                        showDeleteConfirmDialog = false
                        deleteOptionSelected = null
                        when (option) {
                            DeleteScope.THIS_ONLY -> onDeleteSingle?.invoke(installment.id)
                            DeleteScope.THIS_AND_FUTURE -> onDeleteFuture?.invoke(installment.billId, installment.dueDate)
                            null -> Unit
                        }
                    },
                    enabled = deleteOptionSelected != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (deleteOptionSelected == DeleteScope.THIS_AND_FUTURE)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Confirmar Exclusão")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        deleteOptionSelected = null
                    }
                ) {
                    Text(AppStrings.Actions.BACK)
                }
            }
        )
    }
}

private enum class DeleteScope {
    THIS_ONLY,
    THIS_AND_FUTURE
}
