package com.platform.app.presentation.budgets

import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.PlatformSurfaceVariant

import com.platform.app.presentation.budgets.components.AddEditBudgetDialog
import com.platform.app.presentation.budgets.components.BudgetDetailBottomSheet
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.platform.app.presentation.common.AppStrings
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import com.platform.app.presentation.theme.PlatformColorPicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.Budget
import com.platform.app.domain.model.Category
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.BrandPrimaryDark
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber
import com.platform.app.presentation.statistics.components.MonthNavigationHeader
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var isNewBudgetOpen by remember { mutableStateOf(false) }
    var budgetToViewDetails by remember { mutableStateOf<BudgetWithSpend?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is BudgetsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is BudgetsUiEffect.BudgetSaved -> {
                    budgetToEdit = null
                    isNewBudgetOpen = false
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PlatformAppBar(
                title = "Orçamentos",
                onOpenDrawer = onOpenDrawer
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isNewBudgetOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Orçamento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonthNavigationHeader(
                selectedMonthMillis = uiState.selectedMonthMillis,
                onPreviousMonth = { viewModel.onAction(BudgetsUiAction.PreviousMonth) },
                onNextMonth = { viewModel.onAction(BudgetsUiAction.NextMonth) },
                onCurrentMonth = { viewModel.onAction(BudgetsUiAction.ChangeMonth(System.currentTimeMillis())) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Hero Card de Resumo de Orçamentos
                BudgetsSummaryCard(
                    totalLimitCents = uiState.totalLimitCents,
                    totalSpentCents = uiState.totalSpentCents,
                    totalPaidCents = uiState.totalPaidCents,
                    totalPendingCents = uiState.totalPendingCents,
                    progress = uiState.overallProgress
                )

                Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading && uiState.budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.budgets.isEmpty()) {
                EmptyBudgetsView(onAddClick = { isNewBudgetOpen = true })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.budgets, key = { it.budget.id }) { item ->
                        BudgetCard(
                            item = item,
                            onClick = { budgetToViewDetails = item }
                        )
                    }
                }
            }
        }
    }

    if (isNewBudgetOpen || budgetToEdit != null) {
            AddEditBudgetDialog(
                budget = budgetToEdit,
                categories = uiState.categories,
                onDismiss = {
                    isNewBudgetOpen = false
                    budgetToEdit = null
                },
                onConfirm = { budget ->
                    viewModel.onAction(BudgetsUiAction.SaveBudget(budget))
                }
            )
        }

        budgetToViewDetails?.let { item ->
            val currentItem = uiState.budgets.find { it.budget.id == item.budget.id } ?: item
            BudgetDetailBottomSheet(
                item = currentItem,
                onDismiss = { budgetToViewDetails = null },
                onEdit = {
                    budgetToViewDetails = null
                    budgetToEdit = currentItem.budget
                },
                onDelete = { budgetId ->
                    budgetToViewDetails = null
                    viewModel.onAction(BudgetsUiAction.DeleteBudget(budgetId))
                }
            )
        }
    }
}

@Composable
fun BudgetsSummaryCard(
    totalLimitCents: Long,
    totalSpentCents: Long,
    totalPaidCents: Long = 0L,
    totalPendingCents: Long = 0L,
    progress: Float
) {
    val isOverLimit = totalSpentCents > totalLimitCents && totalLimitCents > 0L
    val barColor = if (isOverLimit) MaterialTheme.colorScheme.error else if (progress > 0.8f) WarningAmber else SuccessGreen

    PlatformSurface(
        variant = PlatformSurfaceVariant.Tonal,
        shape = PlatformShapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Teto Mensal Geral",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% do orçamento mensal consumido",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Gasto no Mês",
                        style = MaterialTheme.typography.labelSmall,
                        color = barColor
                    )
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(totalSpentCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    if (totalPaidCents > 0L || totalPendingCents > 0L) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pago: ${CurrencyUtils.formatCentsToCurrency(totalPaidCents)} • Pend: ${CurrencyUtils.formatCentsToCurrency(totalPendingCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Limite Orçado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(totalLimitCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetCard(
    item: BudgetWithSpend,
    onClick: () -> Unit
) {
    val budget = item.budget
    val color = remember(budget.colorHex) {
        try { Color(android.graphics.Color.parseColor(budget.colorHex)) } catch (e: Exception) { BrandPrimaryDark }
    }
    val progressColor = if (item.isExceeded)
        MaterialTheme.colorScheme.error
    else if (item.progress > 0.8f)
        WarningAmber
    else
        SuccessGreen

    val remainingCents = budget.limitAmountCents - item.spentCents

    PlatformCard(
        onClick = onClick,
        containerColor = if (item.isExceeded)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (item.isExceeded)
                            "Excedido em ${CurrencyUtils.formatCentsToCurrency(-remainingCents)}"
                        else
                            "Resta ${CurrencyUtils.formatCentsToCurrency(remainingCents)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = if (item.isExceeded) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isExceeded) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Limite Excedido",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(item.spentCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "de ${CurrencyUtils.formatCentsToCurrency(budget.limitAmountCents)} (${(item.progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver detalhes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { item.progress.coerceIn(0f, 1f) },
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}

@Composable
fun EmptyBudgetsView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhum teto de orçamento definido",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Estipule limites mensais por categoria (como Alimentação, Transporte, Lazer) para controlar seus gastos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick, shape = PlatformShapes.medium) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Criar Primeiro Teto")
        }
    }
}
