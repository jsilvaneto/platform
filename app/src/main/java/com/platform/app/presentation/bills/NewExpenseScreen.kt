package com.platform.app.presentation.bills

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            // 1. ITEM DE DESPESA E VALOR (OBRIGATÓRIO & PROTAGONISTA)
            ExpenseItemAndAmountCard(
                uiState = uiState,
                onItemSelect = viewModel::onItemSelect,
                onAmountChange = viewModel::onAmountChange,
                onNavigateToExpenseItems = onNavigateToExpenseItems
            )

            // 2. DESTINATÁRIO E VENCIMENTO (OBRIGATÓRIO)
            ExpenseRecipientAndDueDateCard(
                uiState = uiState,
                onContactSelect = viewModel::onContactSelect,
                onDueDateChange = viewModel::onDueDateChange,
                onNewQuickContactClick = { showQuickContactDialog = true }
            )

            // 3. SITUAÇÃO DA CONTA
            ExpensePaymentStatusCard(
                isPaid = uiState.isPaid,
                onPaymentStatusChange = viewModel::onPaymentStatusChange
            )

            // 4. MODALIDADE: À VISTA, PARCELADO OU RECORRENTE
            ExpenseCommitmentTypeCard(
                uiState = uiState,
                onExpenseTypeChange = viewModel::onExpenseTypeChange,
                onInstallmentsCountChange = viewModel::onInstallmentsCountChange,
                onRecurrenceFrequencyChange = viewModel::onRecurrenceFrequencyChange,
                onRecurrenceEndTypeChange = viewModel::onRecurrenceEndTypeChange,
                onRecurrenceEndDateChange = viewModel::onRecurrenceEndDateChange,
                onRecurrenceOccurrencesCountChange = viewModel::onRecurrenceOccurrencesCountChange
            )

            // 5. MEIO DE PAGAMENTO
            ExpensePaymentMethodCard(
                uiState = uiState,
                onToggleCreditCard = viewModel::onToggleCreditCard,
                onCreditCardSelect = viewModel::onCreditCardSelect,
                onPaymentMethodSelect = viewModel::onPaymentMethodSelect,
                onFinancialAccountSelect = viewModel::onFinancialAccountSelect
            )

            // 6. OBSERVAÇÕES COMPLEMENTARES (OPCIONAL)
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
                        text = "5. Observações Adicionais (Opcional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text("Descrição / Detalhe Específico") },
                        placeholder = { Text("Ex: Compras no Carrefour, troca de filtro") },
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
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        modifier = Modifier.fillMaxWidth()
                    )
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
