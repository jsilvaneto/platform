package com.platform.app.presentation.theme

import androidx.compose.ui.graphics.Color

// Paleta Escuro (Prompt 55: Minimalista Neutro)
val DarkBackground = PlatformTokens.Dark.background
val DarkSurface = PlatformTokens.Dark.surface1
val DarkSurfaceVariant = PlatformTokens.Dark.surface2
val DarkBorder = PlatformTokens.Dark.line
val DarkBorderSubtle = PlatformTokens.Dark.line
val DarkTextPrimary = PlatformTokens.Dark.text
val DarkTextSecondary = PlatformTokens.Dark.text2
val DarkTextMuted = PlatformTokens.Dark.text2

// Paleta Claro (Prompt 55: Minimalista Neutro)
val LightBackground = PlatformTokens.Light.background
val LightSurface = PlatformTokens.Light.surface1
val LightSurfaceVariant = PlatformTokens.Light.surface2
val LightBorder = PlatformTokens.Light.line
val LightBorderSubtle = PlatformTokens.Light.line
val LightTextPrimary = PlatformTokens.Light.text
val LightTextSecondary = PlatformTokens.Light.text2
val LightTextMuted = PlatformTokens.Light.text2

// Paleta AMOLED (Prompt 55: Preto Puro & Neutros)
val AmoledBackground = PlatformTokens.Amoled.background
val AmoledSurface = PlatformTokens.Amoled.surface1
val AmoledSurfaceVariant = PlatformTokens.Amoled.surface2
val AmoledBorder = PlatformTokens.Amoled.line
val AmoledTextPrimary = PlatformTokens.Amoled.text
val AmoledTextSecondary = PlatformTokens.Amoled.text2

// Marca e Destaque Neutros (Prompt 55: cor de texto como preenchimento)
val BrandPrimary = LightTextPrimary
val BrandPrimaryDark = DarkTextPrimary
val BrandPrimaryAmoled = AmoledTextPrimary

// Cores de status canônicas (acessíveis via PlatformStatusStyle)
val UrgentRed = PlatformTokens.StatusLight.overdue
val UrgentRedContainer = UrgentRed.copy(alpha = 0.12f)
val WarningAmber = PlatformTokens.StatusLight.attention
val WarningAmberContainer = WarningAmber.copy(alpha = 0.12f)
val SuccessGreen = PlatformTokens.StatusLight.paid
val SuccessGreenContainer = SuccessGreen.copy(alpha = 0.12f)

// Aliases mantidos para compatibilidade de compilação semântica
val ErrorRed = UrgentRed
val ErrorRedContainer = UrgentRedContainer
val SuccessGreenLightContainer = SuccessGreenContainer
val WarningAmberLightContainer = WarningAmberContainer
val PrimaryBlue = BrandPrimary
val PrimaryBlueDark = BrandPrimaryDark
val PrimaryBlueLight = BrandPrimaryDark
val InfoCyan = DarkTextSecondary
val InfoCyanContainer = DarkTextSecondary.copy(alpha = 0.12f)
