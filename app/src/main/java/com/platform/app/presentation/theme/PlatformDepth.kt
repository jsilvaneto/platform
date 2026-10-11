package com.platform.app.presentation.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Níveis de profundidade semântica minimalista do aplicativo Platform (Prompt 55).
 * Sem sombras, sem relevo fictício: superfícies 100% planas (0dp) com delimitação por borda de 1dp.
 */
enum class PlatformDepthLevel(
    val tonalElevation: Dp,
    val shadowElevation: Dp
) {
    N0(tonalElevation = 0.dp, shadowElevation = 0.dp),
    N1(tonalElevation = 0.dp, shadowElevation = 0.dp),
    N2(tonalElevation = 0.dp, shadowElevation = 0.dp),
    N3(tonalElevation = 0.dp, shadowElevation = 0.dp)
}

/**
 * Modificador de profundidade semântica.
 * No design minimalista aprovado, retorna o modificador direto sem introduzir sombras.
 */
@Suppress("UNUSED_PARAMETER")
fun Modifier.platformDepth(
    level: PlatformDepthLevel,
    shape: Shape = PlatformShapes.medium,
    clip: Boolean = false
): Modifier {
    return this
}
