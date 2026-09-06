package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.repository.FertilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Implementation of [FertilityRepository].
 */
class DefaultFertilityRepository(
    private val localDataSource: LocalDataSource,
) : FertilityRepository {

    override fun observeLatestNpkForNode(nodeId: String): Flow<Resource<NpkResult?>> {
        return localDataSource.observeLatestNpkForNode(nodeId).map { result ->
            Resource.Success(result) as Resource<NpkResult?>
        }.catch { e ->
            emit(Resource.Error("Failed to observe NPK fertility result: ${e.message}", e))
        }
    }

    override suspend fun saveNpkResult(result: NpkResult): Resource<Unit> {
        return try {
            localDataSource.insertNpkResult(result)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save NPK result locally: ${e.message}", e)
        }
    }
}
