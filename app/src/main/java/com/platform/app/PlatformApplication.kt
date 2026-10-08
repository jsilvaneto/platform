package com.platform.app

import android.app.Application
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            seedInitialDataUseCase()
            extendRecurringBillsUseCase()
        }
    }
}
