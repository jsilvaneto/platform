package com.platform.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.platform.app.MainActivity
import com.platform.app.R
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.domain.repository.FinancialRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DueReminderManager {

    const val CHANNEL_ID = "due_expenses_reminders"
    const val CHANNEL_NAME = "Vencimentos e Faturas"
    const val NOTIFICATION_ID = 1001
    const val WORK_NAME = "com.platform.app.daily_due_reminder_work"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes locais de contas e faturas com vencimento no dia"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun schedulePeriodicWork(context: Context, targetHour: Int = 9, targetMinute: Int = 0) {
        try {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val initialDelayMillis = target.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<ExpenseNotificationWorker>(
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } catch (_: Exception) {
            // Em caso de restrição do ambiente/dispositivo
        }
    }

    fun scheduleDailyReminder(context: Context) {
        schedulePeriodicWork(context)
    }

    suspend fun checkAndNotifyDueExpenses(
        context: Context,
        repository: FinancialRepository,
        notifyTomorrow: Boolean = true,
        notifyOverdue: Boolean = true,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ) {
        createNotificationChannel(context)

        val items = DueReminderCalculator.calculateItems(
            repository = repository,
            nowMillis = referenceTimeMillis,
            notifyTomorrow = notifyTomorrow,
            notifyOverdue = notifyOverdue
        )

        if (items.totalCount <= 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Versão pública (sem detalhes e sem valores confidenciais para tela de bloqueio)
        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("Lembrete de Vencimentos")
            .setContentText("Você tem ${items.totalCount} compromisso(s) pendente(s).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val title = when {
            items.overdueBills.isNotEmpty() || items.overdueInvoices.isNotEmpty() ->
                "Atenção: ${items.totalCount} pagamento(s) pendente(s)"
            items.todayBills.isNotEmpty() || items.todayInvoices.isNotEmpty() ->
                "Você tem compromissos vencendo hoje"
            else ->
                "Lembrete: pagamentos vencendo amanhã"
        }

        val totalFormatted = CurrencyUtils.formatCentsToCurrency(items.totalAmountCents)
        val content = "Total de $totalFormatted para conferir na Platform."

        val summaryLines = mutableListOf<String>()
        val totalOverdue = items.overdueBills.size + items.overdueInvoices.size
        val totalToday = items.todayBills.size + items.todayInvoices.size
        val totalTomorrow = items.tomorrowBills.size + items.tomorrowInvoices.size

        if (totalOverdue > 0) summaryLines.add("• $totalOverdue atrasado(s)")
        if (totalToday > 0) summaryLines.add("• $totalToday vence(m) hoje")
        if (totalTomorrow > 0) summaryLines.add("• $totalTomorrow vence(m) amanhã")

        val bigText = "$content\n" + summaryLines.joinToString("\n") + "\nToque para abrir e liquidar."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setAutoCancel(true)

        // Se houver exatamente 1 parcela de conta vencendo hoje ou atrasada, adiciona ações diretas
        if (items.totalCount == 1 && items.todayBills.size == 1) {
            val singleBill = items.todayBills.first()
            val payIntent = Intent(context, ExpenseNotificationActionReceiver::class.java).apply {
                action = ExpenseNotificationActionReceiver.ACTION_PAY_INSTALLMENT
                putExtra(ExpenseNotificationActionReceiver.EXTRA_INSTALLMENT_ID, singleBill.id)
                putExtra(ExpenseNotificationActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            }
            val payPending = PendingIntent.getBroadcast(
                context, 3001, payIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val postponeIntent = Intent(context, ExpenseNotificationActionReceiver::class.java).apply {
                action = ExpenseNotificationActionReceiver.ACTION_POSTPONE_INSTALLMENT
                putExtra(ExpenseNotificationActionReceiver.EXTRA_INSTALLMENT_ID, singleBill.id)
                putExtra(ExpenseNotificationActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            }
            val postponePending = PendingIntent.getBroadcast(
                context, 3002, postponeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(0, "Paguei", payPending)
            builder.addAction(0, "Adiar 1 dia", postponePending)
        } else if (items.totalCount == 1 && items.todayInvoices.size == 1) {
            val singleInvoice = items.todayInvoices.first()
            val payIntent = Intent(context, ExpenseNotificationActionReceiver::class.java).apply {
                action = ExpenseNotificationActionReceiver.ACTION_PAY_INVOICE
                putExtra(ExpenseNotificationActionReceiver.EXTRA_INVOICE_ID, singleInvoice.id)
                putExtra(ExpenseNotificationActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            }
            val payPending = PendingIntent.getBroadcast(
                context, 3003, payIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Pagar Fatura", payPending)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }
        } catch (_: SecurityException) {
            // Permissão negada no Android 13+
        }
    }
}
