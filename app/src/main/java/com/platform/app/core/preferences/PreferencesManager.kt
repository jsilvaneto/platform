package com.platform.app.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "platform_user_prefs")

interface PreferencesManager {
    val isDarkMode: Flow<Boolean?>
    val isAmoledMode: Flow<Boolean>
    val isBiometricEnabled: Flow<Boolean>
    val lastOfflineBackupTimestamp: Flow<Long>
    val appIcon: Flow<String>
    val areSeedsApplied: Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean?)
    suspend fun setAmoledMode(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setAppIcon(iconKey: String)
    suspend fun updateLastOfflineBackupTimestamp(timestamp: Long)
    suspend fun setSeedsApplied(applied: Boolean)
}

@Singleton
class PreferencesManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesManager {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_AMOLED_MODE = booleanPreferencesKey("is_amoled_mode")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
        val APP_ICON = androidx.datastore.preferences.core.stringPreferencesKey("app_icon")
        val SEEDS_APPLIED = booleanPreferencesKey("seeds_applied")
    }

    override val isDarkMode: Flow<Boolean?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE]
        }

    override val isBiometricEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: false
        }

    override val lastOfflineBackupTimestamp: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] ?: 0L
        }

    override val isAmoledMode: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_AMOLED_MODE] ?: false
        }

    override suspend fun setDarkMode(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(PreferencesKeys.IS_DARK_MODE)
            } else {
                preferences[PreferencesKeys.IS_DARK_MODE] = enabled
            }
        }
    }

    override suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AMOLED_MODE] = enabled
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    override val appIcon: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.APP_ICON] ?: "classic"
        }

    override suspend fun setAppIcon(iconKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_ICON] = iconKey
        }
    }

    override suspend fun updateLastOfflineBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }

    override val areSeedsApplied: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SEEDS_APPLIED] ?: false
        }

    override suspend fun setSeedsApplied(applied: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEEDS_APPLIED] = applied
        }
    }
}
