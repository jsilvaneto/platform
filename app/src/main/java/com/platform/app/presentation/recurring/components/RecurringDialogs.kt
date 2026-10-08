package com.platform.app.presentation.recurring.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.presentation.common.AppStrings
import java.util.Locale

@Composable
fun EmptyRecurringView(
    mode: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (mode) {
                        0 -> Icons.Default.CalendarMonth
                        1 -> Icons.Default.CreditCard
                        else -> Icons.Default.Autorenew
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when (mode) {
                    0 -> "Nenhum Pagamento Futuro"
                    1 -> "Nenhuma Compra Parcelada"
                    else -> "Nenhuma Assinatura Cadastrada"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (mode) {
                    0 -> "Todas as suas parcelas já foram quitadas ou ainda não há compromissos futuros programados."
                    1 -> "Cadastre compras parceladas para acompanhar o cronograma e amortização de débitos."
                    else -> "Cadastre despesas contínuas como streaming, condomínio e serviços recorrentes."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditBillMonthlyAmountDialog(
    currentAmountCents: Long,
    billTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (newAmountCents: Long) -> Unit
) {
    var amountText by remember {
        val initial = if (currentAmountCents > 0L) {
            String.format(Locale.getDefault(), "%.2f", currentAmountCents / 100.0).replace('.', ',')
        } else ""
        mutableStateOf(initial)
    }

    val parsedCents: Long? = remember(amountText) {
        amountText
            .replace(".", "")
            .replace(",", ".")
            .toDoubleOrNull()
            ?.let { (it * 100).toLong() }
            ?.takeIf { it > 0L }
    }

    val isValid = parsedCents != null && parsedCents > 0L
    val isUnchanged = parsedCents == currentAmountCents

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Alterar Valor Mensal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = billTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apenas as próximas cobranças pendentes serão atualizadas. Pagamentos já realizados não serão alterados.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (currentAmountCents > 0L) {
                    Text(
                        text = "Valor atual: ${CurrencyUtils.formatCentsToCurrency(currentAmountCents)}/mês",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == ',' || it == '.' }
                        amountText = filtered
                    },
                    label = { Text("Novo valor mensal (R$)") },
                    placeholder = { Text("Ex: 49,90") },
                    isError = amountText.isNotBlank() && !isValid,
                    supportingText = {
                        when {
                            amountText.isNotBlank() && !isValid ->
                                Text("Informe um valor válido", color = MaterialTheme.colorScheme.error)
                            isUnchanged && isValid ->
                                Text("Igual ao valor atual", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            isValid ->
                                Text(
                                    "Novo valor: ${CurrencyUtils.formatCentsToCurrency(parsedCents!!)}/mês",
                                    color = MaterialTheme.colorScheme.primary
                                )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { parsedCents?.let { onConfirm(it) } },
                enabled = isValid && !isUnchanged
            ) {
                Text("Atualizar Próximas Cobranças")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppStrings.Actions.CANCEL) }
        }
    )
}
