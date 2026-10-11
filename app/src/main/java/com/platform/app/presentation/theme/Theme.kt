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
    primary = PlatformTokens.Dark.text,
    onPrimary = PlatformTokens.Dark.background,
    primaryContainer = PlatformTokens.Dark.surface2,
    onPrimaryContainer = PlatformTokens.Dark.text,
    secondary = PlatformTokens.Dark.text2,
    onSecondary = PlatformTokens.Dark.background,
    secondaryContainer = PlatformTokens.Dark.surface2,
    onSecondaryContainer = PlatformTokens.Dark.text,
    background = PlatformTokens.Dark.background,
    onBackground = PlatformTokens.Dark.text,
    surface = PlatformTokens.Dark.surface1,
    onSurface = PlatformTokens.Dark.text,
    surfaceVariant = PlatformTokens.Dark.surface2,
    onSurfaceVariant = PlatformTokens.Dark.text2,
    surfaceContainerLowest = PlatformTokens.Dark.background,
    surfaceContainerLow = PlatformTokens.Dark.background,
    surfaceContainer = PlatformTokens.Dark.surface1,
    surfaceContainerHigh = PlatformTokens.Dark.surface2,
    surfaceContainerHighest = PlatformTokens.Dark.surface2,
    outline = PlatformTokens.Dark.line,
    outlineVariant = PlatformTokens.Dark.line,
    error = PlatformTokens.StatusDark.overdue,
    onError = PlatformTokens.Dark.background,
    errorContainer = PlatformTokens.StatusDark.overdue.copy(alpha = 0.15f),
    onErrorContainer = PlatformTokens.StatusDark.overdue,
    scrim = Color(0xFF000000),
    surfaceTint = Color.Transparent
)

private val LightColorScheme = lightColorScheme(
    primary = PlatformTokens.Light.text,
    onPrimary = PlatformTokens.Light.surface1,
    primaryContainer = PlatformTokens.Light.surface2,
    onPrimaryContainer = PlatformTokens.Light.text,
    secondary = PlatformTokens.Light.text2,
    onSecondary = PlatformTokens.Light.surface1,
    secondaryContainer = PlatformTokens.Light.surface2,
    onSecondaryContainer = PlatformTokens.Light.text,
    background = PlatformTokens.Light.background,
    onBackground = PlatformTokens.Light.text,
    surface = PlatformTokens.Light.surface1,
    onSurface = PlatformTokens.Light.text,
    surfaceVariant = PlatformTokens.Light.surface2,
    onSurfaceVariant = PlatformTokens.Light.text2,
    surfaceContainerLowest = PlatformTokens.Light.surface1,
    surfaceContainerLow = PlatformTokens.Light.background,
    surfaceContainer = PlatformTokens.Light.surface1,
    surfaceContainerHigh = PlatformTokens.Light.surface2,
    surfaceContainerHighest = PlatformTokens.Light.line,
    outline = PlatformTokens.Light.line,
    outlineVariant = PlatformTokens.Light.line,
    error = PlatformTokens.StatusLight.overdue,
    onError = PlatformTokens.Light.surface1,
    errorContainer = PlatformTokens.StatusLight.overdue.copy(alpha = 0.10f),
    onErrorContainer = PlatformTokens.StatusLight.overdue,
    scrim = Color(0xFF000000),
    surfaceTint = Color.Transparent
)

private val AmoledDarkColorScheme = darkColorScheme(
    primary = PlatformTokens.Amoled.text,
    onPrimary = PlatformTokens.Amoled.background,
    primaryContainer = PlatformTokens.Amoled.surface2,
    onPrimaryContainer = PlatformTokens.Amoled.text,
    secondary = PlatformTokens.Amoled.text2,
    onSecondary = PlatformTokens.Amoled.background,
    secondaryContainer = PlatformTokens.Amoled.surface2,
    onSecondaryContainer = PlatformTokens.Amoled.text,
    background = PlatformTokens.Amoled.background,
    onBackground = PlatformTokens.Amoled.text,
    surface = PlatformTokens.Amoled.surface1,
    onSurface = PlatformTokens.Amoled.text,
    surfaceVariant = PlatformTokens.Amoled.surface2,
    onSurfaceVariant = PlatformTokens.Amoled.text2,
    surfaceContainerLowest = PlatformTokens.Amoled.background,
    surfaceContainerLow = PlatformTokens.Amoled.background,
    surfaceContainer = PlatformTokens.Amoled.surface1,
    surfaceContainerHigh = PlatformTokens.Amoled.surface2,
    surfaceContainerHighest = PlatformTokens.Amoled.surface2,
    outline = PlatformTokens.Amoled.line,
    outlineVariant = PlatformTokens.Amoled.line,
    error = PlatformTokens.StatusDark.overdue,
    onError = PlatformTokens.Amoled.background,
    errorContainer = PlatformTokens.StatusDark.overdue.copy(alpha = 0.15f),
    onErrorContainer = PlatformTokens.StatusDark.overdue,
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
                it.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(it, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PlatformShapes.Material3Shapes,
        content = content
    )
}

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    PlatformTheme(darkTheme = darkTheme, isAmoled = isAmoled, content = content)
}
