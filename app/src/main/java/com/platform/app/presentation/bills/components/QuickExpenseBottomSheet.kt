package com.platform.app.presentation.bills.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.components.PlatformPrimaryButton
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.PlatformShapes
import java.util.Calendar

private enum class DueDateOption {
    TODAY,
    TOMORROW,
    DAY_5,
    OTHER
}

/**
 * Bottom sheet de Lançamento Rápido acionada pelo FAB (Prompt 60):
 * - Raio de curvatura 28dp (PlatformShapes.bottomSheet).
 * - Valor em destaque com 52sp e cursor.
 * - Chips neutros de Item de Despesa.
 * - Chips neutros de Vencimento: Hoje, Amanhã, Dia 5, Outro.
 * - Toggle "Já paga".
 * - Botão Salvar fixo com explicação do motivo quando desabilitado.
 * - Link "Mais detalhes" direcionando para a Nova Despesa completa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseBottomSheet(
    expenseItems: List<ExpenseItem>,
    onDismiss: () -> Unit,
    onConfirm: (amountCents: Long, itemId: String, isPaid: Boolean, dueDate: Long) -> Unit,
    onMoreDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rawDigits by remember { mutableStateOf("") }
    val amountCents = remember(rawDigits) { rawDigits.toLongOrNull() ?: 0L }
    var selectedItemId by remember { mutableStateOf(expenseItems.firstOrNull()?.id) }
    var isPaid by remember { mutableStateOf(false) }

    var selectedDueOption by remember { mutableStateOf(DueDateOption.TODAY) }
    var customDueDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                customDueDate = cal.timeInMillis
                selectedDueOption = DueDateOption.OTHER
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Cálculo da data de vencimento baseada na opção selecionada
    val computedDueDate = remember(selectedDueOption, customDueDate) {
        when (selectedDueOption) {
            DueDateOption.TODAY -> DateUtils.getStartOfDay(System.currentTimeMillis())
            DueDateOption.TOMORROW -> DateUtils.addDays(DateUtils.getStartOfDay(System.currentTimeMillis()), 1)
            DueDateOption.DAY_5 -> {
                val cal = Calendar.getInstance()
                if (cal.get(Calendar.DAY_OF_MONTH) > 5) {
                    cal.add(Calendar.MONTH, 1)
                }
                cal.set(Calendar.DAY_OF_MONTH, 5)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            DueDateOption.OTHER -> customDueDate
        }
    }

    val isValid = amountCents > 0L && selectedItemId != null
    val disableReason = when {
        amountCents == 0L -> "Informe o valor para salvar"
        selectedItemId == null -> "Selecione o item para salvar"
        else -> "Salvar"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = PlatformShapes.bottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "Lançamento Rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1. VALOR EM 52sp COM CURSOR
            val formattedCurrency = CurrencyUtils.formatCentsToCurrency(amountCents)
            Column {
                Text(
                    text = "VALOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                BasicTextField(
                    value = formattedCurrency,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        if (digits.length <= 11) {
                            rawDigits = digits
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFeatureSettings = "tnum"
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            innerTextField()
                        }
                    }
                )
            }

            // 2. CHIPS DE ITEM DE DESPESA
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlatformSectionTitle(text = "ITEM")
                if (expenseItems.isEmpty()) {
                    Text(
                        text = "Nenhum item cadastrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(expenseItems, key = { it.id }) { item ->
                            val isSelected = selectedItemId == item.id
                            PlatformNeutralChip(
                                text = item.name,
                                selected = isSelected,
                                onClick = { selectedItemId = item.id },
                                leadingIcon = {
                                    Icon(
                                        imageVector = PlatformIconCatalog.getIcon(item.categoryIconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 3. CHIPS DE VENCIMENTO (Hoje, Amanhã, Dia 5, Outro)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlatformSectionTitle(text = "VENCIMENTO")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformNeutralChip(
                        text = "Hoje",
                        selected = selectedDueOption == DueDateOption.TODAY,
                        onClick = { selectedDueOption = DueDateOption.TODAY }
                    )
                    PlatformNeutralChip(
                        text = "Amanhã",
                        selected = selectedDueOption == DueDateOption.TOMORROW,
                        onClick = { selectedDueOption = DueDateOption.TOMORROW }
                    )
                    PlatformNeutralChip(
                        text = "Dia 5",
                        selected = selectedDueOption == DueDateOption.DAY_5,
                        onClick = { selectedDueOption = DueDateOption.DAY_5 }
                    )
                    val otherLabel = if (selectedDueOption == DueDateOption.OTHER) {
                        DateUtils.formatDayMonth(customDueDate)
                    } else {
                        "Outro"
                    }
                    PlatformNeutralChip(
                        text = otherLabel,
                        selected = selectedDueOption == DueDateOption.OTHER,
                        onClick = { datePickerDialog.show() }
                    )
                }
            }

            // 4. TOGGLE: JÁ PAGA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPaid = !isPaid },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Já paga",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isPaid,
                    onCheckedChange = { isPaid = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. AÇÕES FIXAS NO RODAPÉ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onDismiss()
                        onMoreDetails()
                    },
                    shape = PlatformShapes.small
                ) {
                    Text(
                        text = "Mais detalhes",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                PlatformPrimaryButton(
                    text = disableReason,
                    onClick = {
                        val itemId = selectedItemId
                        if (itemId != null && amountCents > 0L) {
                            onConfirm(amountCents, itemId, isPaid, computedDueDate)
                            onDismiss()
                        }
                    },
                    enabled = isValid
                )
            }
        }
    }
}
