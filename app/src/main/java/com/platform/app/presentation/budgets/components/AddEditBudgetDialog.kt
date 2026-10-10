package com.platform.app.presentation.budgets.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.Budget
import com.platform.app.domain.model.Category
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.PlatformColorPicker
import java.util.UUID


@Composable
fun AddEditBudgetDialog(
    budget: Budget?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId) }
    var selectedCategoryName by remember { mutableStateOf(budget?.categoryName ?: (categories.firstOrNull()?.name ?: "Geral")) }
    var limitText by remember { mutableStateOf(if (budget != null) (budget.limitAmountCents / 100).toString() else "") }
    var selectedColor by remember { mutableStateOf(budget?.colorHex ?: "#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (budget == null) "Novo Teto de Orçamento" else "Editar Orçamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecione a Categoria:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = {
                                selectedCategoryId = null
                                selectedCategoryName = "Geral (Todas as despesas)"
                            },
                            label = { Text("Geral") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = cat.id
                                selectedCategoryName = cat.name
                                selectedColor = cat.colorHex
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { char -> char.isDigit() } },
                    label = { Text("Limite Mensal Máximo (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                PlatformColorPicker(
                    selectedColorHex = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitCents = (limitText.toLongOrNull() ?: 0L) * 100L
                    if (limitCents > 0L) {
                        onConfirm(
                            Budget(
                                id = budget?.id ?: UUID.randomUUID().toString(),
                                categoryId = selectedCategoryId,
                                categoryName = selectedCategoryName,
                                limitAmountCents = limitCents,
                                colorHex = selectedColor
                            )
                        )
                    }
                },
                enabled = (limitText.toLongOrNull() ?: 0L) > 0L,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (budget == null) "Salvar Teto" else "Atualizar Teto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}

