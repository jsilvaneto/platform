package com.platform.app.presentation.recurring.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.recurring.BillWithInstallments
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@Composable
fun RecurringBillCard(
    item: BillWithInstallments,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bill = item.bill

    PlatformCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = PlatformShapes.large
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Ícone, Título, Contato e Valor Recorrente
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Contato em destaque
                        if (!item.contactName.isNullOrBlank()) {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.contactName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Categoria
                        if (item.categoryName.isNotBlank() && item.categoryName != "Geral") {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Status Pausada
                        if (item.isPaused) {
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = WarningAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Pausada",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Coluna de Valor: Mensalidade Regular
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(item.regularAmountCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/mês",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Banner explicativo se houver primeira parcela diferenciada
            if (item.hasVariableFirstInstallment) {
                Spacer(modifier = Modifier.height(10.dp))
                val isFirstPending = item.installments.firstOrNull()?.isPaid == false
                Surface(
                    shape = PlatformShapes.small,
                    color = if (isFirstPending) WarningAmber.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFirstPending) Icons.Default.Info else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isFirstPending) WarningAmber else SuccessGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFirstPending) {
                                "1ª parcela (Entrada/Adesão): ${CurrencyUtils.formatCentsToCurrency(item.firstInstallmentAmountCents)} • Demais parcelas: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)}/mês"
                            } else {
                                "1ª parcela (${CurrencyUtils.formatCentsToCurrency(item.firstInstallmentAmountCents)}) já quitada • Mensalidade: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents)}/mês"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isFirstPending) MaterialTheme.colorScheme.onSurface else SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Próximo Débito e Projeção Anual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.nextInstallment?.let { next ->
                    val isNextToday = DateUtils.isToday(next.dueDate)
                    val isNextOverdue = next.dueDate < System.currentTimeMillis() && !isNextToday
                    Surface(
                        shape = PlatformShapes.extraSmall,
                        color = when {
                            isNextOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            isNextToday -> WarningAmber.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ) {
                        Text(
                            text = when {
                                isNextOverdue -> "Atrasado: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)} em ${DateUtils.formatDate(next.dueDate)}"
                                isNextToday -> "Débito hoje: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)}"
                                else -> "Próximo débito: ${CurrencyUtils.formatCentsToCurrency(next.amountCents)} em ${DateUtils.formatDate(next.dueDate)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isNextOverdue -> MaterialTheme.colorScheme.error
                                isNextToday -> WarningAmber
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } ?: run {
                    Text(
                        text = "Todas as cobranças quitadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Projeção: ${CurrencyUtils.formatCentsToCurrency(item.regularAmountCents * 12)}/ano",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
