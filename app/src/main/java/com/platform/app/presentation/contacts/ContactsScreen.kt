package com.platform.app.presentation.contacts

import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.components.PlatformSurfaceVariant

import com.platform.app.presentation.theme.PlatformShapes

import com.platform.app.presentation.contacts.components.AddContactBottomSheet

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Signpost
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformAvatar
import com.platform.app.presentation.components.PlatformSearchTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.platform.app.domain.model.Contact
import com.platform.app.domain.model.ContactType
import com.platform.app.core.util.CurrencyUtils
import androidx.compose.ui.text.style.TextOverflow
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onNavigateToDetail: (String) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(key1 = true) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ContactsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ContactsUiEffect.ContactSaved -> {
                    isBottomSheetOpen = false
                    contactToEdit = null
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PlatformSearchTopBar(
                title = "Contatos",
                searchQuery = uiState.searchQuery,
                isSearchActive = isSearchExpanded,
                onSearchQueryChange = { viewModel.onAction(ContactsUiAction.SearchQueryChanged(it)) },
                onSearchActiveChange = { isSearchExpanded = it },
                placeholder = "Buscar contato...",
                onOpenDrawer = onOpenDrawer
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    contactToEdit = null
                    isBottomSheetOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Contato")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Filtro por Tipo de Contato
            ContactTypeFilterRow(
                selectedType = uiState.selectedTypeFilter,
                contacts = uiState.contacts,
                onSelectType = { viewModel.onAction(ContactsUiAction.FilterTypeSelected(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredContacts.isEmpty() -> {
                    EmptyContactsView()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                    ) {
                        if (uiState.selectedTypeFilter == null) {
                            // Exibição agrupada por tipo quando filtro estiver em 'Todos'
                            val groups = ContactType.entries.mapNotNull { type ->
                                val list = uiState.filteredContacts.filter { it.type == type }
                                if (list.isNotEmpty()) type to list else null
                            }
                            groups.forEach { (type, contactsInType) ->
                                item(key = "header_${type.name}") {
                                    ContactSectionHeader(type = type, count = contactsInType.size)
                                }
                                items(contactsInType, key = { it.id }) { contact ->
                                    ContactCard(
                                        contact = contact,
                                        openBalanceCents = uiState.openBalances[contact.id] ?: 0L,
                                        onClick = { onNavigateToDetail(contact.id) }
                                    )
                                }
                            }
                        } else {
                            // Exibição direta da lista do tipo filtrado
                            items(uiState.filteredContacts, key = { it.id }) { contact ->
                                ContactCard(
                                    contact = contact,
                                    openBalanceCents = uiState.openBalances[contact.id] ?: 0L,
                                    onClick = { onNavigateToDetail(contact.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isBottomSheetOpen) {
            AddContactBottomSheet(
                contact = contactToEdit,
                sheetState = sheetState,
                onLookupCep = viewModel::lookupCep,
                onDismiss = {
                    isBottomSheetOpen = false
                    contactToEdit = null
                },
                onSave = { savedContact ->
                    viewModel.onAction(ContactsUiAction.SaveContact(savedContact))
                }
            )
        }
    }
}

@Composable
fun ContactCard(
    contact: Contact,
    openBalanceCents: Long = 0L,
    onClick: () -> Unit
) {
    val (avatarColor, _) = getContactTypeColors(contact.type)
    val typeLabel = getContactTypeLabel(contact.type)

    PlatformSurface(
        variant = PlatformSurfaceVariant.Tonal,
        shape = PlatformShapes.medium,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlatformAvatar(
                name = contact.name,
                size = 40.dp,
                color = avatarColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val balanceText = if (openBalanceCents > 0L) {
                    "${CurrencyUtils.formatCentsToCurrency(openBalanceCents)} em aberto"
                } else {
                    "Em dia"
                }

                Text(
                    text = "$typeLabel · $balanceText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}


@Composable
fun EmptyContactsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "👥", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhum contato encontrado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Toque no botão '+' para adicionar fornecedores, prestadores ou recebedores.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

fun getContactTypeIcon(type: ContactType): ImageVector = when (type) {
    ContactType.PESSOA_FISICA -> Icons.Default.Person
    ContactType.FORNECEDOR -> Icons.Default.Business
    ContactType.ORGAO_PUBLICO -> Icons.Default.AccountBalance
}

@Composable
fun getContactTypeColors(type: ContactType): Pair<Color, Color> {
    return when (type) {
        ContactType.PESSOA_FISICA -> {
            val content = MaterialTheme.colorScheme.primary
            content to content.copy(alpha = 0.12f)
        }
        ContactType.FORNECEDOR -> {
            val content = WarningAmber
            content to content.copy(alpha = 0.12f)
        }
        ContactType.ORGAO_PUBLICO -> {
            val content = MaterialTheme.colorScheme.tertiary
            content to content.copy(alpha = 0.12f)
        }
    }
}

fun getContactTypeLabel(type: ContactType): String = when (type) {
    ContactType.PESSOA_FISICA -> AppStrings.ContactType.INDIVIDUAL_SHORT
    ContactType.FORNECEDOR -> AppStrings.ContactType.SUPPLIER
    ContactType.ORGAO_PUBLICO -> AppStrings.ContactType.PUBLIC_ENTITY
}

@Composable
fun ContactTypeBadge(
    type: ContactType,
    modifier: Modifier = Modifier
) {
    val (contentColor, containerColor) = getContactTypeColors(type)
    val icon = getContactTypeIcon(type)
    val label = getContactTypeLabel(type)

    Surface(
        shape = PlatformShapes.small,
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun ContactTypeFilterRow(
    selectedType: ContactType?,
    contacts: List<Contact>,
    onSelectType: (ContactType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = contacts.size
    val pfCount = contacts.count { it.type == ContactType.PESSOA_FISICA }
    val fornCount = contacts.count { it.type == ContactType.FORNECEDOR }
    val orgCount = contacts.count { it.type == ContactType.ORGAO_PUBLICO }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
    ) {
        item {
            FilterChipItem(
                label = "${AppStrings.Actions.ALL} ($totalCount)",
                icon = null,
                isSelected = selectedType == null,
                onClick = { onSelectType(null) }
            )
        }
        item {
            FilterChipItem(
                label = "${AppStrings.ContactType.INDIVIDUAL} ($pfCount)",
                icon = Icons.Default.Person,
                isSelected = selectedType == ContactType.PESSOA_FISICA,
                onClick = { onSelectType(ContactType.PESSOA_FISICA) }
            )
        }
        item {
            FilterChipItem(
                label = "${AppStrings.ContactType.SUPPLIER} ($fornCount)",
                icon = Icons.Default.Business,
                isSelected = selectedType == ContactType.FORNECEDOR,
                onClick = { onSelectType(ContactType.FORNECEDOR) }
            )
        }
        item {
            FilterChipItem(
                label = "${AppStrings.ContactType.PUBLIC_ENTITY} ($orgCount)",
                icon = Icons.Default.AccountBalance,
                isSelected = selectedType == ContactType.ORGAO_PUBLICO,
                onClick = { onSelectType(ContactType.ORGAO_PUBLICO) }
            )
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = PlatformShapes.small,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (isSelected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun ContactSectionHeader(
    type: ContactType,
    count: Int,
    modifier: Modifier = Modifier
) {
    val (contentColor, _) = getContactTypeColors(type)
    val icon = getContactTypeIcon(type)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
            shape = CircleShape,
            color = contentColor.copy(alpha = 0.12f)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}
