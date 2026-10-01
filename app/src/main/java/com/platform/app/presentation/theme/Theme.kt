package com.platform.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1E3A8A).copy(alpha = 0.5f),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFE2E8F0),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = Color(0xFF070A0F),
    surfaceContainerLow = Color(0xFF0E121B),
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = Color(0xFF1A212E),
    surfaceContainerHighest = Color(0xFF222B3D),
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = UrgentRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = UrgentRedContainer,
    onErrorContainer = Color(0xFFFECACA),
    scrim = Color(0xFF000000),
    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF334155),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = LightSurface,
    surfaceContainerHigh = Color(0xFFF1F5F9),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = UrgentRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = UrgentRedContainer,
    onErrorContainer = Color(0xFF991B1B),
    scrim = Color(0xFF000000),
    surfaceTint = Color.Transparent
)

private val AmoledDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E3A8A).copy(alpha = 0.5f),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF18181B),
    onSecondaryContainer = Color(0xFFE4E4E7),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF09090B),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFA1A1AA),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF050507),
    surfaceContainer = Color(0xFF09090B),
    surfaceContainerHigh = Color(0xFF121215),
    surfaceContainerHighest = Color(0xFF18181B),
    outline = Color(0xFF27272A),
    outlineVariant = Color(0xFF1E1E22),
    error = UrgentRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = UrgentRedContainer,
    onErrorContainer = Color(0xFFFECACA),
    scrim = Color(0xFF000000),
    surfaceTint = Color.Transparent
)

@Composable
fun PlatformTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme && isAmoled -> AmoledDarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.surface.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) = PlatformTheme(darkTheme = darkTheme, isAmoled = isAmoled, content = content)
