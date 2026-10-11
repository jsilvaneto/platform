package com.platform.app.presentation.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.components.PlatformNeutralChip
import com.platform.app.presentation.overview.components.Overview12MonthChart
import com.platform.app.presentation.overview.components.OverviewAttentionBanner
import com.platform.app.presentation.overview.components.OverviewFreeingUpList
import com.platform.app.presentation.overview.components.OverviewHeroCard
import com.platform.app.presentation.overview.components.OverviewTilesRow

/**
 * Tela principal de Visão Geral (Prompt 58):
 * - Sem seletor de mês; chip informativo "Todos os períodos" no topo.
 * - Banner de atenção condicional (contas atrasadas + vencendo hoje).
 * - Hero plano "Parcelado em aberto" (sup1, 36sp, sem sombras/gradientes).
 * - Tiles operacionais: custo fixo/mês, cartões em uso e reserva.
 * - Projeção temporal em barras de 12 meses (2 passados, atual em negrito, 9 futuros com pico e folga).
 * - Lista "Quando fica livre" com projeção de desoneração do orçamento.
 */
@Composable
fun OverviewScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToToday: () -> Unit = {},
    viewModel: OverviewViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    OverviewContent(
        overview = uiState.overview,
        onOpenDrawer = onOpenDrawer,
        onNavigateToToday = onNavigateToToday,
        modifier = modifier
    )
}

@Composable
fun OverviewContent(
    overview: com.platform.app.domain.model.PortfolioOverview,
    onOpenDrawer: () -> Unit,
    onNavigateToToday: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            PlatformAppBar(
                title = AppStrings.Drawer.OVERVIEW,
                onOpenDrawer = onOpenDrawer
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Topo: Chip fixo "Todos os períodos"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                PlatformNeutralChip(
                    text = AppStrings.Overview.ALL_PERIODS,
                    selected = true
                )
            }

            // Banner de atenção (some se 0)
            if (overview.hasAttention) {
                OverviewAttentionBanner(
                    attentionCount = overview.attentionCount,
                    onNavigateToToday = onNavigateToToday
                )
            }

            // Hero plano "Parcelado em aberto"
            OverviewHeroCard(overview = overview)

            // Linha de tiles operacionais
            OverviewTilesRow(overview = overview)

            // Série temporal de 12 meses
            if (overview.monthlySeries.isNotEmpty()) {
                Overview12MonthChart(monthlySeries = overview.monthlySeries)
            }

            // Lista "Quando fica livre"
            if (overview.freeingUpCommitments.isNotEmpty()) {
                OverviewFreeingUpList(commitments = overview.freeingUpCommitments)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
