package com.platform.app.core.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.platform.app.core.preferences.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockState @Inject constructor(
    private val preferencesManager: PreferencesManager
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var lastBackgroundAt: Long = 0L
    private var cachedTimeoutSeconds: Int = 0

    init {
        scope.launch {
            preferencesManager.lockTimeoutSeconds.collect { timeout ->
                cachedTimeoutSeconds = timeout
            }
        }
        runCatching {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    fun unlock() {
        _isUnlocked.value = true
        lastBackgroundAt = 0L
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun setLockTimeoutSeconds(seconds: Int) {
        cachedTimeoutSeconds = seconds
    }

    fun onAppBackgrounded(timestamp: Long = System.currentTimeMillis()) {
        lastBackgroundAt = timestamp
    }

    fun onAppForegrounded(timestamp: Long = System.currentTimeMillis()) {
        if (lastBackgroundAt > 0L) {
            val elapsedSeconds = (timestamp - lastBackgroundAt) / 1000
            if (elapsedSeconds >= cachedTimeoutSeconds) {
                _isUnlocked.value = false
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        onAppBackgrounded(System.currentTimeMillis())
    }

    override fun onStart(owner: LifecycleOwner) {
        onAppForegrounded(System.currentTimeMillis())
    }
}
