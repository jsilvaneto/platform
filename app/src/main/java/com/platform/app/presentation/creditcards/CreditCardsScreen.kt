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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsScreen(
    uiState: CreditCardsUiState,
    onAction: (CreditCardsUiAction) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
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
                    // Carrossel de Cartões no Topo
                    item {
                        Text(
                            text = "Meus Cartões",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingSmall))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                        ) {
                            items(uiState.cardsWithSummary, key = { it.card.id }) { summary ->
                                CreditCardItemCard(
                                    summary = summary,
                                    isSelected = summary.card.id == uiState.selectedCardId,
                                    onClick = { onAction(CreditCardsUiAction.SelectCard(summary.card.id)) }
                                )
                            }
                        }
                    }

                    // Detalhe da Fatura Selecionada
                    val selectedCardSummary = uiState.cardsWithSummary.find { it.card.id == uiState.selectedCardId }
                    if (selectedCardSummary != null) {
                        item {
                            Spacer(modifier = Modifier.height(Dimens.spacingSmall))
                            Text(
                                text = "Fatura Atual • ${selectedCardSummary.card.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        item {
                            PlatformCard(
                                shape = RoundedCornerShape(Dimens.cardCornerRadius)
                            ) {
                                Column(modifier = Modifier.padding(Dimens.spacingNormal)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Valor Consumido",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = CurrencyUtils.formatCentsToCurrency(selectedCardSummary.usedLimitCents),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        PlatformStatusChip(
                                            text = "Fecha dia ${selectedCardSummary.card.closingDay}",
                                            type = StatusChipType.INFO
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Dimens.spacingMedium))

                                    Button(
                                        onClick = {
                                            uiState.currentInvoice?.id?.let {
                                                onAction(CreditCardsUiAction.PayInvoice(it))
                                            }
                                        },
                                        enabled = uiState.currentInvoice != null && selectedCardSummary.usedLimitCents > 0L,
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                                        Text(
                                            text = "Liquidar Fatura",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddCardSheet) {
            AddCreditCardBottomSheet(
                onDismiss = { showAddCardSheet = false },
                onSave = { newCard ->
                    onAction(CreditCardsUiAction.SaveCard(newCard))
                    showAddCardSheet = false
                }
            )
        }
    }
}

@Composable
fun CreditCardItemCard(
    summary: CreditCardWithInvoiceSummary,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val card = summary.card
    val cardColor = try { Color(card.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Surface(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        color = cardColor,
        modifier = Modifier
            .width(260.dp)
            .height(150.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(Dimens.cardCornerRadius))
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = "Limite Disponível",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(summary.availableLimitCents),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Limite Total: ${CurrencyUtils.formatCentsToCurrency(card.totalLimitCents)} • Venc. dia ${card.dueDay}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardBottomSheet(
    onDismiss: () -> Unit,
    onSave: (CreditCard) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }
    var closingDayText by remember { mutableStateOf("25") }
    var dueDayText by remember { mutableStateOf("5") }
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
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            Text(
                text = "Novo Cartão de Crédito",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Cartão (ex: Nubank, Unique)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = limitText,
                onValueChange = { limitText = it },
                label = { Text("Limite Total em Centavos (ex: 500000 para R$ 5.000,00)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
            ) {
                OutlinedTextField(
                    value = closingDayText,
                    onValueChange = { closingDayText = it },
                    label = { Text("Dia Fechamento") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { dueDayText = it },
                    label = { Text("Dia Vencimento") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
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
                        val limitCents = limitText.toLongOrNull() ?: 0L
                        val closing = closingDayText.toIntOrNull() ?: 25
                        val due = dueDayText.toIntOrNull() ?: 5
                        if (name.isNotBlank() && limitCents > 0L) {
                            onSave(
                                CreditCard(
                                    name = name.trim(),
                                    totalLimitCents = limitCents,
                                    closingDay = closing,
                                    dueDay = due
                                )
                            )
                        }
                    },
                    enabled = name.isNotBlank() && (limitText.toLongOrNull() ?: 0L) > 0L
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}
