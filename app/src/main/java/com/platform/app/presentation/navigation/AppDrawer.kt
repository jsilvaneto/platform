package com.platform.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DrawerItem(
    val screen: Screen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        DrawerItem(
            screen = Screen.Dashboard,
            title = "Início",
            subtitle = "Visão geral e planejamento",
            icon = Icons.Default.Home
        ),
        DrawerItem(
            screen = Screen.Bills,
            title = "Registros",
            subtitle = "Lançamentos e vencimentos",
            icon = Icons.Default.ReceiptLong
        ),
        DrawerItem(
            screen = Screen.RecurringInstallments,
            title = "Recorrentes e Parcelados",
            subtitle = "Assinaturas e parcelamentos",
            icon = Icons.Default.Repeat
        ),
        DrawerItem(
            screen = Screen.Goals,
            title = "Metas",
            subtitle = "Objetivos e reservas financeiras",
            icon = Icons.Default.Flag
        ),
        DrawerItem(
            screen = Screen.Budgets,
            title = "Orçamentos",
            subtitle = "Tetos de gastos por categoria",
            icon = Icons.Default.PieChart
        ),
        DrawerItem(
            screen = Screen.Contacts,
            title = "Contatos",
            subtitle = "Favorecidos e beneficiários",
            icon = Icons.Default.People
        ),
        DrawerItem(
            screen = Screen.Settings,
            title = "Configurações",
            subtitle = "Cadastros, biometria e sobre",
            icon = Icons.Default.Settings
        )
    )

    ModalDrawerSheet(
        modifier = modifier.width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header do Drawer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "P",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Platform",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gestão Financeira Pessoal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Itens de Navegação
            items.forEach { item ->
                val selected = currentRoute == item.screen.route

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    },
                    selected = selected,
                    onClick = {
                        onCloseDrawer()
                        if (currentRoute != item.screen.route) {
                            onNavigate(item.screen)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Rodapé do Drawer
            Text(
                text = "Platform • Versão 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
