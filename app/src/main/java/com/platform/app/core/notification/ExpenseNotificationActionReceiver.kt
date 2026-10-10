package com.platform.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ExpenseNotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: FinancialRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_PAY_INSTALLMENT -> {
                        val installmentId = intent.getStringExtra(EXTRA_INSTALLMENT_ID)
                        if (!installmentId.isNullOrBlank()) {
                            val now = System.currentTimeMillis()
                            repository.toggleInstallmentPayment(
                                installmentId = installmentId,
                                isPaid = true,
                                paidTimestamp = now,
                                actualPaymentDate = now
                            )
                        }
                    }
                    ACTION_POSTPONE_INSTALLMENT -> {
                        val installmentId = intent.getStringExtra(EXTRA_INSTALLMENT_ID)
                        if (!installmentId.isNullOrBlank()) {
                            repository.postponeInstallmentDueDate(installmentId, days = 1)
                        }
                    }
                    ACTION_PAY_INVOICE -> {
                        val invoiceId = intent.getStringExtra(EXTRA_INVOICE_ID)
                        if (!invoiceId.isNullOrBlank()) {
                            repository.payInvoice(invoiceId, actualPaymentDate = System.currentTimeMillis())
                        }
                    }
                }
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, DueReminderManager.NOTIFICATION_ID)
                NotificationManagerCompat.from(context).cancel(notificationId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PAY_INSTALLMENT = "com.platform.app.ACTION_PAY_INSTALLMENT"
        const val ACTION_POSTPONE_INSTALLMENT = "com.platform.app.ACTION_POSTPONE_INSTALLMENT"
        const val ACTION_PAY_INVOICE = "com.platform.app.ACTION_PAY_INVOICE"
        const val EXTRA_INSTALLMENT_ID = "extra_installment_id"
        const val EXTRA_INVOICE_ID = "extra_invoice_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
