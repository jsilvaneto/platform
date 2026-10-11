package com.platform.app.presentation.components

import com.platform.app.presentation.theme.PlatformShapes

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.presentation.common.AppStrings
import java.util.Calendar

data class PaymentConfirmationDetails(
    val paymentDate: Long,
    val amountCents: Long,
    val interestCents: Long = 0L,
    val fineCents: Long = 0L,
    val discountCents: Long = 0L
)

@Composable
fun ConfirmPaymentDialog(
    installmentTitle: String,
    amountCents: Long,
    dueDate: Long,
    initialDate: Long = System.currentTimeMillis(),
    onConfirm: (actualPaymentDate: Long) -> Unit,
    onConfirmWithDetails: ((PaymentConfirmationDetails) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedPaymentDate by remember { mutableLongStateOf(initialDate) }
    var amountInput by remember {
        mutableStateOf(CurrencyUtils.formatCentsToCurrency(amountCents).replace("R$", "").trim())
    }
    var showAdjustments by remember { mutableStateOf(false) }
    var interestInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }

    val cal = Calendar.getInstance().apply { timeInMillis = selectedPaymentDate }
    val isDueToday = remember(dueDate) { DateUtils.isToday(dueDate) }
    val isSelectedToday = remember(selectedPaymentDate) { DateUtils.isToday(selectedPaymentDate) }
    val isSelectedDueDate = remember(selectedPaymentDate, dueDate) {
        val calSelected = Calendar.getInstance().apply { timeInMillis = selectedPaymentDate }
        val calDue = Calendar.getInstance().apply { timeInMillis = dueDate }
        calSelected.get(Calendar.YEAR) == calDue.get(Calendar.YEAR) &&
                calSelected.get(Calendar.DAY_OF_YEAR) == calDue.get(Calendar.DAY_OF_YEAR)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = PlatformShapes.large,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = AppStrings.Dialogs.CONFIRM_PAYMENT_TITLE,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = AppStrings.Dialogs.CONFIRM_PAYMENT_SUBTITLE,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card com Informações da Conta
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PlatformShapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = installmentTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(amountCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vence: ${DateUtils.formatDate(dueDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Valor pago
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text(AppStrings.Dialogs.PAID_AMOUNT_LABEL) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = PlatformShapes.medium
                )

                // Atalhos Rápidos de Data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isSelectedToday,
                        onClick = { selectedPaymentDate = System.currentTimeMillis() },
                        label = { Text(AppStrings.Dialogs.TODAY, style = MaterialTheme.typography.labelMedium) },
                        shape = PlatformShapes.small,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    if (!isDueToday) {
                        FilterChip(
                            selected = isSelectedDueDate && !isSelectedToday,
                            onClick = { selectedPaymentDate = dueDate },
                            label = { Text(AppStrings.Dialogs.ON_DUE_DATE, style = MaterialTheme.typography.labelMedium) },
                            shape = PlatformShapes.small,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Seletor de Data Editável
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            PlatformShapes.medium
                        )
                        .clip(PlatformShapes.medium)
                        .clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selectedCal = Calendar.getInstance().apply {
                                        set(year, month, day, 12, 0, 0)
                                    }
                                    selectedPaymentDate = selectedCal.timeInMillis
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = AppStrings.Dialogs.ACTUAL_PAYMENT_DATE,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = DateUtils.formatDate(selectedPaymentDate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Editar data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Acréscimos e Descontos Opcionais
                TextButton(
                    onClick = { showAdjustments = !showAdjustments }
                ) {
                    Text(
                        text = if (showAdjustments) AppStrings.Dialogs.HIDE_ADJUSTMENTS_LABEL else AppStrings.Dialogs.SHOW_ADJUSTMENTS_LABEL,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (showAdjustments) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = interestInput,
                            onValueChange = { interestInput = it },
                            label = { Text(AppStrings.Dialogs.INTEREST_FINE_LABEL) },
                            placeholder = { Text(AppStrings.Dialogs.CENTS_ZERO_PLACEHOLDER) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = PlatformShapes.medium
                        )
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text(AppStrings.Dialogs.DISCOUNT_LABEL) },
                            placeholder = { Text(AppStrings.Dialogs.CENTS_ZERO_PLACEHOLDER) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = PlatformShapes.medium
                        )
                    }
                }

                Text(
                    text = AppStrings.Dialogs.ACTUAL_DATE_METRICS_INFO,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onConfirmWithDetails != null) {
                        val parsedAmount = CurrencyUtils.parseInputToCents(amountInput).takeIf { it > 0L } ?: amountCents
                        val parsedInterest = CurrencyUtils.parseInputToCents(interestInput)
                        val parsedDiscount = CurrencyUtils.parseInputToCents(discountInput)
                        onConfirmWithDetails(
                            PaymentConfirmationDetails(
                                paymentDate = selectedPaymentDate,
                                amountCents = parsedAmount,
                                interestCents = parsedInterest,
                                fineCents = 0L,
                                discountCents = parsedDiscount
                            )
                        )
                    } else {
                        onConfirm(selectedPaymentDate)
                    }
                },
                shape = PlatformShapes.medium
            ) {
                Text(AppStrings.Actions.CONFIRM)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = PlatformShapes.medium
            ) {
                Text(AppStrings.Actions.CANCEL)
            }
        }
    )
}
