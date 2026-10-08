package com.platform.app.presentation.bills.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.presentation.bills.NewExpenseUiState
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensePaymentMethodCard(
    uiState: NewExpenseUiState,
    onToggleCreditCard: (Boolean) -> Unit,
    onCreditCardSelect: (String) -> Unit,
    onPaymentMethodSelect: (String) -> Unit,
    onFinancialAccountSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var cardDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Meio de Pagamento",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Toggle: Cartão de Crédito?
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (uiState.isCreditCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pagar com Cartão de Crédito",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Switch(
                    checked = uiState.isCreditCard,
                    onCheckedChange = onToggleCreditCard,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            // Se CARTÃO DE CRÉDITO selecionado:
            if (uiState.isCreditCard) {
                ExposedDropdownMenuBox(
                    expanded = cardDropdownExpanded,
                    onExpandedChange = { cardDropdownExpanded = !cardDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedCreditCard?.let { card ->
                            val avail = uiState.availableLimitForSelectedCard ?: 0L
                            "${card.name} (Disp: ${CurrencyUtils.formatCentsToCurrency(avail)})"
                        } ?: "Selecione o Cartão de Crédito",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cartão de Crédito *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardDropdownExpanded) },
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cardDropdownExpanded,
                        onDismissRequest = { cardDropdownExpanded = false }
                    ) {
                        for (card in uiState.creditCards) {
                            val avail = uiState.creditCardSummaries[card.id] ?: card.totalLimitCents
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(card.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Limite Disp: ${CurrencyUtils.formatCentsToCurrency(avail)} • Corte dia ${card.closingDay}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onCreditCardSelect(card.id)
                                    cardDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Alerta se excede o limite
                if (uiState.isExceedingCreditLimit) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarningAmber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, WarningAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aviso: O valor da despesa excede o limite disponível deste cartão.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Se NÃO FOR CARTÃO: Seletor de Forma de Pagamento e Conta
                ExposedDropdownMenuBox(
                    expanded = methodDropdownExpanded,
                    onExpandedChange = { methodDropdownExpanded = !methodDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedPaymentMethod?.name ?: "Selecione a Forma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Forma de Pagamento (PIX, Boleto, etc.)") },
                        leadingIcon = uiState.selectedPaymentMethod?.let { pm ->
                            {
                                Icon(
                                    imageVector = PlatformIconCatalog.getIcon(pm.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = methodDropdownExpanded,
                        onDismissRequest = { methodDropdownExpanded = false }
                    ) {
                        for (pm in uiState.paymentMethods) {
                            DropdownMenuItem(
                                text = { Text(pm.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = PlatformIconCatalog.getIcon(pm.iconName),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    onPaymentMethodSelect(pm.id)
                                    methodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedFinancialAccount?.name ?: "Selecione a Conta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conta Financeira / Banco") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        for (acc in uiState.financialAccounts) {
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    onFinancialAccountSelect(acc.id)
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
