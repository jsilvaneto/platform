package com.platform.app.core.security

import com.platform.app.core.preferences.PreferencesManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppLockStateTest {

    private val preferencesManager = mockk<PreferencesManager>()
    private lateinit var appLockState: AppLockState

    @Before
    fun setUp() {
        every { preferencesManager.lockTimeoutSeconds } returns flowOf(30)
        appLockState = AppLockState(preferencesManager)
    }

    @Test
    fun initialState_isLocked() {
        assertFalse(appLockState.isUnlocked.value)
    }

    @Test
    fun unlock_updatesStateToUnlocked() {
        appLockState.unlock()
        assertTrue(appLockState.isUnlocked.value)
    }

    @Test
    fun lock_updatesStateToLocked() {
        appLockState.unlock()
        assertTrue(appLockState.isUnlocked.value)

        appLockState.lock()
        assertFalse(appLockState.isUnlocked.value)
    }

    @Test
    fun foreground_afterTimeoutExpired_locksApp() = runTest {
        appLockState.unlock()
        assertTrue(appLockState.isUnlocked.value)

        appLockState.setLockTimeoutSeconds(30)

        val backgroundTime = 1000000L
        appLockState.onAppBackgrounded(backgroundTime)

        // 35 segundos depois (> 30s)
        val foregroundTime = backgroundTime + 35000L
        appLockState.onAppForegrounded(foregroundTime)

        assertFalse(appLockState.isUnlocked.value)
    }

    @Test
    fun foreground_beforeTimeoutExpired_keepsAppUnlocked() = runTest {
        appLockState.unlock()
        assertTrue(appLockState.isUnlocked.value)

        appLockState.setLockTimeoutSeconds(60)

        val backgroundTime = 1000000L
        appLockState.onAppBackgrounded(backgroundTime)

        // 20 segundos depois (< 60s)
        val foregroundTime = backgroundTime + 20000L
        appLockState.onAppForegrounded(foregroundTime)

        assertTrue(appLockState.isUnlocked.value)
    }

    @Test
    fun immediateTimeout_locksInstantlyUponForeground() = runTest {
        appLockState.unlock()
        assertTrue(appLockState.isUnlocked.value)

        appLockState.setLockTimeoutSeconds(0)

        val backgroundTime = 1000000L
        appLockState.onAppBackgrounded(backgroundTime)

        val foregroundTime = backgroundTime + 100L
        appLockState.onAppForegrounded(foregroundTime)

        assertFalse(appLockState.isUnlocked.value)
    }
}
