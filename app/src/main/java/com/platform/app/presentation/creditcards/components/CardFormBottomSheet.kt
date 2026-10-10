package com.platform.app.presentation.creditcards.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformShapes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormBottomSheet(
    cardToEdit: CreditCard?,
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit
) {
    val isEdit = cardToEdit != null
    var name by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var limitText by remember { mutableStateOf(if (cardToEdit != null) cardToEdit.totalLimitCents.toString() else "") }
    var closingDayText by remember { mutableStateOf(if (cardToEdit != null) cardToEdit.closingDay.toString() else "25") }
    var dueDayText by remember { mutableStateOf(if (cardToEdit != null) cardToEdit.dueDay.toString() else "5") }
    var selectedColor by remember { mutableStateOf(cardToEdit?.colorHex ?: "#3B82F6") }

    val presetColors = listOf(
        "#3B82F6", "#8B5CF6", "#EC4899", "#EF4444", "#F59E0B",
        "#10B981", "#14B8A6", "#06B6D4", "#6366F1", "#1E293B"
    )

    val limitCents = limitText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val closing = closingDayText.toIntOrNull() ?: 0
    val due = dueDayText.toIntOrNull() ?: 0
    val validation = CreditCardCalculator.validateCard(name, limitCents, closing, due)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEdit) "Editar Cartão de Crédito" else "Novo Cartão de Crédito",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Nome
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Cartão *") },
                placeholder = { Text("Ex: Nubank Mastercard, Unique Visa") },
                isError = validation.nameError != null && name.isNotBlank(),
                supportingText = if (validation.nameError != null && name.isNotBlank()) {
                    { Text(validation.nameError, color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Limite Total
            OutlinedTextField(
                value = CurrencyUtils.formatCentsToCurrency(limitCents),
                onValueChange = { input ->
                    limitText = input.filter { it.isDigit() }
                },
                label = { Text("Limite Total *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dias de Corte e Vencimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
            ) {
                OutlinedTextField(
                    value = closingDayText,
                    onValueChange = { if (it.length <= 2) closingDayText = it.filter { char -> char.isDigit() } },
                    label = { Text("Dia Corte (1-31) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = validation.closingDayError != null,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { if (it.length <= 2) dueDayText = it.filter { char -> char.isDigit() } },
                    label = { Text("Dia Venc. (1-31) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = validation.dueDayError != null,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Seletor de Cores
            Text(
                text = "Cor do Cartão:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetColors) { hex ->
                    val color = try { Color(hex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .clickable { selectedColor = hex }
                            .then(
                                if (selectedColor == hex) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == hex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(AppStrings.Actions.CANCEL)
                }
                Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                Button(
                    onClick = {
                        if (validation.isValid) {
                            onSave(
                                CreditCard(
                                    id = cardToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    totalLimitCents = limitCents,
                                    closingDay = closing,
                                    dueDay = due,
                                    colorHex = selectedColor
                                )
                            )
                        }
                    },
                    enabled = validation.isValid
                ) {
                    Text(if (isEdit) "Salvar Alterações" else "Cadastrar Cartão")
                }
            }
        }
    }
}

