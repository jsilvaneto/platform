package com.platform.app.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Delegador de compatibilidade para [PlatformAppBar], mantendo a API existente
 * enquanto unifica a implementação subjacente em um único componente.
 */
@Composable
fun PlatformSearchTopBar(
    title: String,
    searchQuery: String,
    isSearchActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    subtitle: String? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    PlatformAppBar(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        onOpenDrawer = onOpenDrawer,
        onNavigateBack = onNavigateBack,
        navigationIcon = navigationIcon,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        searchPlaceholder = placeholder,
        onSearchQueryChange = onSearchQueryChange,
        onSearchActiveChange = onSearchActiveChange,
        actions = actions
    )
}
