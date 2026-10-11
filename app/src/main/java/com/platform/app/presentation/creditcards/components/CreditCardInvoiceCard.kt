package com.platform.app.presentation.creditcards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.PlatformShapes

/**
 * Card de fatura com total em 30sp e chip de status semafórico (Prompt 61):
 * - Aberta e Fechada: âmbar (WARNING)
 * - Vencida: vermelho (ERROR)
 * - Paga: verde (SUCCESS)
 */
@Composable
fun CreditCardInvoiceCard(
    invoice: CreditCardInvoice,
    modifier: Modifier = Modifier
) {
    val currentTime = System.currentTimeMillis()
    val isOverdue = invoice.status != InvoiceStatus.PAGA && invoice.dueDate < currentTime

    val (statusLabel, chipType) = when {
        invoice.status == InvoiceStatus.PAGA -> "Paga" to StatusChipType.SUCCESS
        isOverdue -> "Vencida" to StatusChipType.ERROR
        invoice.status == InvoiceStatus.FECHADA -> "Fechada" to StatusChipType.WARNING
        else -> "Aberta" to StatusChipType.WARNING
    }

    PlatformCard(
        shape = PlatformShapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Linha Superior: Mês de Referência + Chip de Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fatura ${invoice.referenceMonth}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                PlatformStatusChip(
                    text = statusLabel,
                    type = chipType
                )
            }

            // Total em 30sp tabular
            Column {
                Text(
                    text = "Valor Total",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                PlatformMoneyText(
                    amountCents = invoice.totalAmountCents,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Datas de Vencimento e Fechamento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vence em ${DateUtils.formatDate(invoice.dueDate)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Corte: ${DateUtils.formatDate(invoice.closingDate)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
