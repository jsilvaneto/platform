package com.platform.app.presentation.expenseitems

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.Dimens
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemsScreen(
    uiState: ExpenseItemsUiState,
    onAction: (ExpenseItemsUiAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddOrEditSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ExpenseItem?>(null) }
    var itemToViewDetails by remember { mutableStateOf<ExpenseItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ExpenseItem?>(null) }

    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Itens de Despesa",
                subtitle = "Substitui Subcategorias",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showAddOrEditSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Item")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Campo de Pesquisa
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onAction(ExpenseItemsUiAction.SearchQueryChanged(it)) },
                placeholder = { Text("Buscar item de despesa...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingSmall)
            )

            // Chips de Filtro por Categoria
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.spacingNormal),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { onAction(ExpenseItemsUiAction.CategoryFilterChanged(null)) },
                        label = { Text("Todas Categorias") }
                    )
                }
                items(uiState.categories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == cat.id,
                        onClick = { onAction(ExpenseItemsUiAction.CategoryFilterChanged(cat.id)) },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhum item de despesa encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
                ) {
                    items(uiState.filteredItems, key = { it.id }) { item ->
                        ExpenseItemRow(
                            item = item,
                            onClick = { itemToViewDetails = item }
                        )
                    }
                }
            }
        }

        // BottomSheet de Detalhes com opções de Editar e Excluir
        itemToViewDetails?.let { targetItem ->
            ExpenseItemDetailBottomSheet(
                item = targetItem,
                onDismiss = { itemToViewDetails = null },
                onEdit = {
                    itemToViewDetails = null
                    itemToEdit = targetItem
                    showAddOrEditSheet = true
                },
                onDelete = {
                    itemToViewDetails = null
                    itemToDelete = targetItem
                }
            )
        }

        // BottomSheet para Adicionar ou Editar Item
        if (showAddOrEditSheet || itemToEdit != null) {
            AddEditExpenseItemBottomSheet(
                itemToEdit = itemToEdit,
                categories = uiState.categories,
                onDismiss = {
                    showAddOrEditSheet = false
                    itemToEdit = null
                },
                onSave = { savedItem ->
                    onAction(ExpenseItemsUiAction.SaveItem(savedItem))
                    showAddOrEditSheet = false
                    itemToEdit = null
                }
            )
        }

        // Diálogo de confirmação para exclusão
        if (itemToDelete != null) {
            val target = itemToDelete!!
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = {
                    Text(
                        text = "Excluir Item",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Deseja excluir o item de despesa '${target.name}'? Registros anteriores manterão o nome original, mas ele não aparecerá em novos lançamentos.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAction(ExpenseItemsUiAction.DeleteItem(target.id))
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ExpenseItemRow(
    item: ExpenseItem,
    onClick: () -> Unit
) {
    val categoryColor = try { Color(item.categoryColorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    val functionalIcon = com.platform.app.presentation.home.getFunctionalIcon(item.categoryName)

    PlatformCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = functionalIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacingSmall))

            PlatformStatusChip(
                text = item.nature.displayName,
                type = when (item.nature.name) {
                    "OBRIGATORIO" -> StatusChipType.ERROR
                    "NECESSARIO" -> StatusChipType.WARNING
                    "DESEJA" -> StatusChipType.INFO
                    else -> StatusChipType.NEUTRAL
                }
            )

            Spacer(modifier = Modifier.width(Dimens.spacingSmall))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItemDetailBottomSheet(
    item: ExpenseItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }
    val categoryColor = try {
        Color(item.categoryColorHex.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val functionalIcon = com.platform.app.presentation.home.getFunctionalIcon(item.categoryName)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingNormal, vertical = Dimens.spacingSmall)
        ) {
            // Header Row: Ícone, Título, Subtítulo e Menu 3 Pontos
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = functionalIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.spacingMedium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Item de Despesa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 3 Pontos com Menu de Opções
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar Item") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Excluir Item", color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingNormal))

            // Card Contextual de Vínculos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
                ) {
                    // Categoria Vinculada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categoria Vinculada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(categoryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Natureza Herdada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Natureza do Gasto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PlatformStatusChip(
                            text = item.nature.displayName,
                            type = when (item.nature.name) {
                                "OBRIGATORIO" -> StatusChipType.ERROR
                                "NECESSARIO" -> StatusChipType.WARNING
                                "DESEJA" -> StatusChipType.INFO
                                else -> StatusChipType.NEUTRAL
                            }
                        )
                    }

                    // Descrição da Natureza
                    val natureDescription = when (item.nature.name) {
                        "OBRIGATORIO" -> "Gastos indispensáveis para sobrevivência ou compromissos jurídicos inegociáveis."
                        "NECESSARIO" -> "Gastos essenciais para a rotina diária, saúde, trabalho e conforto básico."
                        "DESEJA" -> "Gastos de estilo de vida, lazer, supérfluos e compras por desejo pessoal."
                        else -> "Classificação financeira sem restrição específica."
                    }
                    Text(
                        text = natureDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingNormal))

            // Ação Principal: Botão Editar Item
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.buttonCornerRadius)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                Text("Editar Item")
            }

            Spacer(modifier = Modifier.height(Dimens.spacingMedium))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseItemBottomSheet(
    itemToEdit: ExpenseItem? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (ExpenseItem) -> Unit
) {
    val isEditing = itemToEdit != null
    var name by remember(itemToEdit) { mutableStateOf(itemToEdit?.name ?: "") }
    var selectedCategory by remember(itemToEdit, categories) {
        mutableStateOf(
            itemToEdit?.let { editItem -> categories.find { it.id == editItem.categoryId } }
                ?: categories.firstOrNull()
        )
    }
    var expandedCategoryMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            Text(
                text = if (isEditing) "Editar Item de Despesa" else "Novo Item de Despesa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Item (ex: Supermercado, Internet)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(Dimens.buttonCornerRadius)
            )

            ExposedDropdownMenuBox(
                expanded = expandedCategoryMenu,
                onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Selecione a Categoria",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu) },
                    shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoryMenu,
                    onDismissRequest = { expandedCategoryMenu = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${cat.name} (${cat.nature.displayName})") },
                            onClick = {
                                selectedCategory = cat
                                expandedCategoryMenu = false
                            }
                        )
                    }
                }
            }

            selectedCategory?.let { cat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Natureza Herdada:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PlatformStatusChip(
                        text = cat.nature.displayName,
                        type = when (cat.nature.name) {
                            "OBRIGATORIO" -> StatusChipType.ERROR
                            "NECESSARIO" -> StatusChipType.WARNING
                            "DESEJA" -> StatusChipType.INFO
                            else -> StatusChipType.NEUTRAL
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                Button(
                    onClick = {
                        val cat = selectedCategory
                        if (name.isNotBlank() && cat != null) {
                            onSave(
                                ExpenseItem(
                                    id = itemToEdit?.id ?: UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    categoryId = cat.id,
                                    categoryName = cat.name,
                                    categoryColorHex = cat.colorHex,
                                    nature = cat.nature
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                    enabled = name.isNotBlank() && selectedCategory != null
                ) {
                    Text(if (isEditing) "Salvar Alterações" else "Salvar")
                }
            }
        }
    }
}

