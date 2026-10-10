package com.platform.app.presentation.expenseitems

import com.platform.app.presentation.expenseitems.components.ExpenseItemDetailBottomSheet
import com.platform.app.presentation.expenseitems.components.AddEditExpenseItemBottomSheet

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
import com.platform.app.presentation.common.AppStrings
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
                        try { Color(cat.colorHex.toColorInt()) } catch (e: Exception) { BrandPrimary }
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
                        Text(AppStrings.Actions.DELETE)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text(AppStrings.Actions.CANCEL)
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
        try { Color(category.colorHex.toColorInt()) } catch (e: Exception) { BrandPrimary }
    }
    val functionalIcon = PlatformIconCatalog.getIcon(category.iconName)

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

