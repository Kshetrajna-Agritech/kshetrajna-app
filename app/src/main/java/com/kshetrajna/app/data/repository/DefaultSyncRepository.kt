package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.data.remote.RemoteDataSource
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


/**
 * Concrete implementation of [SyncRepository].
 * Manages synchronization status and pending records without hiding synchronization failures.
 */
class DefaultSyncRepository(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : SyncRepository {

    override fun observeSyncStatus(): Flow<Resource<SyncStatus>> {
        return flow {
            val pendingRecords = localDataSource.getPendingSyncRecords()
            val status = if (pendingRecords.isNotEmpty()) SyncStatus.PENDING else SyncStatus.SYNCED
            emit(Resource.Success(status))
        }
    }

    override suspend fun getPendingSyncRecords(): Resource<List<SyncRecord>> {
        return try {
            val records = localDataSource.getPendingSyncRecords()
            Resource.Success(records)
        } catch (e: Exception) {
            Resource.Error("Failed to retrieve pending sync records: ${e.message}", e)
        }
    }

    override suspend fun saveSyncRecord(record: SyncRecord): Resource<Unit> {
        return try {
            localDataSource.insertSyncRecord(record)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save sync record: ${e.message}", e)
        }
    }

    override suspend fun updateSyncRecord(record: SyncRecord): Resource<Unit> {
        return try {
            localDataSource.updateSyncRecord(record)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update sync record: ${e.message}", e)
        }
    }

    override suspend fun triggerSync(): Resource<Unit> {
        return try {
            val pendingRecords = localDataSource.getPendingSyncRecords()
            if (pendingRecords.isEmpty()) {
                return Resource.Success(Unit)
            }
            // Delegate sync attempt to RemoteDataSource
            remoteDataSource.syncPendingData()
        } catch (e: Exception) {
            Resource.Error("Sync attempt failed: ${e.message}", e)
        }
    }
}
