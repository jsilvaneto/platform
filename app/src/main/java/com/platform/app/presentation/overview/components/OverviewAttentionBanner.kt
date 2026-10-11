package com.platform.app.presentation.overview.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

/**
 * Banner de atenção da Visão Geral (Prompt 58):
 * Notifica sobre contas atrasadas ou vencendo hoje. Some se attentionCount == 0.
 * Redireciona para a tela Hoje.
 */
@Composable
fun OverviewAttentionBanner(
    attentionCount: Int,
    onNavigateToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (attentionCount <= 0) return

    val isDark = isSystemInDarkTheme()
    val statusColors = PlatformStatusStyle.getColors(PlatformStatusType.ATTENTION, isDark)

    PlatformSurface(
        shape = PlatformShapes.medium,
        color = statusColors.containerColor,
        borderColor = statusColors.borderColor,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToToday)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = statusColors.contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${AppStrings.Overview.ATTENTION_PREFIX}$attentionCount${AppStrings.Overview.ATTENTION_SUFFIX}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = statusColors.contentColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = AppStrings.Overview.ATTENTION_ACTION,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColors.contentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = statusColors.contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
