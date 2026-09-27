package com.platform.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CategorySpend
import com.platform.app.presentation.components.PlatformAppBar
import com.platform.app.presentation.home.OfflineBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            PlatformAppBar(
                title = "Dashboard",
                actions = {
                    OfflineBadge()
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Seletor de Mês
            MonthSelector(
                selectedMonthMillis = uiState.selectedMonthMillis,
                onPreviousMonth = { onAction(DashboardUiAction.PreviousMonth) },
                onNextMonth = { onAction(DashboardUiAction.NextMonth) },
                onCurrentMonth = { onAction(DashboardUiAction.CurrentMonth) }
            )

            if (uiState.isLoading && uiState.metrics == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val metrics = uiState.metrics
                if (metrics == null || (metrics.totalDueMonthCents == 0L && metrics.upcomingInstallments.isEmpty())) {
                    EmptyDashboardState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Grade de KPIs
                        item {
                            KpiGrid(
                                totalDue = metrics.totalDueMonthCents,
                                totalPending = metrics.totalPendingMonthCents,
                                totalPaid = metrics.totalPaidMonthCents,
                                totalOverdue = metrics.totalOverdueMonthCents
                            )
                        }

                        // Próximos Vencimentos
                        if (metrics.upcomingInstallments.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Próximos Vencimentos",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(metrics.upcomingInstallments, key = { it.id }) { installment ->
                                UpcomingInstallmentCard(installment)
                            }
                        }

                        // Distribuição por Categoria
                        if (metrics.categoryDistribution.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Despesas por Categoria",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(metrics.categoryDistribution, key = { it.categoryName }) { catSpend ->
                                CategorySpendRow(catSpend)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    selectedMonthMillis: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Text("◀", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DateUtils.formatMonthYear(selectedMonthMillis),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onCurrentMonth, modifier = Modifier.height(28.dp)) {
                    Text("Ir para o mês atual", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(onClick = onNextMonth) {
                Text("▶", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun KpiGrid(
    totalDue: Long,
    totalPending: Long,
    totalPaid: Long,
    totalOverdue: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total do Mês",
                amountCents = totalDue,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                textColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "A Pagar (Pendente)",
                amountCents = totalPending,
                containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f),
                textColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Já Pago",
                amountCents = totalPaid,
                containerColor = Color(0xFF10B981).copy(alpha = 0.12f),
                textColor = Color(0xFF059669),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Vencido / Atrasado",
                amountCents = totalOverdue,
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                textColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    amountCents: Long,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyUtils.formatCentsToCurrency(amountCents),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun UpcomingInstallmentCard(installment: BillInstallment) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = try {
                Color(android.graphics.Color.parseColor(installment.categoryColorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = installment.billTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${installment.categoryName} • Vence em ${DateUtils.formatDate(installment.dueDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategorySpendRow(catSpend: CategorySpend) {
    val barColor = try {
        Color(android.graphics.Color.parseColor(catSpend.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = catSpend.categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${CurrencyUtils.formatCentsToCurrency(catSpend.amountCents)} (${String.format("%.1f", catSpend.percentage)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (catSpend.percentage / 100f).coerceIn(0f, 1f) },
                color = barColor,
                trackColor = barColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}

@Composable
fun EmptyDashboardState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📊", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhuma conta cadastrada para este mês",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Acesse a aba 'Contas' para cadastrar despesas avulsas, parceladas ou recorrentes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
