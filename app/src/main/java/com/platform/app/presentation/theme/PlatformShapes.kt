package com.platform.app.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Tokens padronizados de forma e cantos arredondados do aplicativo Platform.
 * Substitui raios literais dispersos (RoundedCornerShape(n.dp)) por uma escala coerente:
 * - small: 8.dp (badges, micro-elementos, botões compactos)
 * - medium: 12.dp (cards secundários, text fields, linhas de lista, chips)
 * - large: 16.dp (cards principais, hero cards, bottom sheets, diálogos)
 * - pill: CircleShape / 999.dp (chips de status, tabs pílula, botões arredondados)
 */
object PlatformShapes {
    val none = RoundedCornerShape(0.dp)
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(24.dp)
    val pill = CircleShape
    val bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    val Material3Shapes = Shapes(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge
    )
}
