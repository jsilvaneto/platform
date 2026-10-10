package com.platform.app.presentation.recurring.components

import com.platform.app.presentation.theme.PlatformShapes

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@Composable
fun InstallmentRow(
    installment: BillInstallment,
    isVariableFirst: Boolean = false,
    onTogglePayment: () -> Unit,
    onOpenAdjust: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isToday = !installment.isPaid && !installment.isPaused && DateUtils.isToday(installment.dueDate)
    val isOverdue = !installment.isPaid && !installment.isPaused && installment.dueDate < System.currentTimeMillis() && !isToday

    val rowTitle = when (installment.type) {
        BillType.RECURRING -> if (isVariableFirst && installment.installmentNumber == 1) {
            "1ª Cobrança (Adesão/Entrada)"
        } else {
            "Cobrança ${installment.installmentNumber}"
        }
        else -> "Parcela ${installment.installmentNumber}"
    }

    Surface(
        shape = PlatformShapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onTogglePayment,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (installment.isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (installment.isPaid) "Marcar como pendente" else "Marcar como paga",
                        tint = when {
                            installment.isPaid -> SuccessGreen
                            installment.isPaused -> WarningAmber
                            isOverdue -> MaterialTheme.colorScheme.error
                            isToday -> WarningAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = rowTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isVariableFirst && installment.installmentNumber == 1 && installment.type == BillType.RECURRING) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = PlatformShapes.extraSmall,
                                color = WarningAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Entrada",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = when {
                            installment.isPaid -> AppStrings.Status.PAID
                            installment.isPaused -> "Cobrança Pausada"
                            isOverdue -> "Venceu ${DateUtils.formatDate(installment.dueDate)}"
                            isToday -> "Vence hoje"
                            else -> "Vence ${DateUtils.formatDate(installment.dueDate)}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isOverdue || isToday || installment.isPaused) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            installment.isPaid -> SuccessGreen
                            installment.isPaused -> WarningAmber
                            isOverdue -> MaterialTheme.colorScheme.error
                            isToday -> WarningAmber
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (installment.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )

                if (onOpenAdjust != null) {
                    IconButton(
                        onClick = onOpenAdjust,
                        modifier = Modifier.size(32.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ajustar parcela",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
