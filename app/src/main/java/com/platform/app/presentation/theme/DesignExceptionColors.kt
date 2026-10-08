package com.platform.app.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Exceções Explícitas ao Design System Central do Platform.
 *
 * Este arquivo isola e documenta de forma centralizada os únicos agrupamentos de cores
 * estáticas que intencionalmente NÃO seguem a alternância dinâmica de Dark/Light mode
 * provida por [MaterialTheme.colorScheme].
 *
 * ### Justificativas Arquiteturais:
 *
 * 1. [ThemePreviewColors]: Miniaturas visuais de temas na tela de configurações (Settings).
 *    Cada miniatura deve retratar com precisão milimétrica a identidade cromática e o contraste
 *    específico do respectivo tema (ex: Obsidian é conceitualmente preto absoluto com detalhes
 *    âmbar/dourados; Classic possui marinho profundo e azul royal; Emerald é verde teal profundo).
 *    Se essas miniaturas consumissem tokens dinâmicos do tema em execução, elas perderiam sua
 *    função primordial: apresentar visualmente ao usuário a aparência do tema ANTES da troca.
 *
 * 2. [CardSkinColors]: Substrato e acabamentos do Cartão de Crédito virtual/físico (PlatformCreditCardView).
 *    Cartões bancários de alta categoria possuem identidade skeuomórfica física estabelecida
 *    (acabamento acetinado escuro, contatos metálicos de chip EMV em ouro/bronze polido, e tipografia
 *    de alto contraste). Para garantir legibilidade e consistência visual independente do tema
 *    do sistema operacional ou do aplicativo, esses elementos mantêm sua renderização de alto contraste.
 */
object ThemePreviewColors {
    // --- Tema Classic (Marinho & Azul Royal) ---
    val ClassicBackground = Color(0xFF0B132B)
    val ClassicCard = Color(0xFF2563EB)
    val ClassicAccent = Color(0xFF3B82F6)
    val ClassicBadge = Color(0xFF10B981)
    val ClassicDot = Color(0xFFFFFFFF)

    // --- Tema Modern V2 (Deep Dark & Gradiente Tecnológico) ---
    val ModernV2Background = Color(0xFF0A0F1D)
    val ModernV2Bar1 = Color(0xFF2563EB)
    val ModernV2Bar2 = Color(0xFF3B82F6)
    val ModernV2Dot = Color(0xFF10B981)
    val ModernV2Bar3 = Color(0xFF06B6D4)

    // --- Tema Emerald (Teal Petróleo & Esmeralda) ---
    val EmeraldBackground = Color(0xFF042F2E)
    val EmeraldCard = Color(0xFF0D9488)
    val EmeraldAccent = Color(0xFF14B8A6)
    val EmeraldDot = Color(0xFF34D399)

    // --- Tema Obsidian (Preto Puro & Ouro Âmbar) ---
    val ObsidianBackground = Color(0xFF000000)
    val ObsidianCard = Color(0xFF1C1917)
    val ObsidianAccent = Color(0xFFD97706)
    val ObsidianDot = Color(0xFFFBBF24)
}

/**
 * Cores do substrato, chip metálico EMV e elementos de acabamento do Cartão de Crédito.
 */
object CardSkinColors {
    // Substrato de base para gradiente escuro do cartão
    val BaseGradientDark = Color(0xFF121418)

    // Chip EMV Metálico Dourado
    val EmvChipBase = Color(0xFFD4AF37)
    val EmvChipBorder = Color(0xFFB8860B)
    val EmvChipContactLine = Color(0xFF8B7500)

    // Tipografia de Alto Contraste sobre cartão escuro
    val TextPrimary = Color(0xFFFFFFFF)
    val TextMuted = Color(0xCCFFFFFF) // 80% alpha
    val TextSubtle = Color(0xBFFFFFFF) // 75% alpha
    val TrackBackground = Color(0x33FFFFFF) // 20% alpha

    // Controles e Ações Rápidas do Cartão
    val ActionOverlay = Color(0x40000000) // 25% alpha
    val ActionIcon = Color(0xE6FFFFFF) // 90% alpha
    val ContactlessIcon = Color(0xBFFFFFFF) // 75% alpha
}
