package com.platform.app.presentation.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.theme.PlatformShapes
import com.platform.app.presentation.theme.PlatformStatusStyle
import com.platform.app.presentation.theme.PlatformStatusType
import com.platform.app.presentation.theme.PlatformTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DrawerItemData(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val badgeContent: (@Composable () -> Unit)? = null
)

data class DrawerGroup(
    val title: String,
    val items: List<DrawerItemData>
)

val DrawerWidth = 310.dp
val DrawerShape = PlatformShapes.drawer

private fun isScreenSelected(currentRoute: String?, screen: Screen): Boolean {
    if (currentRoute == null) return false
    return when (screen) {
        is Screen.Overview -> currentRoute == Screen.Overview.route
        is Screen.Dashboard -> currentRoute == Screen.Dashboard.route
        is Screen.Bills -> currentRoute == Screen.Bills.route
        is Screen.CreditCards -> currentRoute == Screen.CreditCards.route
        is Screen.Statistics -> currentRoute == Screen.Statistics.route || currentRoute == Screen.Budgets.route || currentRoute == Screen.Goals.route
        is Screen.Contacts -> currentRoute == Screen.Contacts.route || currentRoute.startsWith("contact_detail")
        is Screen.Management -> currentRoute.startsWith("management_screen")
        is Screen.Settings -> currentRoute == Screen.Settings.route
        else -> currentRoute == screen.route
    }
}

@Composable
fun DrawerContent(
    currentRoute: String?,
    monthRemainingCents: Long,
    overdueCount: Int,
    lastBackupTimestamp: Long,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = remember(overdueCount) {
        listOf(
            DrawerGroup(
                title = AppStrings.Drawer.SECTION_OPERATIONAL,
                items = listOf(
                    DrawerItemData(
                        screen = Screen.Overview,
                        title = AppStrings.Drawer.OVERVIEW,
                        icon = Icons.Default.Dashboard
                    ),
                    DrawerItemData(
                        screen = Screen.Dashboard,
                        title = AppStrings.Drawer.TODAY,
                        icon = Icons.Default.Home
                    ),
                    DrawerItemData(
                        screen = Screen.Bills,
                        title = AppStrings.Drawer.BILLS,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        badgeContent = if (overdueCount > 0) {
                            {
                                val overdueColors = PlatformStatusStyle.getColors(PlatformStatusType.OVERDUE)
                                Badge(
                                    containerColor = overdueColors.containerColor,
                                    contentColor = overdueColors.contentColor
                                ) {
                                    Text(
                                        text = overdueCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else null
                    ),
                    DrawerItemData(
                        screen = Screen.CreditCards,
                        title = AppStrings.Drawer.CREDIT_CARDS,
                        icon = Icons.Default.CreditCard
                    )
                )
            ),
            DrawerGroup(
                title = AppStrings.Drawer.SECTION_PLANNING,
                items = listOf(
                    DrawerItemData(
                        screen = Screen.Statistics,
                        title = AppStrings.Drawer.ANALYTICS,
                        icon = Icons.Default.BarChart
                    )
                )
            ),
            DrawerGroup(
                title = AppStrings.Drawer.SECTION_MANAGEMENT,
                items = listOf(
                    DrawerItemData(
                        screen = Screen.Contacts,
                        title = AppStrings.Drawer.CONTACTS,
                        icon = Icons.Default.People
                    ),
                    DrawerItemData(
                        screen = Screen.Management,
                        title = AppStrings.Drawer.REGISTRATIONS,
                        icon = Icons.Default.Category
                    )
                )
            ),
            DrawerGroup(
                title = AppStrings.Drawer.SECTION_SYSTEM,
                items = listOf(
                    DrawerItemData(
                        screen = Screen.Settings,
                        title = AppStrings.Drawer.SETTINGS,
                        icon = Icons.Default.Settings
                    )
                )
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        // Cabeçalho: "Platform" e "Mês · restam R$ X"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text(
                text = AppStrings.Drawer.APP_NAME,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            val subtitle = if (monthRemainingCents > 0L) {
                "${AppStrings.Drawer.MONTH_REMAINING_PREFIX}${CurrencyUtils.formatCentsToCurrency(monthRemainingCents)}"
            } else {
                AppStrings.Drawer.MONTH_ALL_SETTLED
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = PlatformTokens.BorderWidth,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        )

        // Lista de grupos com rolagem vertical
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            groups.forEachIndexed { groupIndex, group ->
                if (groupIndex > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                PlatformSectionTitle(
                    text = group.title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )

                group.items.forEach { item ->
                    val isSelected = isScreenSelected(currentRoute, item.screen)

                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        badge = item.badgeContent,
                        selected = isSelected,
                        onClick = {
                            onCloseDrawer()
                            if (!isSelected) {
                                onNavigate(item.screen)
                            }
                        },
                        shape = PlatformShapes.medium,
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedContainerColor = Color.Transparent,
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = PlatformTokens.BorderWidth,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        )

        // Rodapé: "Último backup: data e hora"
        val backupText = if (lastBackupTimestamp > 0L) {
            val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
            "${AppStrings.Drawer.LAST_BACKUP_PREFIX}${formatter.format(Date(lastBackupTimestamp))}"
        } else {
            AppStrings.Drawer.LAST_BACKUP_NEVER
        }
        Text(
            text = backupText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AppModalDrawerSheet(
    currentRoute: String?,
    monthRemainingCents: Long,
    overdueCount: Int,
    lastBackupTimestamp: Long,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(DrawerWidth)
            .border(PlatformTokens.BorderWidth, MaterialTheme.colorScheme.outlineVariant, DrawerShape),
        drawerShape = DrawerShape,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp
    ) {
        DrawerContent(
            currentRoute = currentRoute,
            monthRemainingCents = monthRemainingCents,
            overdueCount = overdueCount,
            lastBackupTimestamp = lastBackupTimestamp,
            onNavigate = onNavigate,
            onCloseDrawer = onCloseDrawer
        )
    }
}

@Composable
fun AppPermanentDrawerSheet(
    currentRoute: String?,
    monthRemainingCents: Long,
    overdueCount: Int,
    lastBackupTimestamp: Long,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier
            .width(DrawerWidth)
            .border(PlatformTokens.BorderWidth, MaterialTheme.colorScheme.outlineVariant, DrawerShape),
        drawerShape = DrawerShape,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp
    ) {
        DrawerContent(
            currentRoute = currentRoute,
            monthRemainingCents = monthRemainingCents,
            overdueCount = overdueCount,
            lastBackupTimestamp = lastBackupTimestamp,
            onNavigate = onNavigate,
            onCloseDrawer = {}
        )
    }
}
