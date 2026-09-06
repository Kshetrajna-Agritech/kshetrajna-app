package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.ManualPhRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Implementation of [ManualPhRepository].
 * Persists manual pH entries locally and queues a [SyncRecord] with [SyncStatus.PENDING].
 */
class DefaultManualPhRepository(
    private val localDataSource: LocalDataSource,
) : ManualPhRepository {

    override fun observeManualPhForNode(nodeId: String): Flow<Resource<List<ManualPH>>> {
        return localDataSource.observeManualPhForNode(nodeId).map { entries ->
            Resource.Success(entries) as Resource<List<ManualPH>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe manual pH entries: ${e.message}", e))
        }
    }

    override suspend fun recordManualPh(entry: ManualPH): Resource<Unit> {
        return try {
            val pendingEntry = entry.copy(syncStatus = SyncStatus.PENDING)
            localDataSource.insertManualPh(pendingEntry)

            val syncRecord = SyncRecord(
                id = "sync_mph_${entry.id}",
                entityType = "ManualPH",
                entityId = entry.id,
                syncStatus = SyncStatus.PENDING,
                createdAtEpochMillis = entry.timestampEpochMillis,
            )
            localDataSource.insertSyncRecord(syncRecord)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to record manual pH entry locally: ${e.message}", e)
        }
    }

    override suspend fun updateManualPhSyncStatus(id: String, status: SyncStatus): Resource<Unit> {
        return try {
            val records = localDataSource.getPendingSyncRecords()
            val record = records.find { (it.entityId == id) && (it.entityType == "ManualPH") }
            record?.let {
                localDataSource.updateSyncRecord(it.copy(syncStatus = status))
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update manual pH sync status: ${e.message}", e)
        }
    }
}
