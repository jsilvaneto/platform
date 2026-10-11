package com.platform.app.presentation.creditcards.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.CreditCard
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.PlatformSurfaceVariant
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

/**
 * Diálogo do simulador "Posso comprar?" para cartões de crédito.
 * Avalia em tempo real se o valor pretendido cabe no limite disponível do cartão,
 * calculando o impacto residual e a competência de cobrança.
 */
@Composable
fun CanIBuyDialog(
    card: CreditCard,
    availableLimitCents: Long,
    onDismiss: () -> Unit
) {
    var rawInput by remember { mutableStateOf("") }
    val parsedCents = remember(rawInput) {
        val clean = rawInput.replace("[^0-9]".toRegex(), "")
        clean.toLongOrNull() ?: 0L
    }

    val canAfford = parsedCents > 0L && parsedCents <= availableLimitCents
    val remainingLimit = if (canAfford) availableLimitCents - parsedCents else 0L
    val deficit = if (parsedCents > availableLimitCents) parsedCents - availableLimitCents else 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Posso comprar no ${card.name}?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Limite disponível: ${CurrencyUtils.formatCentsToCurrency(availableLimitCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = if (parsedCents == 0L && rawInput.isEmpty()) "" else CurrencyUtils.formatCentsToCurrency(parsedCents),
                    onValueChange = { input ->
                        val digits = input.replace("[^0-9]".toRegex(), "")
                        rawInput = digits
                    },
                    label = { Text(AppStrings.CreditCards.CAN_I_BUY_LABEL) },
                    placeholder = { Text(AppStrings.CreditCards.ZERO_CURRENCY_PLACEHOLDER) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (parsedCents > 0L) {
                    PlatformSurface(
                        variant = PlatformSurfaceVariant.Tonal,
                        shape = PlatformShapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val successColors = PlatformStatusStyle.getColors(PlatformStatusType.PAID)
                            val errorColors = PlatformStatusStyle.getColors(PlatformStatusType.OVERDUE)
                            val statusContentColor = if (canAfford) successColors.contentColor else errorColors.contentColor

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (canAfford) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = statusContentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (canAfford) "Cabe no seu limite!" else "Não cabe no seu limite!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusContentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (canAfford) {
                                Text(
                                    text = "Restará de limite: ${CurrencyUtils.formatCentsToCurrency(remainingLimit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Compras a partir do dia ${card.closingDay} entram na próxima fatura.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "Faltam ${CurrencyUtils.formatCentsToCurrency(deficit)} de limite para realizar esta compra.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.Actions.CLOSE)
            }
        }
    )
}
