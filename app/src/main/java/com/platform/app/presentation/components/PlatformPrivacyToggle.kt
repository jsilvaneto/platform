package com.platform.app.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils

/**
 * Alternador visual de privacidade para exibir ou ocultar valores financeiros confidenciais.
 */
@Composable
fun PlatformPrivacyToggle(
    isPrivate: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = if (isPrivate) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (isPrivate) "Exibir valores" else "Ocultar valores",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Função utilitária para formatar valores considerando o modo de privacidade.
 */
fun formatValueOrPrivate(amountCents: Long, isPrivate: Boolean): String {
    return if (isPrivate) "R$ ••••••" else CurrencyUtils.formatCentsToCurrency(amountCents)
}
