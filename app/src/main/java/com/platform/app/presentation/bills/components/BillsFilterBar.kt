package com.platform.app.presentation.bills.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.bills.BillPeriodFilter
import com.platform.app.presentation.components.PlatformNeutralChip

/**
 * Barra de chips de filtros neutros para a tela de Contas (Prompt 60):
 * - Chips de filtro: mês e tipo; os demais em bottom sheet.
 * - Chips com preenchimento e cores estritamente neutras.
 */
@Composable
fun BillsFilterBar(
    periodFilter: BillPeriodFilter,
    typeFilter: BillType?,
    statusFilter: BillStatus?,
    hasActiveFilters: Boolean,
    onOpenFilterBottomSheet: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Chip de Mês / Período
        item {
            val monthLabel = if (periodFilter != BillPeriodFilter.ALL) periodFilter.label else "Mês"
            PlatformNeutralChip(
                text = monthLabel,
                selected = periodFilter != BillPeriodFilter.ALL,
                onClick = onOpenFilterBottomSheet
            )
        }

        // 2. Chip de Tipo (Avulsa, Parcelada, Recorrente)
        item {
            val typeLabel = when (typeFilter) {
                BillType.SINGLE -> "Avulsa"
                BillType.INSTALLMENT -> "Parcelada"
                BillType.RECURRING -> "Recorrente"
                null -> "Tipo"
            }
            PlatformNeutralChip(
                text = typeLabel,
                selected = typeFilter != null,
                onClick = onOpenFilterBottomSheet
            )
        }

        // 3. Chip de Status (Pagas, A Pagar)
        item {
            val statusLabel = when (statusFilter) {
                BillStatus.PAID -> "Pagas"
                BillStatus.PENDING -> "A Pagar"
                else -> "Status"
            }
            PlatformNeutralChip(
                text = statusLabel,
                selected = statusFilter != null,
                onClick = onOpenFilterBottomSheet
            )
        }

        // 4. Chip "+ Filtros" para abrir os demais em bottom sheet
        item {
            PlatformNeutralChip(
                text = "Filtros",
                selected = hasActiveFilters,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                },
                onClick = onOpenFilterBottomSheet
            )
        }

        // 5. Botão Limpar Filtros
        if (hasActiveFilters) {
            item {
                PlatformNeutralChip(
                    text = "Limpar",
                    onClick = onResetFilters,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                )
            }
        }
    }
}
