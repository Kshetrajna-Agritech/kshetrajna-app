package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.remote.RemoteDataSource
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Foundation implementation of [SyncRepository].
 */
class DefaultSyncRepository(
    private val remoteDataSource: RemoteDataSource
) : SyncRepository {
    override fun observeSyncStatus(): Flow<Resource<SyncStatus>> {
        return flowOf(Resource.Success(SyncStatus.SYNCED))
    }

    override suspend fun triggerSync(): Resource<Unit> {
        return remoteDataSource.syncPendingData()
    }
}
