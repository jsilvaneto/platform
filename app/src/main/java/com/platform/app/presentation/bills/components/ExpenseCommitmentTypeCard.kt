package com.platform.app.presentation.bills.components

import com.platform.app.presentation.theme.PlatformShapes

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency
import com.platform.app.presentation.bills.NewExpenseUiState
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.theme.Dimens
import java.util.Calendar

@Composable
fun ExpenseCommitmentTypeCard(
    uiState: NewExpenseUiState,
    onExpenseTypeChange: (BillType) -> Unit,
    onInstallmentsCountChange: (Int) -> Unit,
    onRecurrenceFrequencyChange: (RecurrenceFrequency) -> Unit,
    onRecurrenceEndTypeChange: (RecurrenceEndType) -> Unit,
    onRecurrenceEndDateChange: (Long) -> Unit,
    onRecurrenceOccurrencesCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    PlatformCard(
        shape = PlatformShapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tipo de Compromisso",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            PlatformSegmentedTabs(
                items = listOf(
                    SegmentedTabItem("À Vista"),
                    SegmentedTabItem("Parcelado"),
                    SegmentedTabItem("Recorrente")
                ),
                selectedIndex = when (uiState.expenseType) {
                    BillType.SINGLE -> 0
                    BillType.INSTALLMENT -> 1
                    BillType.RECURRING -> 2
                },
                onTabSelected = { index ->
                    when (index) {
                        0 -> onExpenseTypeChange(BillType.SINGLE)
                        1 -> onExpenseTypeChange(BillType.INSTALLMENT)
                        2 -> onExpenseTypeChange(BillType.RECURRING)
                    }
                }
            )

            // Se for Parcelado: Quantidade de Parcelas
            if (uiState.expenseType == BillType.INSTALLMENT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Número de Parcelas:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onInstallmentsCountChange(uiState.installmentsCount - 1) },
                            enabled = uiState.installmentsCount > 2
                        ) {
                            Text("—", fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = PlatformShapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${uiState.installmentsCount}x",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = { onInstallmentsCountChange(uiState.installmentsCount + 1) },
                            enabled = uiState.installmentsCount < 72
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Preview do valor de cada parcela
                Text(
                    text = "Plano: ${uiState.installmentsCount}x de ${CurrencyUtils.formatCentsToCurrency(uiState.installmentPreviewAmount)} mensais",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Se for Recorrente
            if (uiState.expenseType == BillType.RECURRING) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Text(
                    text = "Frequência de Repetição",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.recurrenceFrequency == RecurrenceFrequency.DAILY,
                        onClick = { onRecurrenceFrequencyChange(RecurrenceFrequency.DAILY) },
                        label = { Text("Diariamente") },
                        leadingIcon = if (uiState.recurrenceFrequency == RecurrenceFrequency.DAILY) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.recurrenceFrequency == RecurrenceFrequency.WEEKLY,
                        onClick = { onRecurrenceFrequencyChange(RecurrenceFrequency.WEEKLY) },
                        label = { Text("Semanalmente") },
                        leadingIcon = if (uiState.recurrenceFrequency == RecurrenceFrequency.WEEKLY) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.recurrenceFrequency == RecurrenceFrequency.MONTHLY,
                        onClick = { onRecurrenceFrequencyChange(RecurrenceFrequency.MONTHLY) },
                        label = { Text("Mensalmente") },
                        leadingIcon = if (uiState.recurrenceFrequency == RecurrenceFrequency.MONTHLY) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.recurrenceFrequency == RecurrenceFrequency.YEARLY,
                        onClick = { onRecurrenceFrequencyChange(RecurrenceFrequency.YEARLY) },
                        label = { Text("Anualmente") },
                        leadingIcon = if (uiState.recurrenceFrequency == RecurrenceFrequency.YEARLY) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Término da Repetição",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.recurrenceEndType == RecurrenceEndType.FOREVER,
                        onClick = { onRecurrenceEndTypeChange(RecurrenceEndType.FOREVER) },
                        label = { Text("Para sempre") },
                        leadingIcon = if (uiState.recurrenceEndType == RecurrenceEndType.FOREVER) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.recurrenceEndType == RecurrenceEndType.UNTIL_DATE,
                        onClick = { onRecurrenceEndTypeChange(RecurrenceEndType.UNTIL_DATE) },
                        label = { Text("Até uma data") },
                        leadingIcon = if (uiState.recurrenceEndType == RecurrenceEndType.UNTIL_DATE) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                FilterChip(
                    selected = uiState.recurrenceEndType == RecurrenceEndType.BY_OCCURRENCES,
                    onClick = { onRecurrenceEndTypeChange(RecurrenceEndType.BY_OCCURRENCES) },
                    label = { Text("Por número de eventos") },
                    leadingIcon = if (uiState.recurrenceEndType == RecurrenceEndType.BY_OCCURRENCES) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                when (uiState.recurrenceEndType) {
                    RecurrenceEndType.FOREVER -> {
                        Surface(
                            shape = PlatformShapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Compromisso contínuo sem data final definida.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                    RecurrenceEndType.UNTIL_DATE -> {
                        val endDateFormatted = DateUtils.formatDate(uiState.recurrenceEndDate)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Repetir até:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = endDateFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { timeInMillis = uiState.recurrenceEndDate }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val selectedCal = Calendar.getInstance().apply {
                                                set(year, month, day, 23, 59, 59)
                                            }
                                            onRecurrenceEndDateChange(selectedCal.timeInMillis)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = PlatformShapes.medium
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Alterar Data Final")
                            }
                        }
                    }
                    RecurrenceEndType.BY_OCCURRENCES -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Número de eventos:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onRecurrenceOccurrencesCountChange(uiState.recurrenceOccurrencesCount - 1) },
                                    enabled = uiState.recurrenceOccurrencesCount > 2
                                ) {
                                    Text("—", fontWeight = FontWeight.Bold)
                                }

                                Surface(
                                    shape = PlatformShapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "${uiState.recurrenceOccurrencesCount}x",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onRecurrenceOccurrencesCountChange(uiState.recurrenceOccurrencesCount + 1) },
                                    enabled = uiState.recurrenceOccurrencesCount < 365
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Preview do resumo
                val freqText = when (uiState.recurrenceFrequency) {
                    RecurrenceFrequency.DAILY -> "diariamente"
                    RecurrenceFrequency.WEEKLY -> "semanalmente"
                    RecurrenceFrequency.MONTHLY -> "mensalmente"
                    RecurrenceFrequency.YEARLY -> "anualmente"
                }
                val endText = when (uiState.recurrenceEndType) {
                    RecurrenceEndType.FOREVER -> "para sempre"
                    RecurrenceEndType.UNTIL_DATE -> "até ${DateUtils.formatDate(uiState.recurrenceEndDate)}"
                    RecurrenceEndType.BY_OCCURRENCES -> "por ${uiState.recurrenceOccurrencesCount} eventos"
                }
                Text(
                    text = "Plano: Repete $freqText, $endText (${CurrencyUtils.formatCentsToCurrency(uiState.amountCents)} cada)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
