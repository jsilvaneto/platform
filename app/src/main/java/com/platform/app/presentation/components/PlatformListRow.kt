package com.platform.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.UrgentRed

/**
 * Componente padronizado para linhas de listagem com suporte a gestos contextuais de Swipe (Material 3):
 * - Swipe da Esquerda para Direita (StartToEnd): Ação de pagamento / quitação rápida (ícone Check verde).
 * - Swipe da Direita para Esquerda (EndToStart): Ação de exclusão rápida (ícone Delete vermelho com suporte a Undo).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                .then(
                    if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier
                )
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

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onSwipeToPay != null && enabled,
        enableDismissFromEndToStart = onSwipeToDelete != null && enabled,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> SuccessGreen
                    SwipeToDismissBoxValue.EndToStart -> UrgentRed
                    else -> Color.Transparent
                },
                label = "swipe_bg_color"
            )

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                else -> null
            }

            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1.15f,
                label = "swipe_icon_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = PlatformShapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(scale)
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier
                )
        ) {
            content()
        }
    }
}
