package com.platform.app.domain.usecase

import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.domain.model.DrawerOverview
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetDrawerOverviewUseCase @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val calculateMonthlyForecastUseCase: CalculateMonthlyForecastUseCase,
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(nowMillis: Long = System.currentTimeMillis()): Flow<DrawerOverview> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        return combine(
            calculateMonthlyForecastUseCase(nowMillis, nowMillis),
            financialRepository.getAllInstallments(),
            financialRepository.getAllCreditCardInvoices(),
            preferencesManager.lastOfflineBackupTimestamp,
            preferencesManager.initialScreen
        ) { forecast, installments, invoices, lastBackup, initialScreen ->
            val overdueBillsCount = installments.count { !it.isPaid && it.dueDateMillis < startOfToday && it.invoiceId == null }
            val overdueInvoicesCount = invoices.count {
                it.status != InvoiceStatus.PAGA && it.dueDateMillis < startOfToday
            }
            val totalOverdueCount = overdueBillsCount + overdueInvoicesCount

            DrawerOverview(
                remainingMonthCents = forecast.totalForecastCents,
                overdueCount = totalOverdueCount,
                lastBackupTimestamp = lastBackup,
                initialScreen = initialScreen
            )
        }
    }
}
