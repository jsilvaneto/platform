package com.platform.app.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PayableItem
import com.platform.app.domain.model.PayableUrgency
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.formatValueOrPrivate
import com.platform.app.presentation.theme.Dimens
import com.platform.app.presentation.theme.PlatformIconCatalog
import com.platform.app.presentation.theme.SuccessGreen
import com.platform.app.presentation.theme.WarningAmber

@Composable
fun PayableItemCard(
    item: PayableItem,
    isMuted: Boolean = false,
    isPrivate: Boolean = false,
    onPay: (() -> Unit)? = null
) {
    val alphaModifier = if (isMuted) Modifier.alpha(0.75f) else Modifier
    val functionalIcon = PlatformIconCatalog.getIcon(item.categoryIconName)
    val categoryColor = try {
        Color(item.categoryColorHex.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    PlatformCard(
        modifier = alphaModifier,
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingNormal),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = functionalIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isPaid) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SuccessGreen.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = AppStrings.Status.PAID,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        } else {
                            val isToday = item.urgency == PayableUrgency.DUE_TODAY || DateUtils.isToday(item.dueDate)
                            val isOverdue = item.urgency == PayableUrgency.OVERDUE || (item.dueDate < System.currentTimeMillis() && !isToday)
                            Text(
                                text = when {
                                    isOverdue -> "Venceu ${DateUtils.formatDate(item.dueDate)}"
                                    isToday -> "Vence hoje"
                                    else -> "Vence ${DateUtils.formatDate(item.dueDate)}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isOverdue || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                color = when {
                                    isOverdue -> MaterialTheme.colorScheme.error
                                    isToday -> WarningAmber
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = item.nature.let {
                                try { Color(it.colorHex.toColorInt()).copy(alpha = 0.15f) }
                                catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }
                            }
                        ) {
                            Text(
                                text = item.nature.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = try { Color(item.nature.colorHex.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatValueOrPrivate(item.amountCents, isPrivate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPaid) SuccessGreen else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (onPay != null && !item.isPaid) {
                    Button(
                        onClick = onPay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pagar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (item.isPaid) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = AppStrings.Status.PAID,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyForecastCard(onAddExpense: () -> Unit) {
    PlatformCard(
        shape = RoundedCornerShape(Dimens.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nenhum Compromisso Imediato",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cadastre suas contas a pagar para planejar seus pagamentos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddExpense,
                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nova Despesa",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun UrgencySectionHeader(
    title: String,
    count: Int,
    badgeColor: Color,
    totalCents: Long,
    isPrivate: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spacingSmall, bottom = Dimens.spacingExtraSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$title ($count)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = formatValueOrPrivate(totalCents, isPrivate),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getFunctionalIcon(categoryName: String, iconName: String? = null): ImageVector {
    if (!iconName.isNullOrBlank() && iconName != "category") {
        return PlatformIconCatalog.getIcon(iconName)
    }
    val lower = categoryName.lowercase()
    return when {
        lower.contains("restaurante") || lower.contains("alimenta") || lower.contains("comida") || lower.contains("mercado") -> Icons.Default.Restaurant
        lower.contains("moradia") || lower.contains("aluguel") || lower.contains("casa") || lower.contains("condom") -> Icons.Default.Home
        lower.contains("transporte") || lower.contains("carro") || lower.contains("combust") || lower.contains("uber") -> Icons.Default.DirectionsCar
        lower.contains("cart") || lower.contains("fatura") || lower.contains("crédito") -> Icons.Default.CreditCard
        lower.contains("saúde") || lower.contains("médic") || lower.contains("remédio") || lower.contains("farm") -> Icons.Default.MedicalServices
        lower.contains("educa") || lower.contains("curso") || lower.contains("faculdade") -> Icons.Default.School
        lower.contains("compra") || lower.contains("shopping") -> Icons.Default.ShoppingCart
        else -> PlatformIconCatalog.getIcon(iconName ?: "")
    }
}
