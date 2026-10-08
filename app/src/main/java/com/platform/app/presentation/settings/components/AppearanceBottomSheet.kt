package com.platform.app.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.presentation.settings.SettingsUiState
import com.platform.app.presentation.theme.ThemePreviewColors

@Composable
fun AppearanceBottomSheetContent(
    uiState: SettingsUiState,
    onSetThemeMode: (Boolean?) -> Unit,
    onSetAmoledMode: (Boolean) -> Unit,
    onSetAppIcon: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Aparência",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Personalize o tema e os ícones do sistema",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tema do Aplicativo",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = uiState.isDarkMode == null,
                onClick = {
                    onSetThemeMode(null)
                    onSetAmoledMode(false)
                },
                label = { Text("Auto", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = uiState.isDarkMode == false,
                onClick = {
                    onSetThemeMode(false)
                    onSetAmoledMode(false)
                },
                label = { Text("Claro", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = uiState.isDarkMode == true && !uiState.isAmoledMode,
                onClick = {
                    onSetThemeMode(true)
                    onSetAmoledMode(false)
                },
                label = { Text("Navy", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = uiState.isDarkMode == true && uiState.isAmoledMode,
                onClick = {
                    onSetThemeMode(true)
                    onSetAmoledMode(true)
                },
                label = { Text("AMOLED", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1.1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ícone do Aplicativo",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Escolha o estilo visual para o ícone do sistema na tela inicial",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppIconPreviewCard(
                title = "Clássico",
                tag = "Original",
                isSelected = uiState.appIcon == "classic",
                onClick = { onSetAppIcon("classic") },
                modifier = Modifier.weight(1f)
            ) {
                ClassicIconPreview()
            }

            AppIconPreviewCard(
                title = "Modern V2",
                tag = "Padrão",
                isSelected = uiState.appIcon == "modern",
                onClick = { onSetAppIcon("modern") },
                modifier = Modifier.weight(1f)
            ) {
                ModernV2IconPreview()
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppIconPreviewCard(
                title = "Esmeralda",
                tag = "Wealth",
                isSelected = uiState.appIcon == "emerald",
                onClick = { onSetAppIcon("emerald") },
                modifier = Modifier.weight(1f)
            ) {
                EmeraldIconPreview()
            }

            AppIconPreviewCard(
                title = "Obsidian",
                tag = "Gold VIP",
                isSelected = uiState.appIcon == "obsidian",
                onClick = { onSetAppIcon("obsidian") },
                modifier = Modifier.weight(1f)
            ) {
                ObsidianIconPreview()
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun AppIconPreviewCard(
    title: String,
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = if (isSelected) "Em uso" else tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ClassicIconPreview() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(ThemePreviewColors.ClassicBackground, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp, 26.dp)
                .background(ThemePreviewColors.ClassicCard, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp, 10.dp)
                    .background(ThemePreviewColors.ClassicAccent, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
            )
            Box(
                modifier = Modifier
                    .size(10.dp, 8.dp)
                    .padding(start = 2.dp, top = 2.dp)
                    .background(ThemePreviewColors.ClassicBadge, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp)
                    .background(ThemePreviewColors.ClassicDot, CircleShape)
            )
        }
    }
}

@Composable
fun ModernV2IconPreview() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(ThemePreviewColors.ModernV2Background, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp, 28.dp)
                    .background(ThemePreviewColors.ModernV2Bar1, RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .size(14.dp, 14.dp)
                    .align(Alignment.Top)
                    .background(ThemePreviewColors.ModernV2Bar2, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .align(Alignment.Center)
                        .background(ThemePreviewColors.ModernV2Dot, CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(6.dp, 20.dp)
                    .background(ThemePreviewColors.ModernV2Bar3, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun EmeraldIconPreview() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(ThemePreviewColors.EmeraldBackground, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp, 26.dp)
                .background(ThemePreviewColors.EmeraldCard, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp, 10.dp)
                    .background(ThemePreviewColors.EmeraldAccent, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp)
                    .background(ThemePreviewColors.EmeraldDot, CircleShape)
            )
        }
    }
}

@Composable
fun ObsidianIconPreview() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(ThemePreviewColors.ObsidianBackground, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp, 26.dp)
                .background(ThemePreviewColors.ObsidianCard, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp, 10.dp)
                    .background(ThemePreviewColors.ObsidianAccent, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp)
                    .background(ThemePreviewColors.ObsidianDot, CircleShape)
            )
        }
    }
}
