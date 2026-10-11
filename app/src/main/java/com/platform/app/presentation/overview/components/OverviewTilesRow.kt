package com.platform.app.presentation.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.domain.model.PortfolioOverview
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformSectionTitle

/**
 * Linha de tiles operacionais da Visão Geral (Prompt 58):
 * 1. Custo fixo/mês (recorrentes ativas).
 * 2. Cartões em uso (% do limite consumido).
 * 3. Reserva (% atingida das metas de reserva).
 */
@Composable
fun OverviewTilesRow(
    overview: PortfolioOverview,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tile 1: Custo fixo / mês
        PlatformCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                PlatformSectionTitle(text = AppStrings.Overview.FIXED_COST_MONTH)
                Spacer(modifier = Modifier.height(6.dp))
                PlatformMoneyText(
                    amountCents = overview.fixedCostMonthlyCents,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Tile 2: Cartões em uso
        PlatformCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                PlatformSectionTitle(text = AppStrings.Overview.CARDS_IN_USE)
                Spacer(modifier = Modifier.height(6.dp))
                val cardsLabel = overview.cardsUsagePercentage?.let { "$it%" } ?: AppStrings.Overview.NO_DATA_DASH
                Text(
                    text = cardsLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Tile 3: Reserva
        PlatformCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                PlatformSectionTitle(text = AppStrings.Overview.RESERVE)
                Spacer(modifier = Modifier.height(6.dp))
                val goalsLabel = overview.goalsReservePercentage?.let { "$it%" } ?: AppStrings.Overview.NO_DATA_DASH
                Text(
                    text = goalsLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
