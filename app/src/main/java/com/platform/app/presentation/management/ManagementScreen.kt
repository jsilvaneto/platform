package com.platform.app.presentation.management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.domain.model.Subcategory
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

enum class ManagementSection {
    ACCOUNTS,
    PAYMENT_METHODS,
    CATEGORIES
}

@Composable
fun AccountsScreen(
    viewModel: ManagementViewModel,
    onNavigateBack: () -> Unit
) {
    ManagementScreen(
        viewModel = viewModel,
        forcedSection = ManagementSection.ACCOUNTS,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun PaymentMethodsScreen(
    viewModel: ManagementViewModel,
    onNavigateBack: () -> Unit
) {
    ManagementScreen(
        viewModel = viewModel,
        forcedSection = ManagementSection.PAYMENT_METHODS,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun CategoriesScreen(
    viewModel: ManagementViewModel,
    onNavigateBack: () -> Unit
) {
    ManagementScreen(
        viewModel = viewModel,
        forcedSection = ManagementSection.CATEGORIES,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    viewModel: ManagementViewModel,
    initialTab: Int = 0,
    forcedSection: ManagementSection? = null,
    onOpenDrawer: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val activeSection = forcedSection ?: when (uiState.selectedTab) {
        0 -> ManagementSection.ACCOUNTS
        1 -> ManagementSection.PAYMENT_METHODS
        2 -> ManagementSection.CATEGORIES
        else -> ManagementSection.ACCOUNTS
    }

    LaunchedEffect(forcedSection, initialTab) {
        val targetTab = when (forcedSection) {
            ManagementSection.ACCOUNTS -> 0
            ManagementSection.PAYMENT_METHODS -> 1
            ManagementSection.CATEGORIES -> 2
            null -> initialTab.coerceIn(0, 2)
        }
        viewModel.onAction(ManagementUiAction.SelectTab(targetTab))
    }

    var accountToEdit by remember { mutableStateOf<FinancialAccount?>(null) }
    var isNewAccountDialog by remember { mutableStateOf(false) }

    var methodToEdit by remember { mutableStateOf<PaymentMethod?>(null) }
    var isNewMethodDialog by remember { mutableStateOf(false) }

    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var isNewCategoryDialog by remember { mutableStateOf(false) }

    var subcategoryTargetCategory by remember { mutableStateOf<Category?>(null) }
    var subcategoryToEdit by remember { mutableStateOf<Pair<Subcategory, Category>?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ManagementUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ManagementUiEffect.ItemSaved -> {
                    accountToEdit = null
                    isNewAccountDialog = false
                    methodToEdit = null
                    isNewMethodDialog = false
                    categoryToEdit = null
                    isNewCategoryDialog = false
                    subcategoryTargetCategory = null
                    subcategoryToEdit = null
                }
            }
        }
    }

    val screenTitle = when (activeSection) {
        ManagementSection.ACCOUNTS -> "Contas"
        ManagementSection.PAYMENT_METHODS -> "Formas de Pagamento"
        ManagementSection.CATEGORIES -> "Categorias"
    }

    val fabDescription = when (activeSection) {
        ManagementSection.ACCOUNTS -> "Nova Conta"
        ManagementSection.PAYMENT_METHODS -> "Nova Forma de Pagamento"
        ManagementSection.CATEGORIES -> "Nova Categoria"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    } else {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu lateral")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeSection) {
                        ManagementSection.ACCOUNTS -> isNewAccountDialog = true
                        ManagementSection.PAYMENT_METHODS -> isNewMethodDialog = true
                        ManagementSection.CATEGORIES -> isNewCategoryDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = fabDescription)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (forcedSection == null) {
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.onAction(ManagementUiAction.SelectTab(0)) },
                        text = { Text("Contas") }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.onAction(ManagementUiAction.SelectTab(1)) },
                        text = { Text("Pagamentos") }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.onAction(ManagementUiAction.SelectTab(2)) },
                        text = { Text("Categorias") }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (activeSection) {
                    ManagementSection.ACCOUNTS -> AccountsTab(
                        accounts = uiState.accounts,
                        onEdit = { accountToEdit = it },
                        onDelete = { viewModel.onAction(ManagementUiAction.DeleteAccount(it)) }
                    )
                    ManagementSection.PAYMENT_METHODS -> PaymentMethodsTab(
                        methods = uiState.paymentMethods,
                        onEdit = { methodToEdit = it },
                        onDelete = { viewModel.onAction(ManagementUiAction.DeletePaymentMethod(it)) }
                    )
                    ManagementSection.CATEGORIES -> CategoriesTab(
                        categories = uiState.categories,
                        subcategories = uiState.subcategories,
                        onEditCategory = { categoryToEdit = it },
                        onEditSubcategory = { sub, cat -> subcategoryToEdit = Pair(sub, cat) },
                        onDeleteCategory = { viewModel.onAction(ManagementUiAction.DeleteCategory(it)) },
                        onDeleteSubcategory = { viewModel.onAction(ManagementUiAction.DeleteSubcategory(it)) },
                        onAddSubcategory = { category -> subcategoryTargetCategory = category }
                    )
                }
            }
        }

        // Diálogos de Contas Financeiras
        if (isNewAccountDialog || accountToEdit != null) {
            AddEditAccountDialog(
                account = accountToEdit,
                onDismiss = {
                    isNewAccountDialog = false
                    accountToEdit = null
                },
                onConfirm = { account -> viewModel.onAction(ManagementUiAction.SaveAccount(account)) }
            )
        }

        // Diálogos de Formas de Pagamento
        if (isNewMethodDialog || methodToEdit != null) {
            AddEditPaymentMethodDialog(
                method = methodToEdit,
                onDismiss = {
                    isNewMethodDialog = false
                    methodToEdit = null
                },
                onConfirm = { method -> viewModel.onAction(ManagementUiAction.SavePaymentMethod(method)) }
            )
        }

        // Diálogos de Categorias
        if (isNewCategoryDialog || categoryToEdit != null) {
            AddEditCategoryDialog(
                category = categoryToEdit,
                onDismiss = {
                    isNewCategoryDialog = false
                    categoryToEdit = null
                },
                onConfirm = { category -> viewModel.onAction(ManagementUiAction.SaveCategory(category)) }
            )
        }

        // Nova Subcategoria
        subcategoryTargetCategory?.let { targetCat ->
            AddEditSubcategoryDialog(
                subcategory = null,
                category = targetCat,
                onDismiss = { subcategoryTargetCategory = null },
                onConfirm = { sub -> viewModel.onAction(ManagementUiAction.SaveSubcategory(sub)) }
            )
        }

        // Editar Subcategoria existente
        subcategoryToEdit?.let { (sub, targetCat) ->
            AddEditSubcategoryDialog(
                subcategory = sub,
                category = targetCat,
                onDismiss = { subcategoryToEdit = null },
                onConfirm = { updatedSub -> viewModel.onAction(ManagementUiAction.SaveSubcategory(updatedSub)) }
            )
        }
    }
}

@Composable
fun AccountsTab(
    accounts: List<FinancialAccount>,
    onEdit: (FinancialAccount) -> Unit,
    onDelete: (String) -> Unit
) {
    if (accounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Nenhuma conta financeira cadastrada. Toque no '+' para adicionar.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(accounts, key = { it.id }) { account ->
                val color = remember(account.colorHex) {
                    try { Color(android.graphics.Color.parseColor(account.colorHex)) } catch (e: Exception) { Color(0xFF3B82F6) }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(account) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = color)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (account.accountType.isNotBlank()) {
                                Text(
                                    text = account.accountType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        IconButton(onClick = { onEdit(account) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar Conta", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { onDelete(account.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir Conta", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsTab(
    methods: List<PaymentMethod>,
    onEdit: (PaymentMethod) -> Unit,
    onDelete: (String) -> Unit
) {
    if (methods.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Nenhuma forma de pagamento cadastrada. Toque no '+' para adicionar.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(methods, key = { it.id }) { method ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(method) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when {
                            method.name.contains("Pix", ignoreCase = true) -> Icons.Default.QrCode
                            method.name.contains("Boleto", ignoreCase = true) -> Icons.Default.Receipt
                            method.name.contains("Cartão", ignoreCase = true) -> Icons.Default.CreditCard
                            method.name.contains("Dinheiro", ignoreCase = true) -> Icons.Default.Payments
                            else -> Icons.Default.AccountBalance
                        }

                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { onEdit(method) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar Forma de Pagamento", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { onDelete(method.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir Forma de Pagamento", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesTab(
    categories: List<Category>,
    subcategories: List<Subcategory>,
    onEditCategory: (Category) -> Unit,
    onEditSubcategory: (Subcategory, Category) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onDeleteSubcategory: (String) -> Unit,
    onAddSubcategory: (Category) -> Unit
) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Nenhuma categoria cadastrada. Toque no '+' para adicionar.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                val subs = remember(subcategories, cat.id) {
                    subcategories.filter { it.categoryId == cat.id }
                }
                var expanded by remember { mutableStateOf(false) }

                val color = remember(cat.colorHex) {
                    try { Color(android.graphics.Color.parseColor(cat.colorHex)) } catch (e: Exception) { Color(0xFF3B82F6) }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.size(14.dp).background(color, CircleShape))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${subs.size} subs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            IconButton(onClick = { onEditCategory(cat) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Categoria",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expandir"
                                )
                            }

                            IconButton(onClick = { onDeleteCategory(cat.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir Categoria",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                )
                            }
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            if (subs.isEmpty()) {
                                Text(
                                    text = "Nenhuma subcategoria ainda.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            } else {
                                subs.forEach { sub ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 24.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${sub.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = { onEditSubcategory(sub, cat) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar Subcategoria",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteSubcategory(sub.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir Subcategoria",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { onAddSubcategory(cat) },
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Adicionar Subcategoria", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditAccountDialog(
    account: FinancialAccount?,
    onDismiss: () -> Unit,
    onConfirm: (FinancialAccount) -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    val types = listOf("Conta Corrente", "Cartão de Crédito", "Dinheiro / Carteira", "Investimento / Reserva", "Outro")
    var selectedType by remember { mutableStateOf(account?.accountType ?: types[0]) }
    val colors = listOf("#3B82F6", "#8B5CF6", "#10B981", "#F59E0B", "#EF4444", "#EC4899", "#64748B")
    var selectedColor by remember { mutableStateOf(account?.colorHex ?: colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "Nova Conta Financeira" else "Editar Conta Financeira") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta (ex: Nubank, Itaú, Carteira)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de Conta:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(types) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) }
                        )
                    }
                }

                Text("Cor de Identificação:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    colors.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
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
                enabled = name.isNotBlank()
            ) {
                Text(if (account == null) "Salvar" else "Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEditPaymentMethodDialog(
    method: PaymentMethod?,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod) -> Unit
) {
    var name by remember { mutableStateOf(method?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (method == null) "Nova Forma de Pagamento" else "Editar Forma de Pagamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome (ex: Pix, Boleto, Vale Refeição)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            PaymentMethod(
                                id = method?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                iconName = method?.iconName ?: "payments"
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (method == null) "Salvar" else "Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEditCategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    val colors = listOf("#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EF4444", "#EC4899", "#6366F1", "#64748B")
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Nova Categoria" else "Editar Categoria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Categoria (ex: Vestuário, Viagens)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Cor de Identificação:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
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
                                iconName = category?.iconName ?: "category"
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (category == null) "Salvar" else "Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEditSubcategoryDialog(
    subcategory: Subcategory?,
    category: Category,
    onDismiss: () -> Unit,
    onConfirm: (Subcategory) -> Unit
) {
    var name by remember { mutableStateOf(subcategory?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (subcategory == null)
                    "Nova Subcategoria em '${category.name}'"
                else
                    "Editar Subcategoria em '${category.name}'"
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Subcategoria (ex: Farmácia, Mercado)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            Subcategory(
                                id = subcategory?.id ?: UUID.randomUUID().toString(),
                                categoryId = category.id,
                                name = name.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (subcategory == null) "Adicionar" else "Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
