package com.platform.app.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CuratedColorToken(
    val hex: String,
    val name: String,
    val family: String
)

/**
 * Paleta Harmonizada de 24 Cores Curadas para evitar a pulverização visual,
 * mantendo conformidade com as regras de design e alto contraste visual.
 */
object PlatformColorPalette {
    val TOKENS: List<CuratedColorToken> = listOf(
        // Família Azul & Índigo
        CuratedColorToken("#2563EB", "Azul Royal", "Azul"),
        CuratedColorToken("#1D4ED8", "Azul Cobalto", "Azul"),
        CuratedColorToken("#0284C7", "Azul Celeste", "Azul"),
        CuratedColorToken("#4F46E5", "Índigo Profundo", "Azul"),

        // Família Verde & Teal
        CuratedColorToken("#10B981", "Esmeralda", "Verde"),
        CuratedColorToken("#059669", "Verde Floresta", "Verde"),
        CuratedColorToken("#14B8A6", "Verde Turquesa", "Verde"),
        CuratedColorToken("#0D9488", "Teal Petróleo", "Verde"),

        // Família Âmbar & Quentes
        CuratedColorToken("#F59E0B", "Ouro Âmbar", "Âmbar"),
        CuratedColorToken("#D97706", "Tangerina", "Âmbar"),
        CuratedColorToken("#EA580C", "Cobre Quente", "Âmbar"),
        CuratedColorToken("#F97316", "Laranja Coral", "Âmbar"),

        // Família Vermelho & Vinho
        CuratedColorToken("#EF4444", "Vermelho Rubi", "Vermelho"),
        CuratedColorToken("#DC2626", "Carmesim", "Vermelho"),
        CuratedColorToken("#E11D48", "Vinho Marsala", "Vermelho"),
        CuratedColorToken("#BE123C", "Terracota Profunda", "Vermelho"),

        // Família Violeta & Rosa
        CuratedColorToken("#8B5CF6", "Roxo Violeta", "Roxo"),
        CuratedColorToken("#7C3AED", "Lavanda Escura", "Roxo"),
        CuratedColorToken("#D946EF", "Fúcsia", "Rosa"),
        CuratedColorToken("#EC4899", "Rosa Orquídea", "Rosa"),

        // Família Ardósia & Neutros Nobres
        CuratedColorToken("#475569", "Ardósia Frio", "Neutro"),
        CuratedColorToken("#334155", "Titânio Escuro", "Neutro"),
        CuratedColorToken("#64748B", "Grafite Neutro", "Neutro"),
        CuratedColorToken("#1E293B", "Obsidiana", "Neutro")
    )

    fun findToken(hex: String): CuratedColorToken? {
        return TOKENS.find { it.hex.equals(hex, ignoreCase = true) }
    }
}

/**
 * Seletor de cores em grade compacta, elegante e harmoniosa.
 */
@Composable
fun PlatformColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedToken = remember(selectedColorHex) {
        PlatformColorPalette.findToken(selectedColorHex)
    }

    Column(modifier = modifier) {
        // Rótulo da cor ativa selecionada
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cor de Identificação",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = selectedToken?.name ?: selectedColorHex.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grade de 24 Cores Curadas (6 colunas x 4 linhas)
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PlatformColorPalette.TOKENS, key = { it.hex }) { token ->
                val isSelected = selectedColorHex.equals(token.hex, ignoreCase = true)
                val color = remember(token.hex) {
                    try {
                        Color(android.graphics.Color.parseColor(token.hex))
                    } catch (e: Exception) {
                        BrandPrimary
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(color, RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Black.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onColorSelected(token.hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selecionada",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
