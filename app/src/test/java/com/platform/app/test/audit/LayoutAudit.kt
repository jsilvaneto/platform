package com.platform.app.test.audit

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult

/**
 * Auditoria Automática de Layout em Semantics do Jetpack Compose (Prompt 80).
 * Valida integridade tipográfica, ausência de overflow visual, não-quebra de valores monetários,
 * limites de tela e inexistência de sobreposição de textos.
 */
object LayoutAudit {

    data class LayoutViolation(
        val type: String,
        val text: String,
        val details: String
    ) {
        override fun toString(): String = "[$type] \"${text.take(50)}\": $details"
    }

    /**
     * Executa a auditoria completa na árvore semântica da regra de teste atual.
     * Lança [AssertionError] caso qualquer defeito seja detectado.
     */
    fun assertNoLayoutDefects(
        rule: ComposeContentTestRule,
        screenName: String,
        themeName: String = "default",
        fontScale: Float = 1.0f
    ) {
        val rootNode = rule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val allNodes = mutableListOf<SemanticsNode>()
        collectAllNodes(rootNode, allNodes)

        val textNodes = allNodes.filter { hasTextOrLayoutResult(it) }
        val violations = mutableListOf<LayoutViolation>()

        // 1. Auditoria individual de nós de texto
        textNodes.forEach { node ->
            auditTextNode(node, rootNode, violations)
        }

        // 2. Auditoria de sobreposição entre pares de nós de texto
        auditTextOverlaps(textNodes, violations)

        if (violations.isNotEmpty()) {
            val report = buildString {
                appendLine("LayoutAudit FAILED em $screenName [Tema: $themeName, Fonte: ${fontScale}x]")
                appendLine("Detectados ${violations.size} defeito(s) de layout:")
                violations.forEach { appendLine("  • $it") }
            }
            throw AssertionError(report)
        }
    }

    private fun collectAllNodes(node: SemanticsNode, result: MutableList<SemanticsNode>) {
        result.add(node)
        for (child in node.children) {
            collectAllNodes(child, result)
        }
    }

    private fun hasTextOrLayoutResult(node: SemanticsNode): Boolean {
        return node.config.getOrNull(SemanticsActions.GetTextLayoutResult) != null ||
                node.config.getOrNull(SemanticsProperties.Text) != null
    }

    private fun getNodeText(node: SemanticsNode, layoutResult: TextLayoutResult?): String {
        val semanticsText = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
        if (!semanticsText.isNullOrBlank()) return semanticsText
        return layoutResult?.layoutInput?.text?.text ?: ""
    }

    private fun auditTextNode(
        node: SemanticsNode,
        rootNode: SemanticsNode,
        violations: MutableList<LayoutViolation>
    ) {
        val layoutResults = mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layoutResults)
        val layoutResult = layoutResults.firstOrNull() ?: return

        val text = getNodeText(node, layoutResult).trim()
        if (text.isEmpty()) return

        // Regra 1: Overflow visual (altura cortada verticalmente ou linhas excedendo largura)
        val hasWidthOverflow = (0 until layoutResult.lineCount).any { line ->
            (layoutResult.getLineRight(line) - layoutResult.getLineLeft(line)) > layoutResult.size.width + 4f
        }
        if (layoutResult.didOverflowHeight || hasWidthOverflow) {
            violations.add(
                LayoutViolation(
                    type = "OVERFLOW_VISUAL",
                    text = text,
                    details = "didOverflowHeight=${layoutResult.didOverflowHeight}, hasWidthOverflow=$hasWidthOverflow, size=${layoutResult.size}"
                )
            )
        }

        // Regra 2: Quebra extrema no meio da palavra / texto quase vertical
        // (Texto com 6+ caracteres e menos de 4 caracteres por linha em média)
        if (text.length >= 6 && layoutResult.lineCount > 1) {
            val avgCharsPerLine = text.length.toFloat() / layoutResult.lineCount
            if (avgCharsPerLine < 4.0f) {
                violations.add(
                    LayoutViolation(
                        type = "QUEBRA_NO_MEIO_DA_PALAVRA",
                        text = text,
                        details = "Média de %.1f caracteres por linha em %d linhas (< 4.0)".format(avgCharsPerLine, layoutResult.lineCount)
                    )
                )
            }
        }

        // Regra 3: Valores monetários ou percentuais ocupando mais de uma linha
        val isFinancialOrPercentage = text.startsWith("R$") || text.contains("R$ ") ||
                (text.contains("%") && text.any { it.isDigit() })

        if (isFinancialOrPercentage && layoutResult.lineCount > 1) {
            // Se for um valor conciso (ex: "R$ 1.500,00" ou "35%")
            // Não deve quebrar em 2 linhas salvo se for uma frase longa que contém moeda
            val isConciseValue = text.length <= 25 || text.startsWith("R$")
            if (isConciseValue) {
                violations.add(
                    LayoutViolation(
                        type = "VALOR_MONETARIO_QUEBRADO",
                        text = text,
                        details = "Valor financeiro ou percentual quebrou em ${layoutResult.lineCount} linhas"
                    )
                )
            }
        }

        // Regra 4: Limites horizontais da tela
        val bounds = node.boundsInRoot
        val rootBounds = rootNode.boundsInRoot

        val isScrollableX = isInsideHorizontalScroll(node)
        if (!isScrollableX) {
            // Tolerância de 2 pixels para subpixel rounding em densidades fracionárias
            if (bounds.left < -2f || bounds.right > rootBounds.width + 2f) {
                violations.add(
                    LayoutViolation(
                        type = "TEXTO_FORA_DA_TELA",
                        text = text,
                        details = "Coordenadas [left=%.1f, right=%.1f] excedem tela [0, %.1f]".format(bounds.left, bounds.right, rootBounds.width)
                    )
                )
            }
        }
    }

    private fun auditTextOverlaps(
        textNodes: List<SemanticsNode>,
        violations: MutableList<LayoutViolation>
    ) {
        val count = textNodes.size
        for (i in 0 until count) {
            val nodeA = textNodes[i]
            val boundsA = nodeA.boundsInRoot
            if (boundsA.width <= 0 || boundsA.height <= 0) continue

            for (j in i + 1 until count) {
                val nodeB = textNodes[j]
                val boundsB = nodeB.boundsInRoot
                if (boundsB.width <= 0 || boundsB.height <= 0) continue

                // Se houver parentesco direto (ancestral/descendente), aninhamento é intencional
                if (isAncestorOrDescendant(nodeA, nodeB)) continue

                // Checagem de sobreposição em ambos os eixos
                val overlapLeft = maxOf(boundsA.left, boundsB.left)
                val overlapRight = minOf(boundsA.right, boundsB.right)
                val overlapTop = maxOf(boundsA.top, boundsB.top)
                val overlapBottom = minOf(boundsA.bottom, boundsB.bottom)

                val overlapWidth = overlapRight - overlapLeft
                val overlapHeight = overlapBottom - overlapTop

                // Tolerância de sobreposição > 4px para ignorar toques de borda
                if (overlapWidth > 4f && overlapHeight > 4f) {
                    val textA = getNodeText(nodeA, null)
                    val textB = getNodeText(nodeB, null)
                    violations.add(
                        LayoutViolation(
                            type = "SOBREPOSICAO_DE_TEXTOS",
                            text = "$textA <-> $textB",
                            details = "Sobreposição de %.1f x %.1f px".format(overlapWidth, overlapHeight)
                        )
                    )
                }
            }
        }
    }

    private fun isInsideHorizontalScroll(node: SemanticsNode): Boolean {
        var current: SemanticsNode? = node
        while (current != null) {
            if (current.config.getOrNull(SemanticsProperties.HorizontalScrollAxisRange) != null) {
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun isAncestorOrDescendant(a: SemanticsNode, b: SemanticsNode): Boolean {
        return isAncestor(a, b) || isAncestor(b, a)
    }

    private fun isAncestor(ancestor: SemanticsNode, descendant: SemanticsNode): Boolean {
        var current = descendant.parent
        while (current != null) {
            if (current.id == ancestor.id) return true
            current = current.parent
        }
        return false
    }
}
