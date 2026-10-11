package com.platform.app.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformPrivacyToggle
import com.platform.app.presentation.components.PlatformSurface
import com.platform.app.presentation.home.HomeViewMode
import com.platform.app.presentation.theme.PlatformShapes

/**
 * TopBar minimalista da tela Hoje (Prompt 59):
 * 1. Botão do menu lateral + Título "Hoje" + Mês navegável.
 * 2. Alternância Lista | Calendário na barra de ações.
 * 3. Alternância de privacidade.
 */
@Composable
fun HomeTopBar(
    viewMode: HomeViewMode,
    monthLabel: String,
    isPrivate: Boolean,
    onTogglePrivacy: () -> Unit,
    onOpenMonthPicker: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onToggleViewMode: (HomeViewMode) -> Unit,
    onOpenDrawer: (() -> Unit)? = null
) {
    PlatformSurface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado Esquerdo: Menu + Título "Hoje" + Mês Navegável
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Lateral",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = AppStrings.Navigation.TODAY,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(onClick = onCurrentMonth)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Cápsula navegável do mês (< Mês >)
                PlatformSurface(
                    shape = PlatformShapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Mês Anterior",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = monthLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable(onClick = onOpenMonthPicker)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Próximo Mês",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Lado Direito (Barra de Ações): Privacidade + Alternância Lista | Calendário
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PlatformPrivacyToggle(
                    isPrivate = isPrivate,
                    onToggle = onTogglePrivacy
                )

                // Alternância Lista | Calendário na barra de ações
                PlatformSurface(
                    shape = PlatformShapes.pill,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        val isList = viewMode == HomeViewMode.LIST
                        val isCalendar = viewMode == HomeViewMode.CALENDAR

                        // Botão Lista
                        PlatformSurface(
                            shape = PlatformShapes.pill,
                            color = if (isList) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            onClick = { onToggleViewMode(HomeViewMode.LIST) },
                            modifier = Modifier.size(width = 30.dp, height = 26.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(width = 30.dp, height = 26.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = AppStrings.Home.VIEW_LIST,
                                    tint = if (isList) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Botão Calendário
                        PlatformSurface(
                            shape = PlatformShapes.pill,
                            color = if (isCalendar) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            onClick = { onToggleViewMode(HomeViewMode.CALENDAR) },
                            modifier = Modifier.size(width = 30.dp, height = 26.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(width = 30.dp, height = 26.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = AppStrings.Home.VIEW_CALENDAR,
                                    tint = if (isCalendar) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
