package com.platform.app.presentation.creditcards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformCreditCardView
import com.platform.app.presentation.components.PlatformListRow
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.components.PlatformPrimaryButton
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.creditcards.components.CanIBuyDialog
import com.platform.app.presentation.creditcards.components.CardFormBottomSheet
import com.platform.app.presentation.creditcards.components.CreditCardInvoiceCard
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

@Composable
fun CreditCardsScreen(
    uiState: CreditCardsUiState,
    onAction: (CreditCardsUiAction) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateToNewExpense: () -> Unit = {}
) {
    var showAddCardSheet by remember { mutableStateOf(false) }
    var showCanIBuyDialog by remember { mutableStateOf(false) }

    val selectedCardSummary = uiState.selectedCardSummary
    val activeInvoice = uiState.activeInvoice
    val canPay = activeInvoice != null && activeInvoice.status != InvoiceStatus.PAGA

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = AppStrings.Navigation.CREDIT_CARDS,
                subtitle = "Limites e Faturas",
                onOpenDrawer = onOpenDrawer,
                actions = {
                    IconButton(onClick = onNavigateToNewExpense) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Nova Despesa",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showAddCardSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar Cartão",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (selectedCardSummary != null) {
                PlatformSurface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão secundário "Posso comprar?"
                        OutlinedButton(
                            onClick = { showCanIBuyDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = PlatformShapes.medium,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.CreditCards.CAN_I_BUY_TITLE,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // Botão principal neutro "Pagar fatura"
                        PlatformPrimaryButton(
                            text = if (activeInvoice?.status == InvoiceStatus.PAGA) {
                                AppStrings.CreditCards.INVOICE_PAID
                            } else {
                                AppStrings.CreditCards.PAY_INVOICE
                            },
                            onClick = {
                                if (activeInvoice != null && canPay) {
                                    onAction(CreditCardsUiAction.PayInvoice(activeInvoice.id))
                                }
                            },
                            enabled = canPay,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                    }
                }
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
            if (uiState.isLoading && uiState.cardsWithSummary.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. CARROSSEL DE CARTÕES
                    item {
                        PlatformSectionTitle(
                            text = "${AppStrings.CreditCards.MY_CARDS} (${uiState.cardsWithSummary.size})"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.cardsWithSummary.isEmpty()) {
                            PlatformCard(
                                shape = PlatformShapes.large
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = AppStrings.CreditCards.NO_CARDS_REGISTERED,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = AppStrings.CreditCards.NO_CARDS_DESC,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showAddCardSheet = true },
                                        shape = PlatformShapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppStrings.CreditCards.REGISTER_FIRST_CARD)
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.cardsWithSummary, key = { it.card.id }) { summary ->
                                    val isSelected = summary.card.id == (uiState.selectedCardId ?: uiState.cardsWithSummary.firstOrNull()?.card?.id)
                                    PlatformCreditCardView(
                                        summary = summary,
                                        isSelected = isSelected,
                                        onClick = { onAction(CreditCardsUiAction.SelectCard(summary.card.id)) },
                                        onEdit = { onAction(CreditCardsUiAction.OpenEditCard(summary.card)) },
                                        onDelete = { onAction(CreditCardsUiAction.RequestDeleteCard(summary.card.id)) }
                                    )
                                }
                            }
                        }
                    }

                    // 2. SEÇÃO DE FATURAS E LANÇAMENTOS DO CARTÃO SELECIONADO
                    if (selectedCardSummary != null) {
                        item {
                            // Seletor de Faturas em Chips Neutros
                            val allInvoices = uiState.invoicesForSelectedCard
                            val openCount = allInvoices.count { it.status == InvoiceStatus.ABERTA }
                            val closedCount = allInvoices.count { it.status == InvoiceStatus.FECHADA }
                            val paidCount = allInvoices.count { it.status == InvoiceStatus.PAGA }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PlatformNeutralChip(
                                    text = "Aberta ($openCount)",
                                    selected = uiState.invoiceFilter == InvoiceFilter.OPEN,
                                    onClick = { onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.OPEN)) }
                                )
                                PlatformNeutralChip(
                                    text = "Fechadas ($closedCount)",
                                    selected = uiState.invoiceFilter == InvoiceFilter.CLOSED,
                                    onClick = { onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.CLOSED)) }
                                )
                                PlatformNeutralChip(
                                    text = "Pagas ($paidCount)",
                                    selected = uiState.invoiceFilter == InvoiceFilter.PAID,
                                    onClick = { onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.PAID)) }
                                )
                                PlatformNeutralChip(
                                    text = "Todas (${allInvoices.size})",
                                    selected = uiState.invoiceFilter == InvoiceFilter.ALL,
                                    onClick = { onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.ALL)) }
                                )
                            }
                        }

                        // Card da fatura ativa com total em 30sp
                        if (activeInvoice != null) {
                            item {
                                CreditCardInvoiceCard(invoice = activeInvoice)
                            }
                        }

                        // 3. LISTA DE LANÇAMENTOS NA FATURA
                        item {
                            PlatformSectionTitle(
                                text = "${AppStrings.CreditCards.INVOICE_INSTALLMENTS} (${uiState.installmentsForSelectedInvoice.size})"
                            )
                        }

                        val installments = uiState.installmentsForSelectedInvoice
                        if (installments.isEmpty()) {
                            item {
                                PlatformCard(
                                    shape = PlatformShapes.large
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = AppStrings.CreditCards.NO_INSTALLMENTS_ON_INVOICE,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(installments, key = { it.id }) { item ->
                                val subtitle = buildString {
                                    append(item.categoryName)
                                    if (!item.itemName.isNullOrBlank()) {
                                        append(" • ")
                                        append(item.itemName)
                                    }
                                    append(" • Parcela ")
                                    append(item.installmentNumber)
                                    append("/")
                                    append(item.totalInstallments)
                                }
                                PlatformListRow(
                                    title = item.billTitle,
                                    subtitle = subtitle,
                                    amountCents = item.amountCents,
                                    icon = Icons.Default.CreditCard
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM SHEET: ADICIONAR NOVO CARTÃO
        if (showAddCardSheet) {
            CardFormBottomSheet(
                cardToEdit = null,
                onDismiss = { showAddCardSheet = false },
                onSave = { newCard ->
                    onAction(CreditCardsUiAction.SaveCard(newCard))
                    showAddCardSheet = false
                }
            )
        }

        // BOTTOM SHEET: EDITAR CARTÃO
        if (uiState.cardToEdit != null) {
            CardFormBottomSheet(
                cardToEdit = uiState.cardToEdit,
                onDismiss = { onAction(CreditCardsUiAction.CloseEditCard) },
                onSave = { updatedCard ->
                    onAction(CreditCardsUiAction.SaveCard(updatedCard))
                }
            )
        }

        // DIÁLOGO: CONFIRMAÇÃO DE EXCLUSÃO COM DEPENDÊNCIAS
        if (uiState.pendingDeleteDependencies != null) {
            val deps = uiState.pendingDeleteDependencies
            val overdueColors = PlatformStatusStyle.getColors(PlatformStatusType.OVERDUE)
            val attentionColors = PlatformStatusStyle.getColors(PlatformStatusType.ATTENTION)

            AlertDialog(
                onDismissRequest = { onAction(CreditCardsUiAction.CancelDeleteCard) },
                title = { Text(AppStrings.CreditCards.DELETE_CARD_TITLE) },
                text = {
                    if (deps.hasActiveDependencies) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = attentionColors.contentColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.CreditCards.CARD_IN_USE_WARNING,
                                    fontWeight = FontWeight.Bold,
                                    color = overdueColors.contentColor
                                )
                            }
                            Text(
                                text = "Este cartão possui ${deps.invoiceCount} fatura(s) e ${deps.installmentCount} lançamento(s) registrados. Ao excluir, essas faturas e vínculos serão removidos.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Deseja realmente continuar com a exclusão?",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(AppStrings.CreditCards.DELETE_CARD_CONFIRM)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onAction(CreditCardsUiAction.ConfirmDeleteCard) },
                        colors = ButtonDefaults.buttonColors(containerColor = overdueColors.contentColor)
                    ) {
                        Text(AppStrings.CreditCardLabels.CONFIRM_DELETE_BUTTON)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(CreditCardsUiAction.CancelDeleteCard) }) {
                        Text(AppStrings.Actions.CANCEL)
                    }
                }
            )
        }

        // DIÁLOGO SIMULADOR: POSSO COMPRAR?
        if (showCanIBuyDialog && selectedCardSummary != null) {
            CanIBuyDialog(
                card = selectedCardSummary.card,
                availableLimitCents = selectedCardSummary.availableLimitCents,
                onDismiss = { showCanIBuyDialog = false }
            )
        }
    }
}
