package com.platform.app.presentation.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.CurrencyUtils

/**
 * Componente padronizado para exibição de valores monetários com tipografia tabular (tnum)
 * e centavos menores em texto2 (Prompt 56).
 *
 * Garante alinhamento numérico exato, sobriedade financeira e elimina trepidações.
 */
@Composable
fun PlatformMoneyText(
    amountCents: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    centsColor: Color? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    val formatted = CurrencyUtils.formatCentsToCurrency(amountCents)
    val parts = formatted.split(",")
    val mainPart = parts.getOrNull(0) ?: formatted
    val centsPart = if (parts.size > 1) ",${parts[1]}" else ""

    val primaryColor = if (color != Color.Unspecified) color else LocalTextStyle.current.color.takeIf { it != Color.Unspecified } ?: MaterialTheme.colorScheme.onSurface
    val secondaryColor = centsColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    val baseFontSize = style.fontSize.takeIf { it.isSp } ?: 14.sp
    val centsFontSize = (baseFontSize.value * 0.82f).sp

    val annotatedString = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = primaryColor,
                fontWeight = fontWeight ?: style.fontWeight ?: FontWeight.Bold,
                fontSize = baseFontSize,
                fontFeatureSettings = "tnum"
            )
        ) {
            append(mainPart)
        }
        if (centsPart.isNotEmpty()) {
            withStyle(
                SpanStyle(
                    color = secondaryColor,
                    fontWeight = fontWeight ?: style.fontWeight ?: FontWeight.Normal,
                    fontSize = centsFontSize,
                    fontFeatureSettings = "tnum"
                )
            ) {
                append(centsPart)
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Extensão de conveniência para ativar números tabulares (tnum) em qualquer estilo tipográfico.
 */
fun TextStyle.withTabularNumbers(): TextStyle = this.copy(fontFeatureSettings = "tnum")
