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
import java.util.Calendar

enum class PauseOption(val label: String) {
    ONE_MONTH("Por 1 mês"),
    TWO_MONTHS("Por 2 meses"),
    THREE_MONTHS("Por 3 meses"),
    CUSTOM_DATE("Até uma data específica"),
    INDEFINITE("Sem data definida (indeterminado)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseRecurringDialog(
    billTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (pausedUntil: Long?) -> Unit
) {
    var selectedOption by remember { mutableStateOf(PauseOption.ONE_MONTH) }
    var customDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pausar '$billTitle'",
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
                    text = "Cobranças dentro do período serão marcadas como 'Puladas'. Parcelas vencidas anteriores continuam visíveis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                PauseOption.values().forEach { option ->
                    val isSelected = selectedOption == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    selectedOption = option
                                    if (option == PauseOption.CUSTOM_DATE && customDateMillis == null) {
                                        showDatePicker = true
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onSurface,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (option == PauseOption.CUSTOM_DATE && customDateMillis != null) {
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
            }
        },
        confirmButton = {
            PlatformPrimaryButton(
                text = "Confirmar Pausa",
                onClick = {
                    val now = System.currentTimeMillis()
                    val targetTimestamp: Long? = when (selectedOption) {
                        PauseOption.ONE_MONTH -> DateUtils.addMonths(now, 1)
                        PauseOption.TWO_MONTHS -> DateUtils.addMonths(now, 2)
                        PauseOption.THREE_MONTHS -> DateUtils.addMonths(now, 3)
                        PauseOption.CUSTOM_DATE -> customDateMillis ?: DateUtils.addMonths(now, 1)
                        PauseOption.INDEFINITE -> null
                    }
                    onConfirm(targetTimestamp)
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
