package com.platform.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType

/**
 * Linha padronizada de lista minimalista (Prompt 56):
 * - Ícone 36dp com raio 12 e fundo sup2 (neutro).
 * - Título 14sp bold.
 * - Subtítulo 12sp (colorido com cor de status se especificado).
 * - Valor 14sp bold à direita com [PlatformMoneyText].
 * - Chip de status abaixo do valor.
 * - Swipe para pagar (fundo verde semafórico com "Pagar").
 * - Suporte a seleção por long-press.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlatformListRow(
    title: String,
    subtitle: String,
    amountCents: Long,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    statusType: PlatformStatusType? = null,
    isSubtitleStatus: Boolean = false,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onSwipeToPay: (() -> Unit)? = null
) {
    val content = @Composable {
        val rowBg = if (isSelected) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBg, PlatformShapes.medium)
                .combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lado Esquerdo: Ícone 36dp com raio 12 e fundo sup2 + Título e Subtítulo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(PlatformShapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    val subtitleColor = if (isSubtitleStatus && statusType != null) {
                        PlatformStatusStyle.getColors(statusType).contentColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = subtitleColor,
                        maxLines = 1
                    )
                }
            }

            // Lado Direito: Valor 14sp bold e Chip de Status abaixo
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PlatformMoneyText(
                    amountCents = amountCents,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (statusText != null && statusType != null) {
                    PlatformStatusChip(
                        text = statusText,
                        type = when (statusType) {
                            PlatformStatusType.PAID -> StatusChipType.SUCCESS
                            PlatformStatusType.ATTENTION -> StatusChipType.WARNING
                            PlatformStatusType.OVERDUE -> StatusChipType.ERROR
                        }
                    )
                }
            }
        }
    }

    if (onSwipeToPay == null) {
        content()
        return
    }

    val currentOnPay by rememberUpdatedState(onSwipeToPay)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                currentOnPay()
                false
            } else {
                false
            }
        }
    )

    val paidColors = PlatformStatusStyle.getColors(PlatformStatusType.PAID)

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                    paidColors.contentColor
                } else {
                    paidColors.containerColor
                },
                label = "swipe_bg"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, PlatformShapes.medium)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Pagar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pagar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Overload genérico de PlatformListRow para layouts com conteúdo interno customizado.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlatformListRow(
    modifier: Modifier = Modifier,
    onSwipeToPay: (() -> Unit)? = null,
    onSwipeToDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (onSwipeToPay == null && onSwipeToDelete == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null && enabled) Modifier.combinedClickable(onClick = onClick) else Modifier)
        ) {
            content()
        }
        return
    }

    val currentOnPay by rememberUpdatedState(onSwipeToPay)
    val currentOnDelete by rememberUpdatedState(onSwipeToDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (currentOnPay != null) {
                        currentOnPay?.invoke()
                        false
                    } else false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (currentOnDelete != null) {
                        currentOnDelete?.invoke()
                        false
                    } else false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )
    val paidColors = PlatformStatusStyle.getColors(PlatformStatusType.PAID)
    val deleteColors = PlatformStatusStyle.getColors(PlatformStatusType.OVERDUE)

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onSwipeToPay != null && enabled,
        enableDismissFromEndToStart = onSwipeToDelete != null && enabled,
        backgroundContent = {
            val isStart = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val bgColor = if (isStart) paidColors.contentColor else deleteColors.contentColor
            val alignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
            val text = if (isStart) "Pagar" else "Excluir"

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor, PlatformShapes.medium)
                    .padding(horizontal = 16.dp),
                contentAlignment = alignment
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null && enabled) Modifier.combinedClickable(onClick = onClick) else Modifier)
        ) {
            content()
        }
    }
}

