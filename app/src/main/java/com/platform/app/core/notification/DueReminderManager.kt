package com.platform.app.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.platform.app.MainActivity
import com.platform.app.R
import com.platform.app.core.util.CurrencyUtils
import com.platform.app.core.util.DateUtils
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

object DueReminderManager {

    const val CHANNEL_ID = "due_expenses_reminders"
    const val CHANNEL_NAME = "Vencimentos e Faturas"
    const val NOTIFICATION_ID = 1001

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

    fun scheduleDailyReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DueReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {
            // Caso AlarmManager tenha restrições específicas do dispositivo
        }
    }

    suspend fun checkAndNotifyDueExpenses(context: Context, repository: FinancialRepository) {
        createNotificationChannel(context)

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis

        val installments = repository.getAllInstallments().first()
        val invoices = repository.getAllCreditCardInvoices().first()

        // Contas avulsas pendentes vencendo hoje
        val dueBillsToday = installments.filter { inst ->
            inst.invoiceId == null && !inst.isPaid && !inst.isPaused && inst.dueDate in startOfToday..endOfToday
        }

        // Faturas pendentes vencendo hoje
        val dueInvoicesToday = invoices.filter { inv ->
            inv.status != InvoiceStatus.PAGA && inv.dueDate in startOfToday..endOfToday
        }

        val totalItems = dueBillsToday.size + dueInvoicesToday.size
        if (totalItems <= 0) return

        val totalAmountCents = dueBillsToday.sumOf { it.amountCents } + dueInvoicesToday.sumOf { it.totalAmountCents }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (totalItems == 1) {
            "Você tem 1 pagamento vencendo hoje"
        } else {
            "Você tem $totalItems pagamentos vencendo hoje"
        }

        val content = "Total de ${CurrencyUtils.formatCentsToCurrency(totalAmountCents)} para liquidar hoje."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content Toque para visualizar na Platform e liquidar com 1 toque."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } catch (_: SecurityException) {
            // Permissão de notificação não concedida pelo usuário
        }
    }
}
