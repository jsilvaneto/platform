package com.platform.app.presentation.bills

import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.PlatformSurfaceVariant
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.platform.app.presentation.bills.components.ExpenseCommitmentTypeCard
import com.platform.app.presentation.bills.components.ExpenseItemAndAmountCard
import com.platform.app.presentation.bills.components.ExpensePaymentMethodCard
import com.platform.app.presentation.bills.components.ExpensePaymentStatusCard
import com.platform.app.presentation.bills.components.ExpenseRecipientAndDueDateCard
import com.platform.app.presentation.bills.components.QuickContactDialog
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.Dimens
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseScreen(
    viewModel: NewExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExpenseItems: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showQuickContactDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val notificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.saveExpense()
    }

    val onSaveClick = {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            !androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.saveExpense()
        }
    }

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

    NewExpenseContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSaveClick = onSaveClick,
        onItemSelect = { viewModel.onItemSelect(it) },
        onAmountChange = viewModel::onAmountChange,
        onNavigateToExpenseItems = onNavigateToExpenseItems,
        onContactSelect = { viewModel.onContactSelect(it) },
        onDueDateChange = viewModel::onDueDateChange,
        onNewQuickContactClick = { showQuickContactDialog = true },
        onExpenseTypeChange = viewModel::onExpenseTypeChange,
        onInstallmentsCountChange = viewModel::onInstallmentsCountChange,
        onRecurrenceFrequencyChange = viewModel::onRecurrenceFrequencyChange,
        onRecurrenceEndTypeChange = viewModel::onRecurrenceEndTypeChange,
        onRecurrenceEndDateChange = viewModel::onRecurrenceEndDateChange,
        onRecurrenceOccurrencesCountChange = viewModel::onRecurrenceOccurrencesCountChange,
        onToggleCreditCard = viewModel::onToggleCreditCard,
        onCreditCardSelect = { viewModel.onCreditCardSelect(it) },
        onPaymentMethodSelect = { viewModel.onPaymentMethodSelect(it) },
        onFinancialAccountSelect = { viewModel.onFinancialAccountSelect(it) },
        onPaymentStatusChange = viewModel::onPaymentStatusChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )

    if (showQuickContactDialog) {
        QuickContactDialog(
            onDismiss = { showQuickContactDialog = false },
            onConfirm = { contactName ->
                viewModel.createQuickContact(contactName)
                showQuickContactDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseContent(
    uiState: NewExpenseUiState,
    onNavigateBack: () -> Unit,
    onSaveClick: () -> Unit,
    onItemSelect: (String) -> Unit = {},
    onAmountChange: (Long) -> Unit = {},
    onNavigateToExpenseItems: (() -> Unit)? = null,
    onContactSelect: (String) -> Unit = {},
    onDueDateChange: (Long) -> Unit = {},
    onNewQuickContactClick: () -> Unit = {},
    onExpenseTypeChange: (com.platform.app.domain.model.BillType) -> Unit = {},
    onInstallmentsCountChange: (Int) -> Unit = {},
    onRecurrenceFrequencyChange: (com.platform.app.domain.model.RecurrenceFrequency) -> Unit = {},
    onRecurrenceEndTypeChange: (com.platform.app.domain.model.RecurrenceEndType) -> Unit = {},
    onRecurrenceEndDateChange: (Long) -> Unit = {},
    onRecurrenceOccurrencesCountChange: (Int) -> Unit = {},
    onToggleCreditCard: (Boolean) -> Unit = {},
    onCreditCardSelect: (String) -> Unit = {},
    onPaymentMethodSelect: (String) -> Unit = {},
    onFinancialAccountSelect: (String) -> Unit = {},
    onPaymentStatusChange: (Boolean) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
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
        bottomBar = {
            PlatformSurface(
                variant = PlatformSurfaceVariant.Tonal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.spacingNormal, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!uiState.isValid && uiState.validationError != null) {
                        Text(
                            text = uiState.validationError ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Button(
                        onClick = onSaveClick,
                        enabled = uiState.isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = PlatformShapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Salvar Despesa",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
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
            // 1. VALOR & ITEM (PROTAGONISTA)
            ExpenseItemAndAmountCard(
                uiState = uiState,
                onItemSelect = onItemSelect,
                onAmountChange = onAmountChange,
                onNavigateToExpenseItems = onNavigateToExpenseItems
            )

            // 2. VENCIMENTO & DESTINATÁRIO
            ExpenseRecipientAndDueDateCard(
                uiState = uiState,
                onContactSelect = onContactSelect,
                onDueDateChange = onDueDateChange,
                onNewQuickContactClick = onNewQuickContactClick
            )

            // 3. TIPO DE COMPROMISSO
            ExpenseCommitmentTypeCard(
                uiState = uiState,
                onExpenseTypeChange = onExpenseTypeChange,
                onInstallmentsCountChange = onInstallmentsCountChange,
                onRecurrenceFrequencyChange = onRecurrenceFrequencyChange,
                onRecurrenceEndTypeChange = onRecurrenceEndTypeChange,
                onRecurrenceEndDateChange = onRecurrenceEndDateChange,
                onRecurrenceOccurrencesCountChange = onRecurrenceOccurrencesCountChange
            )

            // 4. COMO VOCÊ PAGA
            ExpensePaymentMethodCard(
                uiState = uiState,
                onToggleCreditCard = onToggleCreditCard,
                onCreditCardSelect = onCreditCardSelect,
                onPaymentMethodSelect = onPaymentMethodSelect,
                onFinancialAccountSelect = onFinancialAccountSelect
            )

            // 5. SITUAÇÃO DA CONTA
            ExpensePaymentStatusCard(
                isPaid = uiState.isPaid,
                onPaymentStatusChange = onPaymentStatusChange
            )

            // 6. OBSERVAÇÕES COMPLEMENTARES (OPCIONAL)
            PlatformCard(
                shape = PlatformShapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingNormal),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Observações Adicionais (Opcional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text(AppStrings.ExpenseLabels.SPECIFIC_DESCRIPTION) },
                        placeholder = { Text(AppStrings.ExpenseLabels.SPECIFIC_DESCRIPTION_PLACEHOLDER) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        supportingText = {
                            Text(
                                text = "Opcional. Padrão: ${uiState.selectedItem?.name ?: "Nome do item selecionado"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = PlatformShapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
