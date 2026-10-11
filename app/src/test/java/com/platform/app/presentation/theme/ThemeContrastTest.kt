package com.platform.app.presentation.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Teste automatizado de contraste WCAG 2.1 AA para os temas minimalistas do Platform (Prompt 55).
 * Valida a conformidade de acessibilidade para:
 * - Texto primário e secundário (mínimo 4.5:1 normal)
 * - Cores de status sobre fundos e superfícies nos 3 temas
 * - Botão principal neutro
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
    fun lightTheme_meetsWcagAA() {
        val bg = PlatformTokens.Light.background
        val s1 = PlatformTokens.Light.surface1
        val s2 = PlatformTokens.Light.surface2
        val text = PlatformTokens.Light.text
        val text2 = PlatformTokens.Light.text2

        val ratioTextBg = contrastRatio(text, bg)
        val ratioTextS1 = contrastRatio(text, s1)
        val ratioTextS2 = contrastRatio(text, s2)
        assertTrue("Texto claro sobre fundo deve ter >= 4.5:1 ($ratioTextBg)", ratioTextBg >= 4.5)
        assertTrue("Texto claro sobre sup1 deve ter >= 4.5:1 ($ratioTextS1)", ratioTextS1 >= 4.5)
        assertTrue("Texto claro sobre sup2 deve ter >= 4.5:1 ($ratioTextS2)", ratioTextS2 >= 4.5)

        val ratioText2Bg = contrastRatio(text2, bg)
        val ratioText2S1 = contrastRatio(text2, s1)
        assertTrue("Texto2 claro sobre fundo deve ter >= 4.5:1 ($ratioText2Bg)", ratioText2Bg >= 4.5)
        assertTrue("Texto2 claro sobre sup1 deve ter >= 4.5:1 ($ratioText2S1)", ratioText2S1 >= 4.5)

        // Status claro
        val ratioOverdue = contrastRatio(PlatformTokens.StatusLight.overdue, s1)
        val ratioAttention = contrastRatio(PlatformTokens.StatusLight.attention, s1)
        val ratioPaid = contrastRatio(PlatformTokens.StatusLight.paid, s1)
        assertTrue("Atrasado claro deve ter >= 4.5:1 ($ratioOverdue)", ratioOverdue >= 4.5)
        assertTrue("Atenção claro deve ter >= 4.5:1 ($ratioAttention)", ratioAttention >= 4.5)
        assertTrue("Pago claro em chip/componente UI deve ter >= 3.0:1 ($ratioPaid)", ratioPaid >= 3.0)
    }

    @Test
    fun darkTheme_meetsWcagAA() {
        val bg = PlatformTokens.Dark.background
        val s1 = PlatformTokens.Dark.surface1
        val s2 = PlatformTokens.Dark.surface2
        val text = PlatformTokens.Dark.text
        val text2 = PlatformTokens.Dark.text2

        val ratioTextBg = contrastRatio(text, bg)
        val ratioTextS1 = contrastRatio(text, s1)
        val ratioTextS2 = contrastRatio(text, s2)
        assertTrue("Texto escuro sobre fundo deve ter >= 4.5:1 ($ratioTextBg)", ratioTextBg >= 4.5)
        assertTrue("Texto escuro sobre sup1 deve ter >= 4.5:1 ($ratioTextS1)", ratioTextS1 >= 4.5)
        assertTrue("Texto escuro sobre sup2 deve ter >= 4.5:1 ($ratioTextS2)", ratioTextS2 >= 4.5)

        val ratioText2Bg = contrastRatio(text2, bg)
        val ratioText2S1 = contrastRatio(text2, s1)
        assertTrue("Texto2 escuro sobre fundo deve ter >= 4.5:1 ($ratioText2Bg)", ratioText2Bg >= 4.5)
        assertTrue("Texto2 escuro sobre sup1 deve ter >= 4.5:1 ($ratioText2S1)", ratioText2S1 >= 4.5)

        // Status escuro
        val ratioPaid = contrastRatio(PlatformTokens.StatusDark.paid, s1)
        val ratioAttention = contrastRatio(PlatformTokens.StatusDark.attention, s1)
        val ratioOverdue = contrastRatio(PlatformTokens.StatusDark.overdue, s1)
        assertTrue("Pago escuro deve ter >= 4.5:1 ($ratioPaid)", ratioPaid >= 4.5)
        assertTrue("Atenção escuro deve ter >= 4.5:1 ($ratioAttention)", ratioAttention >= 4.5)
        assertTrue("Atrasado escuro deve ter >= 4.5:1 ($ratioOverdue)", ratioOverdue >= 4.5)
    }

    @Test
    fun amoledTheme_meetsWcagAA() {
        val bg = PlatformTokens.Amoled.background
        val s1 = PlatformTokens.Amoled.surface1
        val s2 = PlatformTokens.Amoled.surface2
        val text = PlatformTokens.Amoled.text
        val text2 = PlatformTokens.Amoled.text2

        val ratioTextBg = contrastRatio(text, bg)
        val ratioTextS1 = contrastRatio(text, s1)
        val ratioTextS2 = contrastRatio(text, s2)
        assertTrue("Texto AMOLED sobre fundo deve ter >= 4.5:1 ($ratioTextBg)", ratioTextBg >= 4.5)
        assertTrue("Texto AMOLED sobre sup1 deve ter >= 4.5:1 ($ratioTextS1)", ratioTextS1 >= 4.5)
        assertTrue("Texto AMOLED sobre sup2 deve ter >= 4.5:1 ($ratioTextS2)", ratioTextS2 >= 4.5)

        val ratioText2Bg = contrastRatio(text2, bg)
        val ratioText2S1 = contrastRatio(text2, s1)
        assertTrue("Texto2 AMOLED sobre fundo deve ter >= 4.5:1 ($ratioText2Bg)", ratioText2Bg >= 4.5)
        assertTrue("Texto2 AMOLED sobre sup1 deve ter >= 4.5:1 ($ratioText2S1)", ratioText2S1 >= 4.5)

        // Status AMOLED
        val ratioPaid = contrastRatio(PlatformTokens.StatusDark.paid, s1)
        val ratioAttention = contrastRatio(PlatformTokens.StatusDark.attention, s1)
        val ratioOverdue = contrastRatio(PlatformTokens.StatusDark.overdue, s1)
        assertTrue("Pago AMOLED deve ter >= 4.5:1 ($ratioPaid)", ratioPaid >= 4.5)
        assertTrue("Atenção AMOLED deve ter >= 4.5:1 ($ratioAttention)", ratioAttention >= 4.5)
        assertTrue("Atrasado AMOLED deve ter >= 4.5:1 ($ratioOverdue)", ratioOverdue >= 4.5)
    }

    @Test
    fun primaryButtons_neutralContrastMeetsWcagAA() {
        // Botão primário neutro: preenchimento de texto com conteúdo de fundo/sup1
        val ratioLight = contrastRatio(PlatformTokens.Light.surface1, PlatformTokens.Light.text)
        val ratioDark = contrastRatio(PlatformTokens.Dark.background, PlatformTokens.Dark.text)
        val ratioAmoled = contrastRatio(PlatformTokens.Amoled.background, PlatformTokens.Amoled.text)

        assertTrue("Botão neutro claro deve ter >= 4.5:1 ($ratioLight)", ratioLight >= 4.5)
        assertTrue("Botão neutro escuro deve ter >= 4.5:1 ($ratioDark)", ratioDark >= 4.5)
        assertTrue("Botão neutro AMOLED deve ter >= 4.5:1 ($ratioAmoled)", ratioAmoled >= 4.5)
    }
}
