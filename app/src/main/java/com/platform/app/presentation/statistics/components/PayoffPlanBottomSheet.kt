package com.platform.app.presentation.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.platform.app.domain.usecase.CalculateDebtPayoffPlanUseCase
import com.platform.app.domain.usecase.DebtItem
import com.platform.app.domain.usecase.DebtPayoffStrategy
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformSegmentedTabs
import com.platform.app.presentation.components.SegmentedTabItem
import com.platform.app.presentation.theme.PlatformShapes

/**
 * Bottom sheet do simulador do Plano de Quitação de Dívidas (Prompt 62):
 * - Estratégias Bola de Neve (Snowball) e Avalanche.
 * - Visual minimalista e sem elevação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoffPlanBottomSheet(
    debts: List<DebtItem> = emptyList(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var strategy by remember { mutableStateOf(DebtPayoffStrategy.SNOWBALL) }

    val calculator = remember { CalculateDebtPayoffPlanUseCase() }
    val plan = remember(debts, strategy) {
        calculator.calculate(
            debts = debts,
            extraMonthlyAmountCents = 0L,
            strategy = strategy
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = PlatformShapes.bottomSheet,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Plano de Quitação",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Estratégia para zerar compras parceladas",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Alternância de Estratégia
            PlatformSegmentedTabs(
                items = listOf(
                    SegmentedTabItem(title = "Bola de Neve"),
                    SegmentedTabItem(title = "Avalanche")
                ),
                selectedIndex = if (strategy == DebtPayoffStrategy.SNOWBALL) 0 else 1,
                onTabSelected = { index ->
                    strategy = if (index == 0) DebtPayoffStrategy.SNOWBALL else DebtPayoffStrategy.AVALANCHE
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Resumo da Estratégia
            PlatformCard(
                shape = PlatformShapes.medium
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (strategy == DebtPayoffStrategy.SNOWBALL)
                            "Foco comportamental: quita primeiro as dívidas de menor valor para gerar vitórias rápidas e liberar margem mensal."
                        else
                            "Foco matemático: prioriza as dívidas de maior impacto para reduzir o tempo total de comprometimento.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tempo estimado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (plan.totalMonthsToDebtFree > 0) "${plan.totalMonthsToDebtFree} meses" else "Quitado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total em aberto",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PlatformMoneyText(
                                amountCents = plan.initialTotalDebtCents,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Ordem de Eliminação
            Text(
                text = "Ordem sugerida de quitação",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (debts.isEmpty()) {
                Text(
                    text = "Nenhuma dívida ou parcelamento em aberto no momento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(debts) { debt ->
                        PlatformCard(
                            shape = PlatformShapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = debt.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                PlatformMoneyText(
                                    amountCents = debt.totalRemainingAmountCents,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
