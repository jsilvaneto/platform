package com.platform.app.presentation.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Níveis de profundidade semântica (PlatformDepth) para controle limpo de elevação e relevo tonal.
 * Elimina inclinações 3D desnecessárias e preserva harmonia visual e sobriedade financeira:
 * - N0: Rente / plano ao fundo (Flat, 0dp)
 * - N1: Relevo tonal sutil para cards e linhas de lista (2dp)
 * - N2: Relevo intermediário para menus suspensos e cards flutuantes (6dp)
 * - N3: Elevação pronunciada para diálogos e bottom sheets modais (12dp)
 */
enum class PlatformDepthLevel(
    val tonalElevation: Dp,
    val shadowElevation: Dp
) {
    N0(tonalElevation = 0.dp, shadowElevation = 0.dp),
    N1(tonalElevation = 2.dp, shadowElevation = 1.dp),
    N2(tonalElevation = 4.dp, shadowElevation = 3.dp),
    N3(tonalElevation = 8.dp, shadowElevation = 6.dp)
}

/**
 * Modificador de profundidade semântica com sombras limpas e contidas.
 */
fun Modifier.platformDepth(
    level: PlatformDepthLevel,
    shape: Shape = PlatformShapes.medium,
    clip: Boolean = false
): Modifier {
    return if (level.shadowElevation > 0.dp) {
        this.shadow(
            elevation = level.shadowElevation,
            shape = shape,
            clip = clip,
            ambientColor = Color(0x14000000),
            spotColor = Color(0x1E000000)
        )
    } else {
        this
    }
}
