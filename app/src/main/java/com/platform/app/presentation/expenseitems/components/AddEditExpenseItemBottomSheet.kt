package com.platform.app.presentation.expenseitems.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.core.graphics.toColorInt
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.PlatformShapes
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.platform.app.domain.model.Category
import com.platform.app.domain.model.ExpenseItem
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.theme.PlatformIconPicker
import com.platform.app.presentation.components.PlatformStatusChip
import com.platform.app.presentation.components.StatusChipType
import com.platform.app.presentation.theme.BrandPrimary
import com.platform.app.presentation.theme.BrandPrimaryDark
import java.util.UUID

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
                shape = PlatformShapes.medium
            )

            val catColor = selectedCategory?.let {
                try { Color(it.colorHex.toColorInt()) } catch (e: Exception) { BrandPrimary }
            }

            Surface(
                shape = PlatformShapes.medium,
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
                    Text(AppStrings.Actions.CANCEL)
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
                    shape = PlatformShapes.medium,
                    enabled = name.isNotBlank() && selectedCategory != null
                ) {
                    Text(if (isEditing) AppStrings.Actions.SAVE_CHANGES else AppStrings.Actions.SAVE)
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
        shape = PlatformShapes.bottomSheet,
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
                shape = PlatformShapes.medium,
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
                                try { Color(cat.colorHex.toColorInt()) } catch (e: Exception) { BrandPrimary }
                            }

                            Surface(
                                shape = PlatformShapes.medium,
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


