package com.platform.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.domain.usecase.ExtendRecurringBillsUseCase
import com.platform.app.domain.usecase.SeedInitialDataUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PlatformApplication : Application() {

    @Inject
    lateinit var seedInitialDataUseCase: SeedInitialDataUseCase

    @Inject
    lateinit var extendRecurringBillsUseCase: ExtendRecurringBillsUseCase

    @Inject
    lateinit var database: PlatformDatabase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        com.platform.app.core.notification.DueReminderManager.schedulePeriodicWork(this)
        runCatching {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    applicationScope.launch {
                        database.checkpointWal()
                    }
                }
            })
        }
        applicationScope.launch {
            seedInitialDataUseCase()
            extendRecurringBillsUseCase()
        }
    }
}
