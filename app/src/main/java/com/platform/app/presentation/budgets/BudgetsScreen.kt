package com.platform.app.presentation.budgets

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.model.Budget
import com.platform.app.domain.model.Category
import com.platform.app.presentation.components.PlatformAppBar
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
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Orçamento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Hero Card de Resumo de Orçamentos
            BudgetsSummaryCard(
                totalLimitCents = uiState.totalLimitCents,
                totalSpentCents = uiState.totalSpentCents,
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
                            onEdit = { budgetToEdit = item.budget },
                            onDelete = { viewModel.onAction(BudgetsUiAction.DeleteBudget(item.budget.id)) }
                        )
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
    }
}

@Composable
fun BudgetsSummaryCard(
    totalLimitCents: Long,
    totalSpentCents: Long,
    progress: Float
) {
    val isOverLimit = totalSpentCents > totalLimitCents && totalLimitCents > 0L
    val barColor = if (isOverLimit) MaterialTheme.colorScheme.error else if (progress > 0.8f) Color(0xFFF59E0B) else Color(0xFF10B981)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val budget = item.budget
    val color = remember(budget.colorHex) {
        try { Color(android.graphics.Color.parseColor(budget.colorHex)) } catch (e: Exception) { Color(0xFF3B82F6) }
    }
    val progressColor = if (item.isExceeded)
        MaterialTheme.colorScheme.error
    else if (item.progress > 0.8f)
        Color(0xFFF59E0B)
    else
        Color(0xFF10B981)

    val remainingCents = budget.limitAmountCents - item.spentCents

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isExceeded)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(color, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (item.isExceeded)
                            "Excedido em ${CurrencyUtils.formatCentsToCurrency(-remainingCents)}"
                        else
                            "Resta ${CurrencyUtils.formatCentsToCurrency(remainingCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = if (item.isExceeded) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (item.isExceeded) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Limite Excedido",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Orçamento",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir Orçamento",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gasto: ${CurrencyUtils.formatCentsToCurrency(item.spentCents)} de ${CurrencyUtils.formatCentsToCurrency(budget.limitAmountCents)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(item.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
        }
    }
}

@Composable
fun AddEditBudgetDialog(
    budget: Budget?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Budget) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId) }
    var selectedCategoryName by remember { mutableStateOf(budget?.categoryName ?: (categories.firstOrNull()?.name ?: "Geral")) }
    var limitText by remember { mutableStateOf(if (budget != null) (budget.limitAmountCents / 100).toString() else "") }

    val colors = listOf("#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#EF4444", "#6366F1")
    var selectedColor by remember { mutableStateOf(budget?.colorHex ?: colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (budget == null) "Novo Teto de Orçamento" else "Editar Orçamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecione a Categoria:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = {
                                selectedCategoryId = null
                                selectedCategoryName = "Geral (Todas as despesas)"
                            },
                            label = { Text("Geral") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = cat.id
                                selectedCategoryName = cat.name
                                selectedColor = cat.colorHex
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { char -> char.isDigit() } },
                    label = { Text("Limite Mensal Máximo (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Cor de Destaque:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitCents = (limitText.toLongOrNull() ?: 0L) * 100L
                    if (limitCents > 0L) {
                        onConfirm(
                            Budget(
                                id = budget?.id ?: UUID.randomUUID().toString(),
                                categoryId = selectedCategoryId,
                                categoryName = selectedCategoryName,
                                limitAmountCents = limitCents,
                                colorHex = selectedColor
                            )
                        )
                    }
                },
                enabled = (limitText.toLongOrNull() ?: 0L) > 0L,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (budget == null) "Salvar Teto" else "Atualizar Teto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
        Button(onClick = onAddClick, shape = RoundedCornerShape(12.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Criar Primeiro Teto")
        }
    }
}
