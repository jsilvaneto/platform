package com.platform.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

enum class StatusChipType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL
}

/**
 * Chip padronizado exclusivo para cores semânticas de status (Prompt 56).
 * Todo chip colorido no app é obrigatoriamente um chip de status.
 */
@Composable
fun PlatformStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    type: StatusChipType = StatusChipType.NEUTRAL,
    icon: ImageVector? = null
) {
    if (type == StatusChipType.NEUTRAL) {
        PlatformNeutralChip(
            text = text,
            modifier = modifier,
            leadingIcon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        )
        return
    }

    val isDark = isSystemInDarkTheme()
    val statusType = when (type) {
        StatusChipType.SUCCESS -> PlatformStatusType.PAID
        StatusChipType.WARNING -> PlatformStatusType.ATTENTION
        StatusChipType.ERROR -> PlatformStatusType.OVERDUE
        StatusChipType.INFO -> PlatformStatusType.ATTENTION
        StatusChipType.NEUTRAL -> PlatformStatusType.PAID
    }
    val statusColors = PlatformStatusStyle.getColors(statusType, isDark)

    PlatformSurface(
        shape = PlatformShapes.small,
        color = statusColors.containerColor,
        borderColor = statusColors.borderColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColors.contentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColors.contentColor
            )
        }
    }
}

/**
 * Chip neutro para categorias, filtros e informações estruturais (Prompt 56).
 * Fundo sup2 e cor texto2.
 */
@Composable
fun PlatformNeutralChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val border = if (selected) null else BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )

    PlatformSurface(
        shape = PlatformShapes.small,
        color = containerColor,
        border = border,
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
