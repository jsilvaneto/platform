package com.platform.app.presentation.bills.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.platform.app.domain.model.BillType
import com.platform.app.presentation.bills.BillPeriodFilter
import java.util.Calendar

@Composable
fun BillsFilterBar(
    selectedYear: Int?,
    availableYears: List<Int>,
    periodFilter: BillPeriodFilter,
    typeFilter: BillType?,
    onYearChange: (Int?) -> Unit,
    onPeriodChange: (BillPeriodFilter) -> Unit,
    onTypeChange: (BillType?) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var yearMenuExpanded by remember { mutableStateOf(false) }
    var periodMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val isYearFiltered = selectedYear != currentYear
    val isPeriodFiltered = periodFilter != BillPeriodFilter.ALL
    val isTypeFiltered = typeFilter != null
    val hasActiveFilters = isYearFiltered || isPeriodFiltered || isTypeFiltered

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Dropdown de Ano
        item {
            FilterDropdownChip(
                label = if (selectedYear != null) "$selectedYear" else "Ano: Todos",
                isSelected = isYearFiltered,
                onClick = { yearMenuExpanded = true }
            ) {
                DropdownMenu(
                    expanded = yearMenuExpanded,
                    onDismissRequest = { yearMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os Anos") },
                        onClick = {
                            onYearChange(null)
                            yearMenuExpanded = false
                        }
                    )
                    availableYears.forEach { year ->
                        val isCurrent = year == currentYear
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isCurrent) "$year (Atual)" else "$year",
                                    fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (year == selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onYearChange(year)
                                yearMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 2. Dropdown de Período
        item {
            FilterDropdownChip(
                label = periodFilter.label,
                isSelected = isPeriodFiltered,
                onClick = { periodMenuExpanded = true }
            ) {
                DropdownMenu(
                    expanded = periodMenuExpanded,
                    onDismissRequest = { periodMenuExpanded = false }
                ) {
                    BillPeriodFilter.values().forEach { filter ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = filter.label,
                                    fontWeight = if (filter == periodFilter) FontWeight.Bold else FontWeight.Normal,
                                    color = if (filter == periodFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onPeriodChange(filter)
                                periodMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 3. Dropdown de Tipo
        item {
            val typeLabel = when (typeFilter) {
                BillType.SINGLE -> "Avulsas"
                BillType.INSTALLMENT -> "Parceladas"
                BillType.RECURRING -> "Recorrentes"
                null -> "Todos os Tipos"
            }

            FilterDropdownChip(
                label = typeLabel,
                isSelected = isTypeFiltered,
                onClick = { typeMenuExpanded = true }
            ) {
                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Todos os Tipos",
                                fontWeight = if (typeFilter == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(null)
                            typeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Avulsas",
                                fontWeight = if (typeFilter == BillType.SINGLE) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(BillType.SINGLE)
                            typeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Parceladas",
                                fontWeight = if (typeFilter == BillType.INSTALLMENT) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(BillType.INSTALLMENT)
                            typeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Recorrentes",
                                fontWeight = if (typeFilter == BillType.RECURRING) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTypeChange(BillType.RECURRING)
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }

        // 4. Botão Limpar Filtros Ativos
        if (hasActiveFilters) {
            item {
                Surface(
                    shape = PlatformShapes.small,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable(onClick = onResetFilters)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar filtros",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Limpar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDropdownChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            shape = PlatformShapes.small,
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        content()
    }
}
