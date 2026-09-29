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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseScreen(
    viewModel: NewExpenseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var itemDropdownExpanded by remember { mutableStateOf(false) }
    var contactDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var cardDropdownExpanded by remember { mutableStateOf(false) }
    var showQuickContactDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is NewExpenseUiEffect.ExpenseSaved -> {
                    onNavigateBack()
                }
                is NewExpenseUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Nova Despesa",
                subtitle = "Previsão & Lançamento de Contas",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            // 1. DADOS OBRIGATÓRIOS DO LANÇAMENTO
            PlatformCard(
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingNormal),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Dados Principais *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Descrição
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Descrição da Despesa *") },
                        placeholder = { Text("Ex: Supermercado, Aluguel, Farmácia") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Valor
                    val displayCurrency = CurrencyUtils.formatCentsToCurrency(uiState.amountCents)
                    OutlinedTextField(
                        value = displayCurrency,
                        onValueChange = { newValue ->
                            val cleanDigits = newValue.filter { it.isDigit() }
                            val cents = cleanDigits.toLongOrNull() ?: 0L
                            viewModel.onAmountChange(cents)
                        },
                        label = { Text("Valor da Despesa *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
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
                            label = { Text("Item de Despesa * (Obrigatório)") },
                            placeholder = { Text("Selecione o item categorizado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded) },
                            isError = uiState.selectedItemId == null && uiState.description.isNotBlank(),
                            supportingText = if (uiState.selectedItemId == null && uiState.description.isNotBlank()) {
                                { Text("Item é obrigatório para classificar a despesa", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius),
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
                                DropdownMenuItem(
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
                                        viewModel.onItemSelect(item.id)
                                        itemDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Chip de Natureza Financeira Herdada
                    if (uiState.selectedItem != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Natureza Financeira:",
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
                            }
                        }
                    }

                    // SELETOR DE CONTATO / FORNECEDOR (OBRIGATÓRIO)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = contactDropdownExpanded,
                            onExpandedChange = { contactDropdownExpanded = !contactDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedContact?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Contato / Fornecedor * (Obrigatório)") },
                                placeholder = { Text("Selecione o favorecido") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactDropdownExpanded) },
                                isError = uiState.selectedContactId == null && uiState.description.isNotBlank(),
                                supportingText = if (uiState.selectedContactId == null && uiState.description.isNotBlank()) {
                                    { Text("Contato é obrigatório no lançamento", color = MaterialTheme.colorScheme.error) }
                                } else null,
                                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = contactDropdownExpanded,
                                onDismissRequest = { contactDropdownExpanded = false }
                            ) {
                                if (uiState.contacts.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhum contato cadastrado") },
                                        onClick = { contactDropdownExpanded = false }
                                    )
                                }
                                for (contact in uiState.contacts) {
                                    DropdownMenuItem(
                                        text = { Text(contact.name, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            viewModel.onContactSelect(contact.id)
                                            contactDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                newContactName = ""
                                showQuickContactDialog = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Novo Contato Rápido",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Vencimento e Data
                    val dateFormatted = DateUtils.formatDate(uiState.dueDate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vencimento Inicial *",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = uiState.dueDate }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val selectedCal = Calendar.getInstance().apply {
                                            set(year, month, day, 12, 0, 0)
                                        }
                                        viewModel.onDueDateChange(selectedCal.timeInMillis)
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
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Alterar Data")
                        }
                    }
                }
            }

            // 2. STATUS DO PAGAMENTO: PENDENTE OU JÁ PAGA
            PlatformCard(
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingNormal),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Situação da Conta",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Opção: Pendente
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!uiState.isPaid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (!uiState.isPaid) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.onPaymentStatusChange(false) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (!uiState.isPaid) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pendente",
                                    fontWeight = if (!uiState.isPaid) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!uiState.isPaid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Opção: Já Paga
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.isPaid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (uiState.isPaid) androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.onPaymentStatusChange(true) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (uiState.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Já Paga",
                                    fontWeight = if (uiState.isPaid) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.isPaid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. MODALIDADE: À VISTA, PARCELADO OU RECORRENTE
            PlatformCard(
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.expenseType == BillType.SINGLE,
                            onClick = { viewModel.onExpenseTypeChange(BillType.SINGLE) },
                            label = { Text("À Vista") },
                            leadingIcon = if (uiState.expenseType == BillType.SINGLE) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = uiState.expenseType == BillType.INSTALLMENT,
                            onClick = { viewModel.onExpenseTypeChange(BillType.INSTALLMENT) },
                            label = { Text("Parcelado") },
                            leadingIcon = if (uiState.expenseType == BillType.INSTALLMENT) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = uiState.expenseType == BillType.RECURRING,
                            onClick = { viewModel.onExpenseTypeChange(BillType.RECURRING) },
                            label = { Text("Recorrente") },
                            leadingIcon = if (uiState.expenseType == BillType.RECURRING) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

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
                                    onClick = { viewModel.onInstallmentsCountChange(uiState.installmentsCount - 1) },
                                    enabled = uiState.installmentsCount > 2
                                ) {
                                    Text("—", fontWeight = FontWeight.Bold)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
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
                                    onClick = { viewModel.onInstallmentsCountChange(uiState.installmentsCount + 1) },
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
                        Text(
                            text = "Despesa fixa mensal projetada automaticamente para o próximo período.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. FORMA DE PAGAMENTO, CONTA OU CARTÃO DE CRÉDITO
            PlatformCard(
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingNormal),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Meio de Pagamento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Toggle: Cartão de Crédito?
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (uiState.isCreditCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pagar com Cartão de Crédito",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Switch(
                            checked = uiState.isCreditCard,
                            onCheckedChange = viewModel::onToggleCreditCard,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    // Se CARTÃO DE CRÉDITO selecionado:
                    if (uiState.isCreditCard) {
                        ExposedDropdownMenuBox(
                            expanded = cardDropdownExpanded,
                            onExpandedChange = { cardDropdownExpanded = !cardDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedCreditCard?.let { card ->
                                    val avail = uiState.availableLimitForSelectedCard ?: 0L
                                    "${card.name} (Disp: ${CurrencyUtils.formatCentsToCurrency(avail)})"
                                } ?: "Selecione o Cartão de Crédito",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Cartão de Crédito *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardDropdownExpanded) },
                                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = cardDropdownExpanded,
                                onDismissRequest = { cardDropdownExpanded = false }
                            ) {
                                for (card in uiState.creditCards) {
                                    val avail = uiState.creditCardSummaries[card.id] ?: card.totalLimitCents
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(card.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "Limite Disp: ${CurrencyUtils.formatCentsToCurrency(avail)} • Corte dia ${card.closingDay}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.onCreditCardSelect(card.id)
                                            cardDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Alerta se excede o limite
                        if (uiState.isExceedingCreditLimit) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WarningAmber.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aviso: O valor da despesa excede o limite disponível deste cartão.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        // Se NÃO FOR CARTÃO: Seletor de Forma de Pagamento e Conta
                        ExposedDropdownMenuBox(
                            expanded = methodDropdownExpanded,
                            onExpandedChange = { methodDropdownExpanded = !methodDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedPaymentMethod?.name ?: "Selecione a Forma",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Forma de Pagamento (PIX, Boleto, etc.)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = methodDropdownExpanded,
                                onDismissRequest = { methodDropdownExpanded = false }
                            ) {
                                for (pm in uiState.paymentMethods) {
                                    DropdownMenuItem(
                                        text = { Text(pm.name) },
                                        onClick = {
                                            viewModel.onPaymentMethodSelect(pm.id)
                                            methodDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = accountDropdownExpanded,
                            onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.selectedFinancialAccount?.name ?: "Selecione a Conta",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Conta Financeira / Banco") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = accountDropdownExpanded,
                                onDismissRequest = { accountDropdownExpanded = false }
                            ) {
                                for (acc in uiState.financialAccounts) {
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            viewModel.onFinancialAccountSelect(acc.id)
                                            accountDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            // BOTÃO SALVAR DESPESA
            Button(
                onClick = viewModel::saveExpense,
                enabled = uiState.isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirmar e Salvar Despesa",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }

    // DIÁLOGO RÁPIDO DE NOVO CONTATO
    if (showQuickContactDialog) {
        AlertDialog(
            onDismissRequest = { showQuickContactDialog = false },
            title = { Text("Novo Contato / Fornecedor") },
            text = {
                OutlinedTextField(
                    value = newContactName,
                    onValueChange = { newContactName = it },
                    label = { Text("Nome do Contato ou Estabelecimento *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank()) {
                            viewModel.createQuickContact(newContactName)
                            showQuickContactDialog = false
                        }
                    },
                    enabled = newContactName.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickContactDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
