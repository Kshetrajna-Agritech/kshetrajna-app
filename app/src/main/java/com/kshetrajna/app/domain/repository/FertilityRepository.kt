package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.NpkResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for inferred fertility status (NPK model outputs).
 */
interface FertilityRepository {
    fun observeLatestNpkForNode(nodeId: String): Flow<Resource<NpkResult?>>
    suspend fun saveNpkResult(result: NpkResult): Resource<Unit>
}
