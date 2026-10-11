package com.platform.app.presentation.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.platform.app.core.util.CurrencyUtils

/**
 * Componente padronizado para exibição de valores monetários com tipografia tabular (tnum).
 * Garante alinhamento numérico exato e elimina trepidações em listas, dashboards e cards.
 */
@Composable
fun PlatformMoneyText(
    amountCents: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    val formatted = CurrencyUtils.formatCentsToCurrency(amountCents)
    val tabularStyle = style.copy(
        fontFeatureSettings = "tnum",
        fontWeight = fontWeight ?: style.fontWeight
    )

    Text(
        text = formatted,
        modifier = modifier,
        style = tabularStyle,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Extensão de conveniência para ativar números tabulares (tnum) em qualquer estilo tipográfico.
 */
fun TextStyle.withTabularNumbers(): TextStyle = this.copy(fontFeatureSettings = "tnum")
