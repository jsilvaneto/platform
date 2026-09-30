package com.platform.app.presentation.bills

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.presentation.theme.SuccessGreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInstallmentBottomSheet(
    installment: BillInstallment,
    categories: List<Category>,
    contacts: List<Contact>,
    financialAccounts: List<FinancialAccount>,
    paymentMethods: List<PaymentMethod>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        amountCents: Long,
        dueDate: Long,
        categoryId: String?,
        itemId: String?,
        contactId: String?,
        financialAccountId: String?,
        paymentMethodId: String?
    ) -> Unit,
    onDelete: (billId: String) -> Unit,
    onTogglePayment: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(installment.billTitle) }
    var description by remember { mutableStateOf("") }
    var amountCents by remember { mutableLongStateOf(installment.amountCents) }
    var dueDate by remember { mutableLongStateOf(installment.dueDate) }

    var selectedCategoryId by remember { mutableStateOf(installment.categoryId) }
    var selectedContactId by remember { mutableStateOf(installment.contactId) }
    var selectedFinancialAccountId by remember { mutableStateOf(installment.financialAccountId) }
    var selectedPaymentMethodId by remember { mutableStateOf(installment.paymentMethodId) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var contactExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val selectedCategory = categories.find { it.id == selectedCategoryId }
    val selectedContact = contacts.find { it.id == selectedContactId }
    val selectedAccount = financialAccounts.find { it.id == selectedFinancialAccountId }
    val selectedMethod = paymentMethods.find { it.id == selectedPaymentMethodId }

    val catColor = remember(installment.categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(installment.categoryColorHex))
        } catch (e: Exception) {
            Color(0xFF64748B)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Avatar, Título e Fechar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(catColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Editar Registro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (installment.totalInstallments > 1) {
                            "Parcela ${installment.installmentNumber} de ${installment.totalInstallments} • ${installment.type.label}"
                        } else {
                            installment.type.label
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Título / Descrição
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nome da Conta / Despesa *") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            // 2. Valor
            val displayCurrency = CurrencyUtils.formatCentsToCurrency(amountCents)
            OutlinedTextField(
                value = displayCurrency,
                onValueChange = { newValue ->
                    val cleanDigits = newValue.filter { it.isDigit() }
                    amountCents = cleanDigits.toLongOrNull() ?: 0L
                },
                label = { Text("Valor (R$) *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            // 3. Vencimento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(year, month, day, 12, 0, 0)
                                }
                                dueDate = selectedCal.timeInMillis
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
                Column {
                    Text(
                        text = "Data de Vencimento *",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = DateUtils.formatDate(dueDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Selecionar data",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 4. Categoria
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Selecione uma Categoria",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // 5. Favorecido / Contato
            ExposedDropdownMenuBox(
                expanded = contactExpanded,
                onExpandedChange = { contactExpanded = !contactExpanded }
            ) {
                OutlinedTextField(
                    value = selectedContact?.name ?: "Nenhum contato selecionado",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Favorecido / Fornecedor") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = contactExpanded,
                    onDismissRequest = { contactExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nenhum") },
                        onClick = {
                            selectedContactId = null
                            contactExpanded = false
                        }
                    )
                    contacts.forEach { contact ->
                        DropdownMenuItem(
                            text = { Text(contact.name) },
                            onClick = {
                                selectedContactId = contact.id
                                contactExpanded = false
                            }
                        )
                    }
                }
            }

            // 6. Conta Financeira
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "Nenhuma conta vinculada",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Conta de Débito / Banco") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nenhuma") },
                        onClick = {
                            selectedFinancialAccountId = null
                            accountExpanded = false
                        }
                    )
                    financialAccounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name) },
                            onClick = {
                                selectedFinancialAccountId = acc.id
                                accountExpanded = false
                            }
                        )
                    }
                }
            }

            // 7. Forma de Pagamento
            ExposedDropdownMenuBox(
                expanded = paymentMethodExpanded,
                onExpandedChange = { paymentMethodExpanded = !paymentMethodExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMethod?.name ?: "Nenhuma forma selecionada",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Forma de Pagamento") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = paymentMethodExpanded,
                    onDismissRequest = { paymentMethodExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Nenhuma") },
                        onClick = {
                            selectedPaymentMethodId = null
                            paymentMethodExpanded = false
                        }
                    )
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method.name) },
                            onClick = {
                                selectedPaymentMethodId = method.id
                                paymentMethodExpanded = false
                            }
                        )
                    }
                }
            }

            // 8. Botão de Alternar Pagamento
            OutlinedButton(
                onClick = onTogglePayment,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (installment.isPaid) SuccessGreen else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (installment.isPaid) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (installment.isPaid) "Marcar como Não Pago (Pendente)" else "Marcar como Pago")
            }

            // 9. Botões de Ação na base: Excluir e Salvar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excluir")
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && amountCents > 0L) {
                            onSave(
                                title,
                                description,
                                amountCents,
                                dueDate,
                                selectedCategoryId,
                                installment.itemId,
                                selectedContactId,
                                selectedFinancialAccountId,
                                selectedPaymentMethodId
                            )
                        }
                    },
                    enabled = title.isNotBlank() && amountCents > 0L,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Salvar Alterações", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Excluir Registro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Tem certeza de que deseja excluir '${installment.billTitle}'? Esta ação removerá a conta e suas parcelas do histórico.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(installment.billId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
