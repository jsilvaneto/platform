package com.platform.app.presentation.management

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.common.AppStrings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.PaymentMethod
import com.platform.app.presentation.management.components.AccountDetailBottomSheet
import com.platform.app.presentation.management.components.AccountsTab
import com.platform.app.presentation.management.components.AddEditAccountDialog
import com.platform.app.presentation.management.components.AddEditCategoryDialog
import com.platform.app.presentation.management.components.AddEditPaymentMethodDialog
import com.platform.app.presentation.management.components.CategoriesTab
import com.platform.app.presentation.management.components.CategoryDetailBottomSheet
import com.platform.app.presentation.management.components.PaymentMethodDetailBottomSheet
import com.platform.app.presentation.management.components.PaymentMethodsTab
import com.platform.app.presentation.expenseitems.ExpenseItemsScreen
import com.platform.app.presentation.expenseitems.ExpenseItemsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

enum class ManagementSection {
    ACCOUNTS,
    PAYMENT_METHODS,
    CATEGORIES,
    EXPENSE_ITEMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    viewModel: ManagementViewModel,
    expenseItemsViewModel: ExpenseItemsViewModel = hiltViewModel(),
    initialTab: Int = 0,
    forcedSection: ManagementSection? = null,
    onOpenDrawer: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemsUiState by expenseItemsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val activeSection = forcedSection ?: when (uiState.selectedTab) {
        0 -> ManagementSection.ACCOUNTS
        1 -> ManagementSection.PAYMENT_METHODS
        2 -> ManagementSection.CATEGORIES
        3 -> ManagementSection.EXPENSE_ITEMS
        else -> ManagementSection.ACCOUNTS
    }

    LaunchedEffect(forcedSection, initialTab) {
        val targetTab = when (forcedSection) {
            ManagementSection.ACCOUNTS -> 0
            ManagementSection.PAYMENT_METHODS -> 1
            ManagementSection.CATEGORIES -> 2
            ManagementSection.EXPENSE_ITEMS -> 3
            null -> initialTab.coerceIn(0, 3)
        }
        viewModel.onAction(ManagementUiAction.SelectTab(targetTab))
    }

    var accountToViewDetails by remember { mutableStateOf<FinancialAccount?>(null) }
    var accountToEdit by remember { mutableStateOf<FinancialAccount?>(null) }
    var isNewAccountDialog by remember { mutableStateOf(false) }

    var methodToViewDetails by remember { mutableStateOf<PaymentMethod?>(null) }
    var methodToEdit by remember { mutableStateOf<PaymentMethod?>(null) }
    var isNewMethodDialog by remember { mutableStateOf(false) }

    var categoryToViewDetails by remember { mutableStateOf<Category?>(null) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var isNewCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ManagementUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ManagementUiEffect.ItemSaved -> {
                    accountToViewDetails = null
                    accountToEdit = null
                    isNewAccountDialog = false
                    methodToViewDetails = null
                    methodToEdit = null
                    isNewMethodDialog = false
                    categoryToViewDetails = null
                    categoryToEdit = null
                    isNewCategoryDialog = false
                }
            }
        }
    }

    val screenTitle = when (activeSection) {
        ManagementSection.ACCOUNTS -> "Contas"
        ManagementSection.PAYMENT_METHODS -> "Formas de Pagamento"
        ManagementSection.CATEGORIES -> "Categorias"
        ManagementSection.EXPENSE_ITEMS -> "Itens de Despesa"
    }

    val fabDescription = when (activeSection) {
        ManagementSection.ACCOUNTS -> "Nova Conta"
        ManagementSection.PAYMENT_METHODS -> "Nova Forma de Pagamento"
        ManagementSection.CATEGORIES -> "Nova Categoria"
        ManagementSection.EXPENSE_ITEMS -> "Novo Item"
    }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = screenTitle,
                onOpenDrawer = onOpenDrawer,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (activeSection != ManagementSection.EXPENSE_ITEMS) {
                FloatingActionButton(
                    onClick = {
                        when (activeSection) {
                            ManagementSection.ACCOUNTS -> isNewAccountDialog = true
                            ManagementSection.PAYMENT_METHODS -> isNewMethodDialog = true
                            ManagementSection.CATEGORIES -> isNewCategoryDialog = true
                            ManagementSection.EXPENSE_ITEMS -> {}
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = fabDescription)
                }
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
                        text = { Text(AppStrings.Management.ACCOUNTS) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.onAction(ManagementUiAction.SelectTab(1)) },
                        text = { Text(AppStrings.Management.PAYMENT_METHODS) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.onAction(ManagementUiAction.SelectTab(2)) },
                        text = { Text(AppStrings.Management.CATEGORIES) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.onAction(ManagementUiAction.SelectTab(3)) },
                        text = { Text(AppStrings.Management.EXPENSE_ITEMS) }
                    )
                }
            }

            if (uiState.isLoading && activeSection != ManagementSection.EXPENSE_ITEMS) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (activeSection) {
                    ManagementSection.ACCOUNTS -> AccountsTab(
                        accounts = uiState.accounts,
                        onSelectAccount = { accountToViewDetails = it }
                    )
                    ManagementSection.PAYMENT_METHODS -> PaymentMethodsTab(
                        methods = uiState.paymentMethods,
                        onSelectMethod = { methodToViewDetails = it }
                    )
                    ManagementSection.CATEGORIES -> CategoriesTab(
                        categories = uiState.categories,
                        onSelectCategory = { categoryToViewDetails = it }
                    )
                    ManagementSection.EXPENSE_ITEMS -> ExpenseItemsScreen(
                        uiState = itemsUiState,
                        onAction = expenseItemsViewModel::onAction,
                        onNavigateBack = { onNavigateBack?.invoke() },
                        showTopBar = false
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
                onConfirm = { account ->
                    viewModel.onAction(ManagementUiAction.SaveAccount(account))
                    isNewAccountDialog = false
                    accountToEdit = null
                }
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
                onConfirm = { method ->
                    viewModel.onAction(ManagementUiAction.SavePaymentMethod(method))
                    isNewMethodDialog = false
                    methodToEdit = null
                }
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
                onConfirm = { category ->
                    viewModel.onAction(ManagementUiAction.SaveCategory(category))
                    isNewCategoryDialog = false
                    categoryToEdit = null
                }
            )
        }

        // Detalhes da Conta (Bottom Sheet)
        accountToViewDetails?.let { account ->
            AccountDetailBottomSheet(
                account = account,
                installments = uiState.installments,
                onDismiss = { accountToViewDetails = null },
                onEdit = {
                    accountToViewDetails = null
                    accountToEdit = it
                },
                onDelete = { accountId ->
                    accountToViewDetails = null
                    viewModel.onAction(ManagementUiAction.DeleteAccount(accountId))
                }
            )
        }

        // Detalhes da Forma de Pagamento (Bottom Sheet)
        methodToViewDetails?.let { method ->
            PaymentMethodDetailBottomSheet(
                method = method,
                installments = uiState.installments,
                onDismiss = { methodToViewDetails = null },
                onEdit = {
                    methodToViewDetails = null
                    methodToEdit = it
                },
                onDelete = { methodId ->
                    methodToViewDetails = null
                    viewModel.onAction(ManagementUiAction.DeletePaymentMethod(methodId))
                }
            )
        }

        // Detalhes da Categoria (Bottom Sheet)
        categoryToViewDetails?.let { category ->
            CategoryDetailBottomSheet(
                category = category,
                installments = uiState.installments,
                onDismiss = { categoryToViewDetails = null },
                onEdit = {
                    categoryToViewDetails = null
                    categoryToEdit = it
                },
                onDelete = { categoryId ->
                    categoryToViewDetails = null
                    viewModel.onAction(ManagementUiAction.DeleteCategory(categoryId))
                }
            )
        }
    }
}
