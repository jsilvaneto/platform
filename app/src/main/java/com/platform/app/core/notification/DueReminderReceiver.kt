package com.platform.app.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.platform.app.domain.repository.FinancialRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DueReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: FinancialRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.materializeRecurringCardInvoices()
                DueReminderManager.checkAndNotifyDueExpenses(context, repository)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
