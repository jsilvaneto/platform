package com.platform.app.presentation.creditcards

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
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToNewExpense: () -> Unit = {}
) {
    var showAddCardSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Cartões de Crédito",
                subtitle = "Gestão de Limites e Faturas",
                onOpenDrawer = onOpenDrawer
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
                    contentPadding = PaddingValues(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingMedium),
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
                                shape = RoundedCornerShape(Dimens.cardCornerRadius)
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
                                        text = "Toque no botão + para adicionar o seu primeiro cartão.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                shape = RoundedCornerShape(Dimens.cardCornerRadius)
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
                                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
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
                                    shape = RoundedCornerShape(Dimens.cardCornerRadius)
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
                        Text("Cancelar")
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

@Composable
fun CreditCardItemCard(
    summary: CreditCardWithInvoiceSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val card = summary.card
    val cardColor = try { Color(card.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    val usageRatio = if (card.totalLimitCents > 0) {
        (summary.usedLimitCents.toFloat() / card.totalLimitCents.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        color = cardColor,
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(Dimens.cardCornerRadius))
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Limite Disponível",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(summary.availableLimitCents),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Barra de progresso de uso do limite
                LinearProgressIndicator(
                    progress = { usageRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (usageRatio > 0.85f) UrgentRed else MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Usado: ${CurrencyUtils.formatCentsToCurrency(summary.usedLimitCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Text(
                    text = "Total: ${CurrencyUtils.formatCentsToCurrency(card.totalLimitCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun InvoiceItemCard(
    invoice: CreditCardInvoice,
    onViewDetails: () -> Unit,
    onPay: () -> Unit
) {
    val statusLabel = when (invoice.status) {
        InvoiceStatus.ABERTA -> "Aberta"
        InvoiceStatus.FECHADA -> "Fechada"
        InvoiceStatus.PAGA -> "Paga"
    }
    val chipType = when (invoice.status) {
        InvoiceStatus.ABERTA -> StatusChipType.INFO
        InvoiceStatus.FECHADA -> StatusChipType.WARNING
        InvoiceStatus.PAGA -> StatusChipType.SUCCESS
    }

    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fatura ${invoice.referenceMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                PlatformStatusChip(text = statusLabel, type = chipType)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vencimento: ${DateUtils.formatDate(invoice.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Fechamento: ${DateUtils.formatDate(invoice.closingDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = CurrencyUtils.formatCentsToCurrency(invoice.totalAmountCents),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Compras", style = MaterialTheme.typography.labelMedium)
                }

                if (invoice.status != InvoiceStatus.PAGA) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onPay,
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Liquidar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormBottomSheet(
    cardToEdit: CreditCard?,
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit
) {
    val isEdit = cardToEdit != null
    var name by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var limitText by remember { mutableStateOf(if (cardToEdit != null) cardToEdit.totalLimitCents.toString() else "") }
    var closingDayText by remember { mutableStateOf(if (cardToEdit != null) cardToEdit.closingDay.toString() else "25") }
    var dueDayText by remember { mutableStateOf(if (cardToEdit != null) cardToEdit.dueDay.toString() else "5") }
    var selectedColor by remember { mutableStateOf(cardToEdit?.colorHex ?: "#3B82F6") }

    val presetColors = listOf(
        "#3B82F6", "#8B5CF6", "#EC4899", "#EF4444", "#F59E0B",
        "#10B981", "#14B8A6", "#06B6D4", "#6366F1", "#1E293B"
    )

    val limitCents = limitText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val closing = closingDayText.toIntOrNull() ?: 0
    val due = dueDayText.toIntOrNull() ?: 0
    val validation = CreditCardCalculator.validateCard(name, limitCents, closing, due)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEdit) "Editar Cartão de Crédito" else "Novo Cartão de Crédito",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Nome
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Cartão *") },
                placeholder = { Text("Ex: Nubank Mastercard, Unique Visa") },
                isError = validation.nameError != null && name.isNotBlank(),
                supportingText = if (validation.nameError != null && name.isNotBlank()) {
                    { Text(validation.nameError, color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Limite Total
            OutlinedTextField(
                value = CurrencyUtils.formatCentsToCurrency(limitCents),
                onValueChange = { input ->
                    limitText = input.filter { it.isDigit() }
                },
                label = { Text("Limite Total *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dias de Corte e Vencimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
            ) {
                OutlinedTextField(
                    value = closingDayText,
                    onValueChange = { if (it.length <= 2) closingDayText = it.filter { char -> char.isDigit() } },
                    label = { Text("Dia Corte (1-31) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = validation.closingDayError != null,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { if (it.length <= 2) dueDayText = it.filter { char -> char.isDigit() } },
                    label = { Text("Dia Venc. (1-31) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = validation.dueDayError != null,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Seletor de Cores
            Text(
                text = "Cor do Cartão:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetColors) { hex ->
                    val color = try { Color(hex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .clickable { selectedColor = hex }
                            .then(
                                if (selectedColor == hex) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == hex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                Button(
                    onClick = {
                        if (validation.isValid) {
                            onSave(
                                CreditCard(
                                    id = cardToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    totalLimitCents = limitCents,
                                    closingDay = closing,
                                    dueDay = due,
                                    colorHex = selectedColor
                                )
                            )
                        }
                    },
                    enabled = validation.isValid
                ) {
                    Text(if (isEdit) "Salvar Alterações" else "Cadastrar Cartão")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailsBottomSheet(
    invoice: CreditCardInvoice,
    installments: List<BillInstallment>,
    onDismiss: () -> Unit,
    onPay: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fatura ${invoice.referenceMonth}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Vencimento: ${DateUtils.formatDate(invoice.dueDate)} • Corte: ${DateUtils.formatDate(invoice.closingDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            // Total da Fatura Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Valor Total da Fatura",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(invoice.totalAmountCents),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    val statusLabel = when (invoice.status) {
                        InvoiceStatus.ABERTA -> "Aberta"
                        InvoiceStatus.FECHADA -> "Fechada"
                        InvoiceStatus.PAGA -> "Paga"
                    }
                    val chipType = when (invoice.status) {
                        InvoiceStatus.ABERTA -> StatusChipType.INFO
                        InvoiceStatus.FECHADA -> StatusChipType.WARNING
                        InvoiceStatus.PAGA -> StatusChipType.SUCCESS
                    }
                    PlatformStatusChip(text = statusLabel, type = chipType)
                }
            }

            // Lista de Compras nesta Fatura
            Text(
                text = "Lançamentos e Compras (${installments.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (installments.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhum lançamento registrado nesta fatura.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installments, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.billTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.categoryName}${item.itemName?.let { " • $it" } ?: ""} • Parcela ${item.installmentNumber}/${item.totalInstallments}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = CurrencyUtils.formatCentsToCurrency(item.amountCents),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Botão Liquidar se não estiver paga
            if (invoice.status != InvoiceStatus.PAGA) {
                Button(
                    onClick = {
                        onPay()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Liquidar Fatura Agora", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
