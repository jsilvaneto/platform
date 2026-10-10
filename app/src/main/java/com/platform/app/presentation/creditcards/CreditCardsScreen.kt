package com.platform.app.presentation.creditcards

import com.platform.app.presentation.theme.PlatformShapes

import com.platform.app.presentation.creditcards.components.CreditCardItemCard
import com.platform.app.presentation.creditcards.components.InvoiceItemCard

import com.platform.app.presentation.creditcards.components.CardFormBottomSheet
import com.platform.app.presentation.creditcards.components.InvoiceDetailsBottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformCreditCardView
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed
import com.platform.app.presentation.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsScreen(
    uiState: CreditCardsUiState,
    onAction: (CreditCardsUiAction) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateToNewExpense: () -> Unit = {}
) {
    var showAddCardSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = AppStrings.Navigation.CREDIT_CARDS,
                subtitle = "Limites e Faturas",
                onOpenDrawer = null
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Cartão")
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
                    contentPadding = PaddingValues(start = Dimens.spacingNormal, end = Dimens.spacingNormal, top = Dimens.spacingMedium, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                ) {
                    // 1. CARROSSEL DE CARTÕES
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Meus Cartões",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.cardsWithSummary.size} cadastrado(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacingSmall))

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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Nenhum cartão cadastrado",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cadastre seu cartão de crédito para gerenciar limites e faturas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showAddCardSheet = true },
                                        shape = PlatformShapes.medium
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cadastrar primeiro cartão")
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                            ) {
                                items(uiState.cardsWithSummary, key = { it.card.id }) { summary ->
                                    PlatformCreditCardView(
                                        summary = summary,
                                        isSelected = summary.card.id == (uiState.selectedCardId ?: uiState.cardsWithSummary.firstOrNull()?.card?.id),
                                        onClick = { onAction(CreditCardsUiAction.SelectCard(summary.card.id)) },
                                        onEdit = { onAction(CreditCardsUiAction.OpenEditCard(summary.card)) },
                                        onDelete = { onAction(CreditCardsUiAction.RequestDeleteCard(summary.card.id)) }
                                    )
                                }
                            }
                        }
                    }

                    // 2. EXTRATO DE FATURAS DO CARTÃO SELECIONADO
                    val selectedCardSummary = uiState.selectedCardSummary
                    if (selectedCardSummary != null) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

                            // Card Executivo do Ciclo: Melhor Dia de Compra & Atalho de Lançamento
                            PlatformCard(
                                shape = PlatformShapes.large
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(WarningAmber.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = null,
                                                    tint = WarningAmber,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Melhor Dia: dia ${selectedCardSummary.card.closingDay}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Compras a partir deste dia caem na próxima fatura",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = onNavigateToNewExpense,
                                        shape = PlatformShapes.medium,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Lançar Despesa com ${selectedCardSummary.card.name}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimens.spacingMedium))

                            // Seletor de Faturas em Segmented Tabs
                            val allInvoices = uiState.invoicesForSelectedCard
                            val openCount = allInvoices.count { it.status == InvoiceStatus.ABERTA }
                            val closedCount = allInvoices.count { it.status == InvoiceStatus.FECHADA }
                            val paidCount = allInvoices.count { it.status == InvoiceStatus.PAGA }

                            val invoiceTabItems = listOf(
                                SegmentedTabItem("Aberta", openCount),
                                SegmentedTabItem("Fechadas", closedCount),
                                SegmentedTabItem("Pagas", paidCount),
                                SegmentedTabItem("Todas", allInvoices.size)
                            )

                            val selectedInvoiceTabIndex = when (uiState.invoiceFilter) {
                                InvoiceFilter.OPEN -> 0
                                InvoiceFilter.CLOSED -> 1
                                InvoiceFilter.PAID -> 2
                                InvoiceFilter.ALL -> 3
                            }

                            PlatformSegmentedTabs(
                                items = invoiceTabItems,
                                selectedIndex = selectedInvoiceTabIndex,
                                onTabSelected = { index ->
                                    when (index) {
                                        0 -> onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.OPEN))
                                        1 -> onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.CLOSED))
                                        2 -> onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.PAID))
                                        3 -> onAction(CreditCardsUiAction.SetInvoiceFilter(InvoiceFilter.ALL))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        val invoices = uiState.filteredInvoices
                        if (invoices.isEmpty()) {
                            item {
                                PlatformCard(
                                    shape = PlatformShapes.large
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Dimens.spacingLarge),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Nenhuma fatura encontrada neste filtro.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(invoices, key = { it.id }) { invoice ->
                                InvoiceItemCard(
                                    invoice = invoice,
                                    onViewDetails = { onAction(CreditCardsUiAction.SelectInvoiceForDetails(invoice)) },
                                    onPay = { onAction(CreditCardsUiAction.PayInvoice(invoice.id)) }
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
            AlertDialog(
                onDismissRequest = { onAction(CreditCardsUiAction.CancelDeleteCard) },
                title = { Text("Excluir Cartão de Crédito") },
                text = {
                    if (deps.hasActiveDependencies) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Atenção: Cartão em uso!",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
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
                        Text("Tem certeza de que deseja excluir este cartão de crédito?")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onAction(CreditCardsUiAction.ConfirmDeleteCard) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sim, Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(CreditCardsUiAction.CancelDeleteCard) }) {
                        Text(AppStrings.Actions.CANCEL)
                    }
                }
            )
        }

        // BOTTOM SHEET: DETALHES E EXTRATO DA FATURA
        if (uiState.selectedInvoiceForDetails != null) {
            InvoiceDetailsBottomSheet(
                invoice = uiState.selectedInvoiceForDetails,
                installments = uiState.installmentsForSelectedInvoice,
                onDismiss = { onAction(CreditCardsUiAction.SelectInvoiceForDetails(null)) },
                onPay = {
                    onAction(CreditCardsUiAction.PayInvoice(uiState.selectedInvoiceForDetails.id))
                }
            )
        }
    }
}
