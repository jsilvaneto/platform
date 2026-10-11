package com.platform.app.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tokens padronizados do Design System Minimalista do Platform.
 * Fonte única da verdade em sincronia estrita com design/tokens.json (Prompt 55).
 *
 * Princípios:
 * - Superfícies planas com borda de 1dp na cor de linha (sem gradientes, sombras ou elevações).
 * - Cores de marca neutras (preenchimento na cor de texto e conteúdo na cor de fundo).
 * - Cores de status restritas estritamente ao [PlatformStatusStyle].
 */
object PlatformTokens {

    val BorderWidth = 1.dp

    object Amoled {
        val background = Color(0xFF000000)
        val surface1 = Color(0xFF0B0C10)
        val surface2 = Color(0xFF14161D)
        val line = Color(0xFF23262F)
        val text = Color(0xFFF2F4FF)
        val text2 = Color(0xFF9AA3C0)
    }

    object Dark {
        val background = Color(0xFF0E1220)
        val surface1 = Color(0xFF161B2E)
        val surface2 = Color(0xFF1E2540)
        val line = Color(0xFF2A3354)
        val text = Color(0xFFE8ECFF)
        val text2 = Color(0xFFA3ACCB)
    }

    object Light {
        val background = Color(0xFFF6F7FB)
        val surface1 = Color(0xFFFFFFFF)
        val surface2 = Color(0xFFEEF0F8)
        val line = Color(0xFFDDE1F0)
        val text = Color(0xFF12162B)
        val text2 = Color(0xFF5B6385)
    }

    // Cores semânticas de status internas: acesso restrito a PlatformStatusStyle
    internal object StatusDark {
        val paid = Color(0xFF34D399)       // Pago (Verde menta esmeralda)
        val attention = Color(0xFFFBBF24)  // Atenção / Vence hoje (Âmbar)
        val overdue = Color(0xFFF87171)    // Atrasado (Coral avermelhado)
    }

    internal object StatusLight {
        val paid = Color(0xFF059669)       // Pago (Verde floresta profundo)
        val attention = Color(0xFFB45309)  // Atenção / Vence hoje (Âmbar escuro)
        val overdue = Color(0xFFDC2626)    // Atrasado (Carmesim)
    }
}
