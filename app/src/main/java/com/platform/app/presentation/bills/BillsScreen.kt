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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
                        sheetState = sheetState,
                        onDismiss = { showAddSheet = false },
                        onConfirm = { title, desc, type, totalCents, catId, totalInst, dueDate ->
                            onAction(
                                BillsUiAction.CreateBill(
                                    title = title,
                                    description = desc,
                                    type = type,
                                    totalAmountCents = totalCents,
                                    categoryId = catId,
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
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (String, String, BillType, Long, String?, Int, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rawAmount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BillType.SINGLE) }
    var installmentsCountText by remember { mutableStateOf("2") }
    var selectedCategoryId by remember { mutableStateOf<String?>(categories.firstOrNull()?.id) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Nova Conta / Despesa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Seletor de Tipo
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nome da Conta (ex: Aluguel, Celular, Notebook)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rawAmount,
                onValueChange = { rawAmount = it },
                label = { Text("Valor Total (R$)") },
                placeholder = { Text("Ex: 150,00 ou 150") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedType == BillType.INSTALLMENT) {
                OutlinedTextField(
                    value = installmentsCountText,
                    onValueChange = { installmentsCountText = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantidade de Parcelas (ex: 10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name) }
                    )
                }
            }

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

                        if (title.isNotBlank() && amountCents > 0L) {
                            onConfirm(
                                title,
                                description,
                                selectedType,
                                amountCents,
                                selectedCategoryId,
                                totalInst,
                                System.currentTimeMillis()
                            )
                        }
                    },
                    enabled = title.isNotBlank() && rawAmount.isNotBlank()
                ) {
                    Text("Cadastrar Conta")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
