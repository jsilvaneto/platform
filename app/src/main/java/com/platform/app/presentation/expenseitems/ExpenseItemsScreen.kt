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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import com.platform.app.presentation.theme.BrandPrimary
import com.platform.app.presentation.theme.PlatformIconCatalog
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
import androidx.compose.material3.Surface
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
import com.platform.app.presentation.components.PlatformSearchTopBar
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
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showAddOrEditSheet by remember { mutableStateOf(false) }
    var showCategoryFilterModal by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ExpenseItem?>(null) }
    var itemToViewDetails by remember { mutableStateOf<ExpenseItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ExpenseItem?>(null) }

    Scaffold(
        topBar = {
            PlatformSearchTopBar(
                title = "Itens de Despesa",
                searchQuery = uiState.searchQuery,
                isSearchActive = isSearchExpanded,
                onSearchQueryChange = { onAction(ExpenseItemsUiAction.SearchQueryChanged(it)) },
                onSearchActiveChange = { isSearchExpanded = it },
                placeholder = "Buscar item de despesa...",
                onNavigateBack = onNavigateBack
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
            // Chips de Filtro por Categoria Refatorados (com ponto de cor e contagem)
            val totalItemsCount = uiState.items.size
            LazyRow(
                contentPadding = PaddingValues(horizontal = Dimens.spacingNormal, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { onAction(ExpenseItemsUiAction.CategoryFilterChanged(null)) },
                        label = { Text("Todas ($totalItemsCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { showCategoryFilterModal = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar Categoria",
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Buscar Categoria...") }
                    )
                }
                items(uiState.categories, key = { it.id }) { cat ->
                    val count = remember(uiState.items, cat.id) {
                        uiState.items.count { it.categoryId == cat.id }
                    }
                    val catColor = remember(cat.colorHex) {
                        try { Color(cat.colorHex.toColorInt()) } catch (e: Exception) { Color.Gray }
                    }
                    FilterChip(
                        selected = uiState.selectedCategoryId == cat.id,
                        onClick = { onAction(ExpenseItemsUiAction.CategoryFilterChanged(cat.id)) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(catColor, CircleShape)
                            )
                        },
                        label = { Text("${cat.name} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Agrupamento Hierárquico por Categoria
            val groupedItemsByCategory = remember(uiState.filteredItems, uiState.categories) {
                val categoryMap = uiState.categories.associateBy { it.id }
                uiState.filteredItems
                    .groupBy { it.categoryId }
                    .mapNotNull { (catId, items) ->
                        val cat = categoryMap[catId]
                        if (cat != null) cat to items else null
                    }
                    .sortedBy { (cat, _) -> cat.name }
            }

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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedItemsByCategory.forEach { (cat, catItems) ->
                        item(key = "header_${cat.id}") {
                            CategorySectionHeader(
                                category = cat,
                                itemsCount = catItems.size
                            )
                        }
                        items(catItems, key = { it.id }) { item ->
                            HierarchicalExpenseItemRow(
                                item = item,
                                onClick = { itemToViewDetails = item }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
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

        if (showCategoryFilterModal) {
            CategorySearchableModal(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { cat ->
                    onAction(ExpenseItemsUiAction.CategoryFilterChanged(cat.id))
                    showCategoryFilterModal = false
                },
                onDismiss = { showCategoryFilterModal = false }
            )
        }
    }
}

@Composable
fun CategorySectionHeader(
    category: Category,
    itemsCount: Int
) {
    val categoryColor = remember(category.colorHex) {
        try { Color(category.colorHex.toColorInt()) } catch (e: Exception) { Color.Gray }
    }
    val functionalIcon = com.platform.app.presentation.home.getFunctionalIcon(category.name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(categoryColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = functionalIcon,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = "$itemsCount ${if (itemsCount == 1) "item" else "itens"}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun HierarchicalExpenseItemRow(
    item: ExpenseItem,
    onClick: () -> Unit
) {
    PlatformCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
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
    var showCategoryPickerModal by remember { mutableStateOf(false) }
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

            val catColor = selectedCategory?.let {
                try { Color(it.colorHex.toColorInt()) } catch (e: Exception) { BrandPrimary }
            }

            Surface(
                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryPickerModal = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedCategory != null && catColor != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(catColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PlatformIconCatalog.getIcon(selectedCategory!!.iconName),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Column {
                            Text(
                                text = "Categoria",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedCategory?.name ?: "Toque para selecionar a categoria...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCategory != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedCategory != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Selecionar Categoria",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showCategoryPickerModal) {
                CategorySearchableModal(
                    categories = categories,
                    selectedCategoryId = selectedCategory?.id,
                    onCategorySelected = {
                        selectedCategory = it
                        showCategoryPickerModal = false
                    },
                    onDismiss = { showCategoryPickerModal = false }
                )
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

/**
 * Seletor inteligente e escalável de categorias com busca instantânea e agrupamento por natureza.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySearchableModal(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val groupedByNature = remember(filteredCategories) {
        filteredCategories.groupBy { it.nature }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selecionar Categoria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Buscar entre as ${categories.size} categorias...",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma categoria encontrada para \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedByNature.forEach { (nature, cats) ->
                        item(key = "nature_${nature.name}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nature.displayName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }

                        items(cats, key = { it.id }) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val color = remember(cat.colorHex) {
                                try { Color(cat.colorHex.toColorInt()) } catch (e: Exception) { Color.Gray }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCategorySelected(cat)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(color.copy(alpha = 0.18f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = PlatformIconCatalog.getIcon(cat.iconName),
                                                contentDescription = null,
                                                tint = color,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selecionada",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


