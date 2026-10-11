package com.platform.app.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Tokens padronizados de forma e cantos arredondados do aplicativo Platform (Prompt 55).
 * Escala harmônica e estrita:
 * - small: 8.dp (badges, micro-elementos, botões compactos)
 * - medium: 12.dp (cards secundários, text fields, linhas de lista, chips)
 * - large: 16.dp (cards principais, hero cards)
 * - extraLarge: 28.dp (bottom sheets, diálogos, containers modais)
 * - pill: CircleShape / pílula 50% (chips, tabs e botões arredondados)
 */
object PlatformShapes {
    val none = RoundedCornerShape(0.dp)
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val pill = CircleShape
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val drawer = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    val barTop = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
    val barEnd = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)

    val Material3Shapes = Shapes(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge
    )
}
