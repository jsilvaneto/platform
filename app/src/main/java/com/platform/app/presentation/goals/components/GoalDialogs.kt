package com.platform.app.presentation.goals.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.FinancialGoal
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.PlatformColorPicker
import java.util.UUID


@Composable
fun AddEditGoalDialog(
    goal: Goal?,
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetText by remember { mutableStateOf(if (goal != null) (goal.targetAmountCents / 100).toString() else "") }
    var currentText by remember { mutableStateOf(if (goal != null) (goal.currentAmountCents / 100).toString() else "0") }
    var selectedColor by remember { mutableStateOf(goal?.colorHex ?: "#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "Nova Meta de Poupança" else "Editar Meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta (ex: Reserva, Viagem, Carro)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { char -> char.isDigit() } },
                    label = { Text("Valor Alvo Desejado (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it.filter { char -> char.isDigit() } },
                    label = { Text("Valor Já Poupado Inicial (R$)") },
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
                    val targetCents = (targetText.toLongOrNull() ?: 0L) * 100L
                    val currentCents = (currentText.toLongOrNull() ?: 0L) * 100L
                    if (name.isNotBlank() && targetCents > 0L) {
                        onConfirm(
                            Goal(
                                id = goal?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                targetAmountCents = targetCents,
                                currentAmountCents = currentCents,
                                colorHex = selectedColor
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && (targetText.toLongOrNull() ?: 0L) > 0L,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (goal == null) "Salvar Meta" else "Atualizar Meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}

@Composable
fun AddContributionDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aporte em '${goal.name}'") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Adicione o valor que você guardou para esta meta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Valor do Aporte (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cents = (amountText.toLongOrNull() ?: 0L) * 100L
                    if (cents > 0L) {
                        onConfirm(cents)
                    }
                },
                enabled = (amountText.toLongOrNull() ?: 0L) > 0L,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirmar Aporte")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}
