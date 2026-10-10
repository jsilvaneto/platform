package com.platform.app.presentation.bills.components

import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.platform.app.presentation.bills.NewExpenseUiState
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemAndAmountCard(
    uiState: NewExpenseUiState,
    onItemSelect: (String) -> Unit,
    onAmountChange: (Long) -> Unit,
    onNavigateToExpenseItems: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var itemDropdownExpanded by remember { mutableStateOf(false) }

    PlatformCard(
        shape = PlatformShapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "1. Valor & Item *",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // VALOR DA DESPESA (PROTAGONISTA)
            val displayCurrency = CurrencyUtils.formatCentsToCurrency(uiState.amountCents)
            OutlinedTextField(
                value = displayCurrency,
                onValueChange = { newValue ->
                    val cleanDigits = newValue.filter { it.isDigit() }
                    val cents = cleanDigits.toLongOrNull() ?: 0L
                    onAmountChange(cents)
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

            // SELETOR DE ITEM DE DESPESA (OBRIGATÓRIO)
            ExposedDropdownMenuBox(
                expanded = itemDropdownExpanded,
                onExpandedChange = { itemDropdownExpanded = !itemDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.selectedItem?.let { "${it.name} (${it.categoryName})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(AppStrings.ExpenseForm.EXPENSE_ITEM_REQUIRED) },
                    placeholder = { Text("Selecione o item categorizado") },
                    leadingIcon = {
                        val itemIcon = uiState.selectedItem?.let { PlatformIconCatalog.getIcon(it.categoryIconName) } ?: Icons.Default.Payments
                        val itemColor = uiState.selectedItem?.let {
                            try { Color(android.graphics.Color.parseColor(it.categoryColorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary
                        Icon(
                            imageVector = itemIcon,
                            contentDescription = null,
                            tint = itemColor
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded) },
                    isError = uiState.selectedItemId == null && uiState.amountCents > 0L,
                    supportingText = if (uiState.selectedItemId == null && uiState.amountCents > 0L) {
                        { Text("Item é obrigatório para classificar a despesa", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = PlatformShapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = itemDropdownExpanded,
                    onDismissRequest = { itemDropdownExpanded = false }
                ) {
                    if (uiState.allExpenseItems.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Nenhum item cadastrado") },
                            onClick = { itemDropdownExpanded = false }
                        )
                    }
                    for (item in uiState.allExpenseItems) {
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
                            text = {
                                Column {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${item.categoryName} • ${item.nature.displayName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onItemSelect(item.id)
                                itemDropdownExpanded = false
                            }
                        )
                    }
                    if (onNavigateToExpenseItems != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gerenciar Itens (Criar / Editar)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                itemDropdownExpanded = false
                                onNavigateToExpenseItems()
                            }
                        )
                    }
                }
            }

            // Natureza e Categoria
            if (uiState.selectedItem != null) {
                Surface(
                    shape = PlatformShapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Natureza:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.inheritedNature.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Categoria: ${uiState.selectedCategory?.name ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (onNavigateToExpenseItems != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onNavigateToExpenseItems,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Item",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
