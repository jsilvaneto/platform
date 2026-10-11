package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.platform.app.data.local.entity.SyncOutboxEntity
import com.platform.app.data.local.entity.SyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    // --- Outbox Queries ---
    @Query("SELECT * FROM sync_outbox WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingOutboxEntries(): Flow<List<SyncOutboxEntity>>

    @Query("SELECT * FROM sync_outbox WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPendingOutboxBatch(limit: Int = 50): List<SyncOutboxEntity>

    @Query("SELECT COUNT(*) FROM sync_outbox WHERE status = 'PENDING'")
    fun getPendingOutboxCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(entry: SyncOutboxEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueAll(entries: List<SyncOutboxEntity>)

    @Update
    suspend fun updateOutboxEntry(entry: SyncOutboxEntity)

    @Query("UPDATE sync_outbox SET status = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE sync_outbox SET status = 'FAILED', errorMessage = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun markFailed(id: String, error: String)

    @Query("DELETE FROM sync_outbox WHERE status = 'SYNCED'")
    suspend fun purgeSyncedEntries()

    // --- State Queries ---
    @Query("SELECT value FROM sync_state WHERE key = :key LIMIT 1")
    suspend fun getSyncState(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSyncState(state: SyncStateEntity)

    @Query("DELETE FROM sync_state WHERE key = :key")
    suspend fun clearSyncState(key: String)
}
