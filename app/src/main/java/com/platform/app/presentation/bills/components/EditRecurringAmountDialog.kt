package com.platform.app.presentation.bills.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformPrimaryButton

@Composable
fun EditRecurringAmountDialog(
    currentAmountCents: Long,
    billTitle: String,
    fromDueDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (fromDueDate: Long, newAmountCents: Long) -> Unit
) {
    var rawInput by remember { mutableStateOf((currentAmountCents / 100.0).toString().replace('.', ',')) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Alterar Valor de '$billTitle'",
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
                if (fromDueDate != null) {
                    Text(
                        text = "O novo valor será aplicado para todas as cobranças pendentes a partir de ${DateUtils.formatDate(fromDueDate)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text(AppStrings.RecurringActions.NEW_MONTHLY_AMOUNT) },
                    prefix = { Text(AppStrings.RecurringActions.CURRENCY_PREFIX) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            PlatformPrimaryButton(
                text = AppStrings.Actions.SAVE,
                onClick = {
                    val normalized = rawInput.replace(',', '.').trim()
                    val parsedCents = (normalized.toDoubleOrNull()?.times(100))?.toLong() ?: currentAmountCents
                    val targetDate = fromDueDate ?: System.currentTimeMillis()
                    onConfirm(targetDate, parsedCents)
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
}
