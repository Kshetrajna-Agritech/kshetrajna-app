package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for synchronization tracking.
 */
interface SyncRepository {
    fun observeSyncStatus(): Flow<Resource<SyncStatus>>
    suspend fun getPendingSyncRecords(): Resource<List<SyncRecord>>
    suspend fun saveSyncRecord(record: SyncRecord): Resource<Unit>
    suspend fun updateSyncRecord(record: SyncRecord): Resource<Unit>
    suspend fun triggerSync(): Resource<Unit>
}
