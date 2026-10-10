package com.platform.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.platform.app.presentation.theme.PlatformShapes

/**
 * Variantes semânticas de superfícies para controle rigoroso de camadas visuais:
 * - Flat: Fundo surface puro sem borda nem relevo tonal.
 * - Tonal: Fundo sutil (surfaceVariant ou surfaceContainer) para blocos internos sem borda secundária.
 * - Outlined: Fundo surface com contorno semântico sutil (outlineVariant).
 */
enum class PlatformSurfaceVariant {
    Flat,
    Tonal,
    Outlined
}

/**
 * Componente base de superfície que impõe o padrão visual do Design System:
 * - No máximo 1 nível de borda por tela (blocos internos usam variante Tonal).
 * - Cantos padronizados com [PlatformShapes.medium] por padrão.
 */
@Composable
fun PlatformSurface(
    modifier: Modifier = Modifier,
    variant: PlatformSurfaceVariant = PlatformSurfaceVariant.Flat,
    shape: Shape = PlatformShapes.medium,
    containerColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    contentColor: Color? = null,
    tonalElevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val resolvedContainerColor = containerColor ?: when (variant) {
        PlatformSurfaceVariant.Flat -> MaterialTheme.colorScheme.surface
        PlatformSurfaceVariant.Tonal -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        PlatformSurfaceVariant.Outlined -> MaterialTheme.colorScheme.surface
    }

    val resolvedBorder = when {
        borderColor != null -> BorderStroke(1.dp, borderColor)
        variant == PlatformSurfaceVariant.Outlined -> BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        else -> null
    }

    val finalContentColor = contentColor ?: when (variant) {
        PlatformSurfaceVariant.Tonal -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = resolvedContainerColor,
        contentColor = finalContentColor,
        tonalElevation = tonalElevation,
        border = resolvedBorder
    ) {
        Box(content = content)
    }
}
