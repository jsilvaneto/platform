package com.platform.app.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Teste automatizado de contraste WCAG 2.1 AA para os temas do aplicativo Platform.
 * Valida a conformidade de acessibilidade para:
 * - Texto normal (mínimo 4.5:1)
 * - Texto grande e elementos de interface/ícones (mínimo 3.0:1)
 */
class ThemeContrastTest {

    private fun calculateLuminance(color: Color): Double {
        fun componentLuminance(c: Float): Double {
            return if (c <= 0.04045f) {
                c.toDouble() / 12.92
            } else {
                ((c.toDouble() + 0.055) / 1.055).pow(2.4)
            }
        }
        val r = componentLuminance(color.red)
        val g = componentLuminance(color.green)
        val b = componentLuminance(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(color1: Color, color2: Color): Double {
        val lum1 = calculateLuminance(color1)
        val lum2 = calculateLuminance(color2)
        val lighter = max(lum1, lum2)
        val darker = min(lum1, lum2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun lightTheme_primaryTextMeetsWcagAA() {
        val ratioOnSurface = contrastRatio(LightTextPrimary, LightSurface)
        val ratioOnBackground = contrastRatio(LightTextPrimary, LightBackground)

        assertTrue(
            "Texto primário claro sobre superfície deve ter contraste >= 4.5:1 (atual: $ratioOnSurface)",
            ratioOnSurface >= 4.5
        )
        assertTrue(
            "Texto primário claro sobre fundo deve ter contraste >= 4.5:1 (atual: $ratioOnBackground)",
            ratioOnBackground >= 4.5
        )
    }

    @Test
    fun darkTheme_primaryTextMeetsWcagAA() {
        val ratioOnSurface = contrastRatio(DarkTextPrimary, DarkSurface)
        val ratioOnBackground = contrastRatio(DarkTextPrimary, DarkBackground)

        assertTrue(
            "Texto primário escuro sobre superfície deve ter contraste >= 4.5:1 (atual: $ratioOnSurface)",
            ratioOnSurface >= 4.5
        )
        assertTrue(
            "Texto primário escuro sobre fundo deve ter contraste >= 4.5:1 (atual: $ratioOnBackground)",
            ratioOnBackground >= 4.5
        )
    }

    @Test
    fun amoledTheme_primaryTextMeetsWcagAA() {
        val amoledBackground = Color(0xFF000000)
        val amoledSurface = Color(0xFF09090B)
        val amoledText = Color(0xFFF4F4F5)

        val ratioOnSurface = contrastRatio(amoledText, amoledSurface)
        val ratioOnBackground = contrastRatio(amoledText, amoledBackground)

        assertTrue(
            "Texto AMOLED sobre superfície deve ter contraste >= 4.5:1 (atual: $ratioOnSurface)",
            ratioOnSurface >= 4.5
        )
        assertTrue(
            "Texto AMOLED sobre fundo preto puro deve ter contraste >= 4.5:1 (atual: $ratioOnBackground)",
            ratioOnBackground >= 4.5
        )
    }

    @Test
    fun buttonLabels_onPrimaryMeetsWcagAA() {
        val onPrimary = Color(0xFFFFFFFF)
        val brandPrimary = BrandPrimary
        val brandPrimaryDark = BrandPrimaryDark

        val ratioLight = contrastRatio(onPrimary, brandPrimary)
        val ratioDark = contrastRatio(onPrimary, brandPrimaryDark)

        assertTrue(
            "Texto onPrimary sobre botão principal claro deve ter contraste >= 3.0:1 (atual: $ratioLight)",
            ratioLight >= 3.0
        )
        assertTrue(
            "Texto onPrimary sobre botão principal escuro deve ter contraste >= 3.0:1 (atual: $ratioDark)",
            ratioDark >= 3.0
        )
    }
}
