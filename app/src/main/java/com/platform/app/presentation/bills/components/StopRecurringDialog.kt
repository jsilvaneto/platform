package com.platform.app.presentation.bills.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.DateUtils
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopRecurringDialog(
    billTitle: String,
    nextDueDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (cutoffDueDate: Long?) -> Unit
) {
    var useNextDueDate by remember { mutableStateOf(true) }
    var customDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = nextDueDate ?: System.currentTimeMillis()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Encerrar '$billTitle'",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "As parcelas pagas e já vencidas são preservadas com total segurança. Apenas parcelas pendentes futuras após a data escolhida serão canceladas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Opção 1: A partir do próximo vencimento (padrão)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = useNextDueDate,
                            onClick = { useNextDueDate = true },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = useNextDueDate,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.onSurface,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "A partir do próximo vencimento",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (nextDueDate != null) {
                            Text(
                                text = "Próximo: ${DateUtils.formatDate(nextDueDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Opção 2: A partir de uma data específica
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = !useNextDueDate,
                            onClick = {
                                useNextDueDate = false
                                if (customDateMillis == null) showDatePicker = true
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !useNextDueDate,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.onSurface,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "A partir de outra data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!useNextDueDate && customDateMillis != null) {
                            Text(
                                text = "Data: ${DateUtils.formatDate(customDateMillis!!)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showDatePicker = true }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PlatformPrimaryButton(
                text = "Encerrar Recorrência",
                onClick = {
                    val cutoff = if (useNextDueDate) nextDueDate else (customDateMillis ?: nextDueDate)
                    onConfirm(cutoff)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.Actions.CANCEL, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            customDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(AppStrings.Actions.CONFIRM, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(AppStrings.Actions.CANCEL, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
