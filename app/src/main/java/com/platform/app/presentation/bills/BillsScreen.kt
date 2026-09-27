package com.platform.app.presentation.bills

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory
import java.util.Calendar
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.Category
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.dashboard.MonthSelector
import com.platform.app.presentation.home.OfflineBadge
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    uiState: BillsUiState,
    uiEffect: Flow<BillsUiEffect>,
    onAction: (BillsUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiEffect) {
        uiEffect.collect { effect ->
            when (effect) {
                is BillsUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Contas a Pagar",
                actions = {
                    OfflineBadge()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Seletor de Mês
            MonthSelector(
                selectedMonthMillis = uiState.selectedMonthMillis,
                onPreviousMonth = {
                    val prev = DateUtils.addMonths(uiState.selectedMonthMillis, -1)
                    onAction(BillsUiAction.MonthChanged(prev))
                },
                onNextMonth = {
                    val next = DateUtils.addMonths(uiState.selectedMonthMillis, 1)
                    onAction(BillsUiAction.MonthChanged(next))
                },
                onCurrentMonth = {
                    onAction(BillsUiAction.MonthChanged(System.currentTimeMillis()))
                }
            )

            // Busca
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onAction(BillsUiAction.SearchQueryChanged(it)) },
                label = { Text("Buscar contas ou categorias...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Filtros de Tipo e Status
            FilterChipsRow(
                typeFilter = uiState.typeFilter,
                statusFilter = uiState.statusFilter,
                onTypeFilterChange = { onAction(BillsUiAction.TypeFilterChanged(it)) },
                onStatusFilterChange = { onAction(BillsUiAction.StatusFilterChanged(it)) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.installments.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.filteredInstallments.isEmpty() -> {
                        EmptyBillsState()
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.filteredInstallments, key = { it.id }) { installment ->
                                BillInstallmentItemCard(
                                    installment = installment,
                                    onTogglePayment = { onAction(BillsUiAction.TogglePayment(installment)) },
                                    onDelete = { onAction(BillsUiAction.DeleteBill(installment.billId)) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }

                if (showAddSheet) {
                    AddBillBottomSheet(
                        categories = uiState.categories,
                        subcategories = uiState.subcategories,
                        contacts = uiState.contacts,
                        financialAccounts = uiState.financialAccounts,
                        paymentMethods = uiState.paymentMethods,
                        sheetState = sheetState,
                        onDismiss = { showAddSheet = false },
                        onConfirm = { title, desc, type, totalCents, catId, subcatId, contactId, accountId, paymentMethodId, totalInst, dueDate ->
                            onAction(
                                BillsUiAction.CreateBill(
                                    title = title,
                                    description = desc,
                                    type = type,
                                    totalAmountCents = totalCents,
                                    categoryId = catId,
                                    subcategoryId = subcatId,
                                    contactId = contactId,
                                    financialAccountId = accountId,
                                    paymentMethodId = paymentMethodId,
                                    totalInstallments = totalInst,
                                    firstDueDate = dueDate
                                )
                            )
                            showAddSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    typeFilter: BillType?,
    statusFilter: BillStatus?,
    onTypeFilterChange: (BillType?) -> Unit,
    onStatusFilterChange: (BillStatus?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = typeFilter == null,
                onClick = { onTypeFilterChange(null) },
                label = { Text("Todos Tipos") }
            )
        }
        item {
            FilterChip(
                selected = typeFilter == BillType.SINGLE,
                onClick = { onTypeFilterChange(if (typeFilter == BillType.SINGLE) null else BillType.SINGLE) },
                label = { Text("Avulsas") }
            )
        }
        item {
            FilterChip(
                selected = typeFilter == BillType.INSTALLMENT,
                onClick = { onTypeFilterChange(if (typeFilter == BillType.INSTALLMENT) null else BillType.INSTALLMENT) },
                label = { Text("Parceladas") }
            )
        }
        item {
            FilterChip(
                selected = typeFilter == BillType.RECURRING,
                onClick = { onTypeFilterChange(if (typeFilter == BillType.RECURRING) null else BillType.RECURRING) },
                label = { Text("Recorrentes") }
            )
        }
        item {
            FilterChip(
                selected = statusFilter == BillStatus.PENDING,
                onClick = { onStatusFilterChange(if (statusFilter == BillStatus.PENDING) null else BillStatus.PENDING) },
                label = { Text("A Pagar") }
            )
        }
        item {
            FilterChip(
                selected = statusFilter == BillStatus.PAID,
                onClick = { onStatusFilterChange(if (statusFilter == BillStatus.PAID) null else BillStatus.PAID) },
                label = { Text("Pagas") }
            )
        }
    }
}

@Composable
fun BillInstallmentItemCard(
    installment: BillInstallment,
    onTogglePayment: () -> Unit,
    onDelete: () -> Unit
) {
    val isOverdue = !installment.isPaid && installment.dueDate < System.currentTimeMillis()

    val catColor = try {
        Color(android.graphics.Color.parseColor(installment.categoryColorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (installment.isPaid)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (installment.isPaid) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTogglePayment() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = installment.isPaid,
                onCheckedChange = { onTogglePayment() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = installment.billTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (installment.isPaid) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (installment.isPaid)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    InstallmentBadge(installment)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(catColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = installment.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = when {
                            installment.isPaid -> "Pago"
                            isOverdue -> "Venceu em ${DateUtils.formatDate(installment.dueDate)}"
                            else -> "Vence em ${DateUtils.formatDate(installment.dueDate)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            installment.isPaid -> Color(0xFF10B981)
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (installment.isPaid)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onDelete) {
                    Text("Excluir", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun InstallmentBadge(installment: BillInstallment) {
    val (label, bg, fg) = when (installment.type) {
        BillType.SINGLE -> Triple("Avulsa", MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), MaterialTheme.colorScheme.secondary)
        BillType.INSTALLMENT -> Triple("${installment.installmentNumber}/${installment.totalInstallments}", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFD97706))
        BillType.RECURRING -> Triple("Recorrente", Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED))
    }

    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillBottomSheet(
    categories: List<Category>,
    subcategories: List<Subcategory>,
    contacts: List<Contact>,
    financialAccounts: List<FinancialAccount>,
    paymentMethods: List<PaymentMethod>,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        type: BillType,
        amountCents: Long,
        categoryId: String?,
        subcategoryId: String?,
        contactId: String?,
        accountId: String?,
        paymentMethodId: String?,
        totalInstallments: Int,
        dueDate: Long
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BillType.SINGLE) }
    var installmentsCountText by remember { mutableStateOf("2") }
    var selectedCategoryId by remember { mutableStateOf<String?>(categories.firstOrNull()?.id) }
    var selectedSubcategoryId by remember { mutableStateOf<String?>(null) }
    var selectedContactId by remember { mutableStateOf<String?>(null) }
    var selectedAccountId by remember { mutableStateOf<String?>(financialAccounts.firstOrNull()?.id) }
    var selectedPaymentMethodId by remember { mutableStateOf<String?>(paymentMethods.firstOrNull()?.id) }
    var dueDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showValidationError by remember { mutableStateOf(false) }

    val filteredSubcategories = remember(selectedCategoryId, subcategories) {
        if (selectedCategoryId == null) emptyList()
        else subcategories.filter { it.categoryId == selectedCategoryId }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Nova Conta / Despesa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Seletor de Tipo
            Text(
                text = "Tipo de Despesa:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BillType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label) }
                    )
                }
            }

            // Título
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    showValidationError = false
                },
                label = { Text("Nome da Conta * (ex: Aluguel, Celular, Notebook)") },
                isError = showValidationError && title.isBlank(),
                supportingText = if (showValidationError && title.isBlank()) {
                    { Text("Nome da conta é obrigatório", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Descrição (Observações)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição / Observações (opcional)") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Valor
            OutlinedTextField(
                value = rawAmount,
                onValueChange = {
                    rawAmount = it
                    showValidationError = false
                },
                label = { Text("Valor Total (R$) *") },
                placeholder = { Text("Ex: 150,00 ou 150") },
                isError = showValidationError && CurrencyUtils.parseInputToCents(rawAmount) <= 0L,
                supportingText = if (showValidationError && CurrencyUtils.parseInputToCents(rawAmount) <= 0L) {
                    { Text("Informe um valor maior que R$ 0,00", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Parcelas se for parcelada
            if (selectedType == BillType.INSTALLMENT) {
                OutlinedTextField(
                    value = installmentsCountText,
                    onValueChange = { installmentsCountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantidade de Parcelas (mínimo 2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Data de Vencimento
            Text(
                text = "Data de Primeiro Vencimento: ${DateUtils.formatDate(dueDateMillis)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val now = System.currentTimeMillis()
                    FilterChip(
                        selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(now),
                        onClick = { dueDateMillis = now },
                        label = { Text("Hoje") }
                    )
                }
                item {
                    val plus7 = System.currentTimeMillis() + 7L * 24 * 3600 * 1000
                    FilterChip(
                        selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(plus7),
                        onClick = { dueDateMillis = plus7 },
                        label = { Text("+7 dias") }
                    )
                }
                item {
                    val plus15 = System.currentTimeMillis() + 15L * 24 * 3600 * 1000
                    FilterChip(
                        selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(plus15),
                        onClick = { dueDateMillis = plus15 },
                        label = { Text("+15 dias") }
                    )
                }
                item {
                    val plus30 = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
                    FilterChip(
                        selected = DateUtils.formatDate(dueDateMillis) == DateUtils.formatDate(plus30),
                        onClick = { dueDateMillis = plus30 },
                        label = { Text("+30 dias") }
                    )
                }
            }

            // Seleção de Contato
            if (contacts.isNotEmpty()) {
                Text(
                    text = "Vincular a Contato:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedContactId == null,
                            onClick = { selectedContactId = null },
                            label = { Text("Nenhum") }
                        )
                    }
                    items(contacts, key = { it.id }) { c ->
                        FilterChip(
                            selected = selectedContactId == c.id,
                            onClick = { selectedContactId = c.id },
                            label = { Text(c.name) }
                        )
                    }
                }
            }

            // Seleção de Conta Financeira
            if (financialAccounts.isNotEmpty()) {
                Text(
                    text = "Conta de Débito / Saída:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedAccountId == null,
                            onClick = { selectedAccountId = null },
                            label = { Text("Nenhuma") }
                        )
                    }
                    items(financialAccounts, key = { it.id }) { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name) }
                        )
                    }
                }
            }

            // Seleção de Forma de Pagamento
            if (paymentMethods.isNotEmpty()) {
                Text(
                    text = "Forma de Pagamento:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedPaymentMethodId == null,
                            onClick = { selectedPaymentMethodId = null },
                            label = { Text("Nenhuma") }
                        )
                    }
                    items(paymentMethods, key = { it.id }) { method ->
                        FilterChip(
                            selected = selectedPaymentMethodId == method.id,
                            onClick = { selectedPaymentMethodId = method.id },
                            label = { Text(method.name) }
                        )
                    }
                }
            }

            // Seleção de Categoria
            Text(
                text = "Categoria:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            selectedCategoryId = cat.id
                            selectedSubcategoryId = null
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            // Seleção de Subcategoria (se houver para a categoria selecionada)
            if (filteredSubcategories.isNotEmpty()) {
                Text(
                    text = "Subcategoria:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedSubcategoryId == null,
                            onClick = { selectedSubcategoryId = null },
                            label = { Text("Nenhuma") }
                        )
                    }
                    items(filteredSubcategories, key = { it.id }) { sub ->
                        FilterChip(
                            selected = selectedSubcategoryId == sub.id,
                            onClick = { selectedSubcategoryId = sub.id },
                            label = { Text(sub.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val amountCents = CurrencyUtils.parseInputToCents(rawAmount)
                        val totalInst = if (selectedType == BillType.INSTALLMENT)
                            installmentsCountText.toIntOrNull()?.coerceAtLeast(2) ?: 2
                        else
                            1

                        if (title.isBlank() || amountCents <= 0L) {
                            showValidationError = true
                        } else {
                            onConfirm(
                                title.trim(),
                                description.trim(),
                                selectedType,
                                amountCents,
                                selectedCategoryId,
                                selectedSubcategoryId,
                                selectedContactId,
                                selectedAccountId,
                                selectedPaymentMethodId,
                                totalInst,
                                dueDateMillis
                            )
                        }
                    }
                ) {
                    Text("Cadastrar Conta")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmptyBillsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "💳", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhuma conta encontrada",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Toque no botão '+' abaixo para cadastrar sua primeira conta avulsa, parcelada ou recorrente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
