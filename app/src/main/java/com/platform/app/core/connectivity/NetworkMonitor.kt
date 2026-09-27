package com.platform.app.core.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Monitor reativo do status de conectividade de rede do dispositivo.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
