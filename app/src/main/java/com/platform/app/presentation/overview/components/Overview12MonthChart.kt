package com.platform.app.presentation.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.domain.model.MonthlyBarData
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalDensity

/**
 * Gráfico minimalista de 12 meses da Visão Geral (Prompt 58):
 * - 2 passados (cor de linha), mês atual (rótulo em negrito), 9 futuros (neutro 2).
 * - Pico futuro em âmbar e folga (menor futuro) em verde.
 * - Legenda horizontal minimalista nas mesmas cores.
 */
@Composable
fun Overview12MonthChart(
    monthlySeries: List<MonthlyBarData>,
    modifier: Modifier = Modifier
) {
    if (monthlySeries.isEmpty()) return

    val fontScale = LocalDensity.current.fontScale
    val isLargeFont = fontScale > 1.25f

    val isDark = isSystemInDarkTheme()
    val peakColor = PlatformStatusStyle.getColors(PlatformStatusType.ATTENTION, isDark).contentColor
    val reliefColor = PlatformStatusStyle.getColors(PlatformStatusType.PAID, isDark).contentColor
    val lineColor = MaterialTheme.colorScheme.outline
    val neutral2Color = MaterialTheme.colorScheme.surfaceVariant

    val maxAmount = monthlySeries.maxOfOrNull { it.amountCents } ?: 0L
    val chartHeight = 84.dp

    PlatformCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho com Título e Legenda
            if (isLargeFont) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlatformSectionTitle(text = AppStrings.Overview.MONTHLY_SERIES_TITLE)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(peakColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.Overview.PEAK,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(reliefColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.Overview.RELIEF,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlatformSectionTitle(text = AppStrings.Overview.MONTHLY_SERIES_TITLE)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(peakColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.Overview.PEAK,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(reliefColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.Overview.RELIEF,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Linha das 12 barras
            val chartRowModifier = if (isLargeFont) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = chartHeight + 36.dp)
                    .horizontalScroll(rememberScrollState())
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = chartHeight + 28.dp)
            }

            Row(
                modifier = chartRowModifier,
                horizontalArrangement = if (isLargeFont) Arrangement.spacedBy(8.dp) else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlySeries.forEach { bar ->
                    val barHeight = if (maxAmount > 0L) {
                        (chartHeight.value * (bar.amountCents.toFloat() / maxAmount.toFloat()))
                            .coerceIn(4f, chartHeight.value).dp
                    } else {
                        4.dp
                    }

                    val barColor = when {
                        bar.isPeak -> peakColor
                        bar.isLowestFuture -> reliefColor
                        bar.isPast -> lineColor
                        bar.isCurrent -> MaterialTheme.colorScheme.primary
                        else -> neutral2Color
                    }

                    val colModifier = if (isLargeFont) Modifier.width(36.dp) else Modifier.weight(1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = colModifier
                    ) {
                        Box(
                            modifier = Modifier
                                .height(chartHeight)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(barHeight)
                                    .clip(PlatformShapes.small)
                                    .background(barColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (bar.isCurrent) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (bar.isCurrent) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
