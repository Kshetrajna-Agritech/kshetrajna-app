package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for synchronization state management.
 */
interface SyncRepository {
    fun observeSyncStatus(): Flow<Resource<SyncStatus>>
    suspend fun triggerSync(): Resource<Unit>
}
