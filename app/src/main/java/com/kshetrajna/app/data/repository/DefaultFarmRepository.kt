package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.Farm
import com.kshetrajna.app.domain.repository.FarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Implementation of [FarmRepository].
 */
class DefaultFarmRepository(
    private val localDataSource: LocalDataSource,
) : FarmRepository {

    override fun observeFarms(): Flow<Resource<List<Farm>>> {
        return localDataSource.getFarms().map { farms ->
            Resource.Success(farms) as Resource<List<Farm>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe farms: ${e.message}", e))
        }
    }

    override suspend fun saveFarm(farm: Farm): Resource<Unit> {
        return try {
            localDataSource.insertFarm(farm)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save farm locally: ${e.message}", e)
        }
    }

    override fun observeCropProfiles(): Flow<Resource<List<CropProfile>>> {
        return localDataSource.getCropProfiles().map { profiles ->
            Resource.Success(profiles) as Resource<List<CropProfile>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe crop profiles: ${e.message}", e))
        }
    }

    override suspend fun saveCropProfile(profile: CropProfile): Resource<Unit> {
        return try {
            localDataSource.insertCropProfile(profile)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save crop profile locally: ${e.message}", e)
        }
    }
}
