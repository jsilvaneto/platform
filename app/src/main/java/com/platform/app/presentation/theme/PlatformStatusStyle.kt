package com.platform.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class PlatformStatusType {
    PAID,
    ATTENTION,
    OVERDUE
}

data class PlatformStatusColors(
    val contentColor: Color,
    val containerColor: Color,
    val borderColor: Color
)

/**
 * Ponto de acesso canônico e único para cores de status no app (Prompt 55).
 * Proibido o uso de cores semafóricas diretas em telas ou componentes fora deste estilo.
 */
object PlatformStatusStyle {

    @Composable
    fun getColors(status: PlatformStatusType, isDark: Boolean = isSystemInDarkTheme()): PlatformStatusColors {
        return resolveColors(status, isDark)
    }

    fun resolveColors(status: PlatformStatusType, isDark: Boolean): PlatformStatusColors {
        val baseColor = if (isDark) {
            when (status) {
                PlatformStatusType.PAID -> PlatformTokens.StatusDark.paid
                PlatformStatusType.ATTENTION -> PlatformTokens.StatusDark.attention
                PlatformStatusType.OVERDUE -> PlatformTokens.StatusDark.overdue
            }
        } else {
            when (status) {
                PlatformStatusType.PAID -> PlatformTokens.StatusLight.paid
                PlatformStatusType.ATTENTION -> PlatformTokens.StatusLight.attention
                PlatformStatusType.OVERDUE -> PlatformTokens.StatusLight.overdue
            }
        }

        return PlatformStatusColors(
            contentColor = baseColor,
            containerColor = baseColor.copy(alpha = if (isDark) 0.16f else 0.10f),
            borderColor = baseColor.copy(alpha = 0.35f)
        )
    }
}
