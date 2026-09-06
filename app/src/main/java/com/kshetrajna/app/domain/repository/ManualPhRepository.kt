package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for manual pH entry and local persistence before sync.
 */
interface ManualPhRepository {
    fun observeManualPhForNode(nodeId: String): Flow<Resource<List<ManualPH>>>
    suspend fun recordManualPh(entry: ManualPH): Resource<Unit>
    suspend fun updateManualPhSyncStatus(id: String, status: SyncStatus): Resource<Unit>
}
