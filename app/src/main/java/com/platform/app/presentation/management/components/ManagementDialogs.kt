package com.platform.app.presentation.management.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.FinancialAccountType
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.PlatformColorPicker
import com.platform.app.presentation.theme.PlatformIconPicker
import java.util.UUID

@Composable
fun AddEditAccountDialog(
    account: FinancialAccount?,
    onDismiss: () -> Unit,
    onConfirm: (FinancialAccount) -> Unit
) {
    var name by remember(account) { mutableStateOf(account?.name ?: "") }
    var selectedType by remember(account) {
        mutableStateOf(account?.accountType ?: FinancialAccountType.CORRENTE)
    }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedColor by remember(account) { mutableStateOf(account?.colorHex ?: "#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (account == null) AppStrings.Dialogs.NEW_ACCOUNT_TITLE else AppStrings.Dialogs.EDIT_ACCOUNT_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.Dialogs.ACCOUNT_NAME_LABEL, style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo do Tipo Lista (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(AppStrings.Dialogs.ACCOUNT_TYPE_LABEL, style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (typeDropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Selecionar tipo de conta",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Overlay invisível para capturar o toque em qualquer ponto do campo
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { typeDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.72f)
                    ) {
                        FinancialAccountType.entries.forEach { type ->
                            val isSelected = type == selectedType
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    val icon = when (type) {
                                        FinancialAccountType.CORRENTE -> Icons.Default.AccountBalance
                                        FinancialAccountType.CARTEIRA -> Icons.Default.Payments
                                        FinancialAccountType.POUPANCA -> Icons.Default.Savings
                                        FinancialAccountType.INVESTIMENTO -> Icons.AutoMirrored.Filled.TrendingUp
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                PlatformColorPicker(
                    selectedColorHex = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            FinancialAccount(
                                id = account?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                accountType = selectedType,
                                colorHex = selectedColor
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank()
            ) {
                Text(if (account == null) AppStrings.Actions.SAVE else AppStrings.Actions.UPDATE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}

@Composable
fun AddEditPaymentMethodDialog(
    method: PaymentMethod?,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod) -> Unit
) {
    val commonMethods = listOf(
        "Pix",
        "Cartão de Crédito",
        "Cartão de Débito",
        "Boleto Bancário",
        "Dinheiro em Espécie",
        "Transferência TED/DOC",
        "Outro"
    )
    var name by remember(method) { mutableStateOf(method?.name ?: "") }
    var selectedPreset by remember(method) { mutableStateOf(if (method != null && commonMethods.contains(method.name)) method.name else if (method == null) commonMethods[0] else "Outro") }
    var isCustomName by remember(method) { mutableStateOf(method != null && !commonMethods.contains(method.name)) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedIcon by remember(method) {
        mutableStateOf(
            method?.iconName ?: when {
                method?.name?.contains("Pix", ignoreCase = true) == true -> "qr_code"
                method?.name?.contains("Boleto", ignoreCase = true) == true -> "receipt"
                method?.name?.contains("Cartão", ignoreCase = true) == true -> "credit_card"
                method?.name?.contains("Dinheiro", ignoreCase = true) == true -> "payments"
                else -> "account_balance"
            }
        )
    }

    LaunchedEffect(method) {
        if (method == null && name.isBlank()) {
            name = commonMethods[0]
            selectedIcon = "qr_code"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (method == null) AppStrings.Dialogs.NEW_PAYMENT_METHOD_TITLE else AppStrings.Dialogs.EDIT_PAYMENT_METHOD_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Campo do Tipo Lista (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (isCustomName) "Personalizado: ${name.ifBlank { "Outro" }}" else selectedPreset,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(AppStrings.Dialogs.PAYMENT_TYPE_LABEL, style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Selecionar tipo de pagamento",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.72f)
                    ) {
                        commonMethods.forEach { preset ->
                            val isSelected = (!isCustomName && preset == selectedPreset) || (preset == "Outro" && isCustomName)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                leadingIcon = {
                                    val icon = when {
                                        preset.contains("Pix", ignoreCase = true) -> Icons.Default.QrCode
                                        preset.contains("Boleto", ignoreCase = true) -> Icons.AutoMirrored.Filled.ReceiptLong
                                        preset.contains("Cartão", ignoreCase = true) -> Icons.Default.CreditCard
                                        preset.contains("Dinheiro", ignoreCase = true) -> Icons.Default.Payments
                                        else -> Icons.Default.AccountBalance
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    selectedPreset = preset
                                    dropdownExpanded = false
                                    if (preset == "Outro") {
                                        isCustomName = true
                                        if (name in commonMethods) name = ""
                                    } else {
                                        isCustomName = false
                                        name = preset
                                        selectedIcon = when {
                                            preset.contains("Pix", ignoreCase = true) -> "qr_code"
                                            preset.contains("Boleto", ignoreCase = true) -> "receipt"
                                            preset.contains("Cartão", ignoreCase = true) -> "credit_card"
                                            preset.contains("Dinheiro", ignoreCase = true) -> "payments"
                                            else -> "account_balance"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Se selecionou "Outro" ou quer personalizar o nome
                if (isCustomName) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppStrings.Dialogs.CUSTOM_NAME_LABEL, style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                PlatformIconPicker(
                    selectedIconKey = selectedIcon,
                    onIconSelected = { selectedIcon = it },
                    activeColorHex = "#2563EB"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = (if (isCustomName) name else selectedPreset).trim()
                    if (finalName.isNotBlank()) {
                        onConfirm(
                            PaymentMethod(
                                id = method?.id ?: UUID.randomUUID().toString(),
                                name = finalName,
                                iconName = selectedIcon
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = (!isCustomName && selectedPreset != "Outro") || (isCustomName && name.isNotBlank())
            ) {
                Text(if (method == null) AppStrings.Actions.SAVE else AppStrings.Actions.UPDATE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var name by remember(category) { mutableStateOf(category?.name ?: "") }
    var selectedColor by remember(category) { mutableStateOf(category?.colorHex ?: "#2563EB") }
    var selectedIcon by remember(category) { mutableStateOf(category?.iconName ?: "shopping_cart") }
    var selectedNature by remember(category) { mutableStateOf(category?.nature ?: ExpenseNature.NECESSARIO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (category == null) AppStrings.Dialogs.NEW_CATEGORY_TITLE else AppStrings.Dialogs.EDIT_CATEGORY_TITLE,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.Dialogs.CATEGORY_NAME_LABEL, style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(AppStrings.Nature.LABEL_NATURE_COLON, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExpenseNature.entries.toTypedArray()) { natureOption ->
                        FilterChip(
                            selected = selectedNature == natureOption,
                            onClick = { selectedNature = natureOption },
                            label = { Text(natureOption.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                PlatformColorPicker(
                    selectedColorHex = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                PlatformIconPicker(
                    selectedIconKey = selectedIcon,
                    onIconSelected = { selectedIcon = it },
                    activeColorHex = selectedColor
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            Category(
                                id = category?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                colorHex = selectedColor,
                                iconName = selectedIcon,
                                nature = selectedNature
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank()
            ) {
                Text(if (category == null) AppStrings.Actions.SAVE else AppStrings.Actions.UPDATE)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}
