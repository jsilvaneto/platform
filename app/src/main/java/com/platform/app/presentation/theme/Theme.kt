package com.platform.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextPrimaryDark,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = InfoCyan,
    onSecondary = TextPrimaryDark,
    secondaryContainer = InfoCyanContainer,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = SuccessGreen,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = SuccessGreenContainer,
    onTertiaryContainer = TextPrimaryDark,
    background = SlateBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorderDark,
    outlineVariant = SlateSurfaceVariantDark,
    error = ErrorRed,
    onError = TextPrimaryDark,
    errorContainer = ErrorRedContainer,
    onErrorContainer = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SlateSurfaceLight,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = InfoCyan,
    onSecondary = SlateSurfaceLight,
    secondaryContainer = InfoCyanLightContainer,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = SuccessGreen,
    onTertiary = SlateSurfaceLight,
    tertiaryContainer = SuccessGreenLightContainer,
    onTertiaryContainer = TextPrimaryLight,
    background = SlateBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SlateBorderLight,
    outlineVariant = SlateBorderLight,
    error = ErrorRed,
    onError = SlateSurfaceLight,
    errorContainer = ErrorRedLightContainer,
    onErrorContainer = TextPrimaryLight
)

@Composable
fun PlatformTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
