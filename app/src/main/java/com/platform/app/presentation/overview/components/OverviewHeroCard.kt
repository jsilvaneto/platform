package com.platform.app.presentation.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.PortfolioOverview
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformSectionTitle
import com.platform.app.presentation.theme.PlatformShapes

/**
 * Hero Card plano da Visão Geral (Prompt 58):
 * - Superfície plana sup1 (surface) com borda de 1dp na cor de linha (outline), sem gradiente ou sombra.
 * - Valor 36sp em algarismos tabulares com centavos menores em texto2.
 * - Subtítulo com contagem de parcelas e mês do último vencimento.
 * - Estado positivo "Nada em aberto" quando não houver dívidas parceladas.
 */
@Composable
fun OverviewHeroCard(
    overview: PortfolioOverview,
    modifier: Modifier = Modifier
) {
    PlatformCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            PlatformSectionTitle(text = AppStrings.Overview.OPEN_INSTALLMENTS)
            Spacer(modifier = Modifier.height(10.dp))

            if (overview.hasDebts) {
                PlatformMoneyText(
                    amountCents = overview.openInstallmentsAmountCents,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                val lastDueLabel = overview.lastInstallmentDueDate?.let {
                    " · último em ${DateUtils.formatDate(it)}"
                } ?: ""

                Text(
                    text = "${overview.openInstallmentsCount} parcelas$lastDueLabel",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(PlatformShapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = AppStrings.Overview.NOTHING_OPEN,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = AppStrings.Overview.NO_OPEN_INSTALLMENTS_DESC,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
