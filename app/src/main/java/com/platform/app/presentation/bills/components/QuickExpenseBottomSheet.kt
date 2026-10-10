package com.platform.app.presentation.bills.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.PlatformShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseBottomSheet(
    expenseItems: List<ExpenseItem>,
    onDismiss: () -> Unit,
    onConfirm: (amountCents: Long, itemId: String, isPaid: Boolean) -> Unit,
    onMoreDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountCents by remember { mutableLongStateOf(0L) }
    var selectedItemId by remember { mutableStateOf(expenseItems.firstOrNull()?.id) }
    var isPaid by remember { mutableStateOf(false) }
    var itemDropdownExpanded by remember { mutableStateOf(false) }

    val selectedItem = expenseItems.find { it.id == selectedItemId }
    val isValid = amountCents > 0L && selectedItemId != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = PlatformShapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingSmall)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Lançamento Rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // VALOR GRANDE COM TECLADO NUMÉRICO
            val displayCurrency = CurrencyUtils.formatCentsToCurrency(amountCents)
            OutlinedTextField(
                value = displayCurrency,
                onValueChange = { newValue ->
                    val cleanDigits = newValue.filter { it.isDigit() }
                    amountCents = cleanDigits.toLongOrNull() ?: 0L
                },
                label = { Text(AppStrings.ExpenseForm.AMOUNT_REQUIRED) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
                modifier = Modifier.fillMaxWidth()
            )

            // SELETOR DE ITEM DE DESPESA
            ExposedDropdownMenuBox(
                expanded = itemDropdownExpanded,
                onExpandedChange = { itemDropdownExpanded = !itemDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedItem?.let { "${it.name} (${it.categoryName})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(AppStrings.ExpenseForm.EXPENSE_ITEM_REQUIRED) },
                    placeholder = { Text(AppStrings.ExpenseForm.SELECT_ITEM_PLACEHOLDER) },
                    leadingIcon = {
                        val itemIcon = selectedItem?.let { PlatformIconCatalog.getIcon(it.categoryIconName) } ?: Icons.Default.Payments
                        val itemColor = selectedItem?.let {
                            try { Color(android.graphics.Color.parseColor(it.categoryColorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary
                        Icon(
                            imageVector = itemIcon,
                            contentDescription = null,
                            tint = itemColor
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded) },
                    shape = PlatformShapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = itemDropdownExpanded,
                    onDismissRequest = { itemDropdownExpanded = false }
                ) {
                    for (item in expenseItems) {
                        val itemColor = try { Color(android.graphics.Color.parseColor(item.categoryColorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = PlatformIconCatalog.getIcon(item.categoryIconName),
                                    contentDescription = null,
                                    tint = itemColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = { Text(item.name, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                selectedItemId = item.id
                                itemDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // TOGGLE: JÁ PAGA?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Já paga",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isPaid,
                    onCheckedChange = { isPaid = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // AÇÕES: MAIS DETALHES & SALVAR
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
                    shape = PlatformShapes.medium
                ) {
                    Text(
                        text = "Mais detalhes",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = {
                        val itemId = selectedItemId
                        if (itemId != null && amountCents > 0L) {
                            onConfirm(amountCents, itemId, isPaid)
                            onDismiss()
                        }
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = PlatformShapes.medium
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Salvar",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
