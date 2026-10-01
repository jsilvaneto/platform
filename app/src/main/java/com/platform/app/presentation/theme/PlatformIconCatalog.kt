package com.platform.app.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class CategoryIconItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
    val categoryGroup: String
)

object PlatformIconCatalog {
    val ICONS: List<CategoryIconItem> = listOf(
        // Casa & Moradia
        CategoryIconItem("home", Icons.Default.Home, "Casa", "Moradia"),
        CategoryIconItem("apartment", Icons.Default.Apartment, "Apartamento", "Moradia"),
        CategoryIconItem("lightbulb", Icons.Default.Lightbulb, "Energia/Luz", "Moradia"),
        CategoryIconItem("water_drop", Icons.Default.WaterDrop, "Água", "Moradia"),
        CategoryIconItem("wifi", Icons.Default.Wifi, "Internet", "Moradia"),
        CategoryIconItem("build", Icons.Default.Build, "Manutenção", "Moradia"),

        // Alimentação
        CategoryIconItem("shopping_cart", Icons.Default.ShoppingCart, "Mercado", "Alimentação"),
        CategoryIconItem("restaurant", Icons.Default.Restaurant, "Restaurante", "Alimentação"),
        CategoryIconItem("local_cafe", Icons.Default.LocalCafe, "Café", "Alimentação"),
        CategoryIconItem("fastfood", Icons.Default.Fastfood, "Lanche", "Alimentação"),
        CategoryIconItem("local_bar", Icons.Default.LocalBar, "Bar / Bebidas", "Alimentação"),

        // Transporte
        CategoryIconItem("directions_car", Icons.Default.DirectionsCar, "Carro", "Transporte"),
        CategoryIconItem("local_gas_station", Icons.Default.LocalGasStation, "Combustível", "Transporte"),
        CategoryIconItem("local_taxi", Icons.Default.LocalTaxi, "App / Táxi", "Transporte"),
        CategoryIconItem("directions_bus", Icons.Default.DirectionsBus, "Ônibus", "Transporte"),
        CategoryIconItem("flight", Icons.Default.Flight, "Viagem", "Transporte"),

        // Estilo de Vida & Lazer
        CategoryIconItem("sports_esports", Icons.Default.SportsEsports, "Games", "Lazer"),
        CategoryIconItem("movie", Icons.Default.Movie, "Cinema", "Lazer"),
        CategoryIconItem("subscriptions", Icons.Default.Subscriptions, "Streaming", "Lazer"),
        CategoryIconItem("fitness_center", Icons.Default.FitnessCenter, "Academia", "Lazer"),
        CategoryIconItem("celebration", Icons.Default.Celebration, "Festas", "Lazer"),
        CategoryIconItem("beach_access", Icons.Default.BeachAccess, "Férias", "Lazer"),

        // Saúde & Cuidados
        CategoryIconItem("medical_services", Icons.Default.MedicalServices, "Saúde", "Saúde"),
        CategoryIconItem("local_pharmacy", Icons.Default.LocalPharmacy, "Farmácia", "Saúde"),
        CategoryIconItem("spa", Icons.Default.Spa, "Bem-Estar", "Saúde"),
        CategoryIconItem("favorite", Icons.Default.Favorite, "Seguros", "Saúde"),

        // Finanças & Educação
        CategoryIconItem("account_balance", Icons.Default.AccountBalance, "Banco", "Finanças"),
        CategoryIconItem("savings", Icons.Default.Savings, "Poupança", "Finanças"),
        CategoryIconItem("credit_card", Icons.Default.CreditCard, "Cartão", "Finanças"),
        CategoryIconItem("payments", Icons.Default.Payments, "Dinheiro", "Finanças"),
        CategoryIconItem("receipt", Icons.AutoMirrored.Filled.ReceiptLong, "Boleto", "Finanças"),
        CategoryIconItem("work", Icons.Default.Work, "Trabalho", "Finanças"),
        CategoryIconItem("school", Icons.Default.School, "Educação", "Educação"),

        // Diversos
        CategoryIconItem("pets", Icons.Default.Pets, "Pet", "Outros"),
        CategoryIconItem("shopping_bag", Icons.Default.ShoppingBag, "Compras", "Outros"),
        CategoryIconItem("card_giftcard", Icons.Default.CardGiftcard, "Presentes", "Outros"),
        CategoryIconItem("category", Icons.Default.Category, "Geral", "Outros")
    )

    fun getIcon(key: String): ImageVector {
        return ICONS.find { it.key.equals(key, ignoreCase = true) }?.icon ?: Icons.Default.Category
    }
}

/**
 * Seletor de ícones em grade com identificação visual instantânea.
 */
@Composable
fun PlatformIconPicker(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit,
    activeColorHex: String = "#2563EB",
    modifier: Modifier = Modifier
) {
    val activeColor = try {
        Color(android.graphics.Color.parseColor(activeColorHex))
    } catch (e: Exception) {
        BrandPrimary
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ícone Representativo",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val selectedItem = PlatformIconCatalog.ICONS.find { it.key == selectedIconKey }
            Text(
                text = selectedItem?.label ?: "Padrão",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PlatformIconCatalog.ICONS, key = { it.key }) { item ->
                val isSelected = selectedIconKey.equals(item.key, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = if (isSelected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) activeColor else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onIconSelected(item.key) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
