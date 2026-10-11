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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.presentation.common.AppStrings
import com.platform.app.presentation.components.PlatformCard
import com.platform.app.presentation.components.PlatformMoneyText
import com.platform.app.presentation.components.PlatformSectionTitle

/**
 * Seção "Quando fica livre" da Visão Geral (Prompt 58):
 * Lista até 5 compromissos por ordem cronológica de término,
 * indicando a data de encerramento e a folga mensal liberada.
 */
@Composable
fun OverviewFreeingUpList(
    commitments: List<FreeingUpCommitment>,
    modifier: Modifier = Modifier
) {
    if (commitments.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PlatformSectionTitle(text = AppStrings.Overview.WHEN_FREE_TITLE)

        commitments.forEach { item ->
            PlatformCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Termina em ${DateUtils.formatDate(item.endDate)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        PlatformMoneyText(
                            amountCents = item.monthlyAmountCents,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${AppStrings.Overview.LIBERATES_PREFIX}${item.monthlyAmountCents / 100}${AppStrings.Overview.LIBERATES_SUFFIX}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
