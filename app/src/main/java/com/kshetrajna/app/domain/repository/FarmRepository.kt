package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.Farm
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for farm and crop profile management.
 */
interface FarmRepository {
    fun observeFarms(): Flow<Resource<List<Farm>>>
    suspend fun saveFarm(farm: Farm): Resource<Unit>
    fun observeCropProfiles(): Flow<Resource<List<CropProfile>>>
    suspend fun saveCropProfile(profile: CropProfile): Resource<Unit>
}
