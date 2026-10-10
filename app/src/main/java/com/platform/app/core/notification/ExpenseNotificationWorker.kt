package com.platform.app.core.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class ExpenseNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationWorkerEntryPoint {
        fun financialRepository(): FinancialRepository
        fun preferencesManager(): PreferencesManager
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationWorkerEntryPoint::class.java
        )
        val repository = entryPoint.financialRepository()
        val preferences = entryPoint.preferencesManager()

        try {
            repository.materializeRecurringCardInvoices()
            val notifyTomorrow = preferences.notifyDueTomorrow.first()
            val notifyOverdue = preferences.notifyOverdue.first()

            DueReminderManager.checkAndNotifyDueExpenses(
                context = applicationContext,
                repository = repository,
                notifyTomorrow = notifyTomorrow,
                notifyOverdue = notifyOverdue
            )
        } catch (_: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}
