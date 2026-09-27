package com.platform.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CategorySpend
import com.platform.app.domain.model.FinancialDashboardMetrics
import com.platform.app.domain.model.FutureMonthProjection
import com.platform.app.presentation.home.OfflineBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Visão Financeira & Planejamento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menu")
                    }
                },
                actions = {
                    OfflineBadge()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            // Seletor de Mês Estilizado
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
                if (metrics == null || (metrics.totalDueMonthCents == 0L && metrics.upcomingInstallments.isEmpty() && metrics.futureMonthsProjections.all { it.totalCommittedCents == 0L })) {
                    EmptyDashboardState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Card de Destaque com Resumo e Progresso
                        item {
                            MonthOverviewHeroCard(metrics = metrics)
                        }

                        // Visão de Planejamento Futuro (6 Meses)
                        item {
                            FuturePlanningSection(metrics = metrics)
                        }

                        // Contas a Vencer nos Próximos 7 Dias (Ação Rápida)
                        if (metrics.upcomingWeekInstallments.isNotEmpty()) {
                            item {
                                UpcomingWeekSection(
                                    installments = metrics.upcomingWeekInstallments,
                                    onTogglePayment = { inst ->
                                        onAction(
                                            DashboardUiAction.TogglePayment(
                                                installmentId = inst.id,
                                                currentPaid = inst.isPaid
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        // Próximos Vencimentos Gerais do Mês
                        if (metrics.upcomingInstallments.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Vencimentos do Mês",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
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
                                    text = "Distribuição por Categorias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            items(metrics.categoryDistribution, key = { it.categoryName }) { catSpend ->
                                CategorySpendRow(catSpend)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthOverviewHeroCard(metrics: FinancialDashboardMetrics) {
    val paidProgress = if (metrics.totalDueMonthCents > 0L) {
        (metrics.totalPaidMonthCents.toFloat() / metrics.totalDueMonthCents.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val percentPaid = (paidProgress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total a Pagar no Mês",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(metrics.totalDueMonthCents),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$percentPaid% Quitado",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Barra de progresso elegante
                LinearProgressIndicator(
                    progress = { paidProgress },
                    color = Color(0xFF10B981),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                // Sub-Cards: Já Pago, Pendente e Atrasado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniKpiPill(
                        label = "Já Pago",
                        valueCents = metrics.totalPaidMonthCents,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MiniKpiPill(
                        label = "Pendente",
                        valueCents = metrics.totalPendingMonthCents,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    if (metrics.totalOverdueMonthCents > 0L) {
                        MiniKpiPill(
                            label = "Atrasado",
                            valueCents = metrics.totalOverdueMonthCents,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniKpiPill(
    label: String,
    valueCents: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = CurrencyUtils.formatCentsToCurrency(valueCents),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FuturePlanningSection(metrics: FinancialDashboardMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Planejamento dos Próximos Meses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Comprometimento de parcelas e custos futuros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Projeções futuras em carrossel horizontal
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(metrics.futureMonthsProjections, key = { it.monthMillis }) { proj ->
                    FutureMonthCard(projection = proj)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resumo do total comprometido futuro
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total comprometido para o futuro:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyUtils.formatCentsToCurrency(metrics.totalCommittedFutureCents),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FutureMonthCard(projection: FutureMonthProjection) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.width(150.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = projection.monthLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyUtils.formatCentsToCurrency(projection.totalCommittedCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (projection.totalCommittedCents > 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${projection.installmentsCount} pagamentos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun UpcomingWeekSection(
    installments: List<BillInstallment>,
    onTogglePayment: (BillInstallment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Vencimentos desta Semana",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                    Text(
                        text = "${installments.size} contas vencem nos próximos 7 dias",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD97706).copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            installments.forEach { inst ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onTogglePayment(inst) }) {
                            Icon(
                                imageVector = if (inst.isPaid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Pagar",
                                tint = if (inst.isPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inst.billTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vence em ${DateUtils.formatDate(inst.dueDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = CurrencyUtils.formatCentsToCurrency(inst.amountCents),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
    onCurrentMonth: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = DateUtils.formatMonthYear(selectedMonthMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onCurrentMonth, modifier = Modifier.height(26.dp)) {
                    Text("Voltar ao Mês Atual", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(onClick = onNextMonth) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Próximo mês")
            }
        }
    }
}

@Composable
fun UpcomingInstallmentCard(installment: BillInstallment) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            Text(
                text = CurrencyUtils.formatCentsToCurrency(installment.amountCents),
                style = MaterialTheme.typography.titleMedium,
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
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
                    style = MaterialTheme.typography.bodyMedium,
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
            text = "Abra o menu lateral ou cadastre uma nova despesa para iniciar seu planejamento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
