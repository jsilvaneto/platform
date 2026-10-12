package com.platform.app.presentation.components

import com.platform.app.presentation.theme.PlatformShapes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalDensity

data class SegmentedTabItem(
    val title: String,
    val count: Int? = null
)

/**
 * Abas segmentadas com estética minimalista de alto padrão (Obsidian & Porcelain).
 * Cantos estritamente controlados em 10.dp e transição de cor suave.
 */
@Composable
fun PlatformSegmentedTabs(
    items: List<SegmentedTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val fontScale = LocalDensity.current.fontScale
    val isLargeFont = fontScale > 1.15f

    PlatformSurface(
        modifier = modifier
            .fillMaxWidth()
            .clip(PlatformShapes.medium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = PlatformShapes.medium
    ) {
        val rowModifier = if (isLargeFont) {
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(3.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(3.dp)
        }

        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex

                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    label = "tabContainerColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tabTextColor"
                )

                val border = if (isSelected) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                } else {
                    null
                }

                val itemModifier = if (isLargeFont) {
                    Modifier
                        .heightIn(min = 44.dp)
                        .clip(PlatformShapes.small)
                        .clickable { onTabSelected(index) }
                } else {
                    Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(PlatformShapes.small)
                        .clickable { onTabSelected(index) }
                }

                PlatformSurface(
                    modifier = itemModifier,
                    shape = PlatformShapes.small,
                    color = containerColor,
                    border = border
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                maxLines = 1
                            )

                            if (item.count != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                PlatformSurface(
                                    shape = PlatformShapes.pill,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .widthIn(min = 18.dp)
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.count.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
