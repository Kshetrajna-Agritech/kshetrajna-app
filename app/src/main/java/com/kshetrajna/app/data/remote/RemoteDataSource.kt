package com.kshetrajna.app.data.remote

import com.kshetrajna.app.core.result.Resource

/**
 * Remote API data source contract interface.
 * Exact HTTP paths and DTOs will be bound when approved backend contracts are provided.
 */
interface RemoteDataSource {
    suspend fun syncPendingData(): Resource<Unit>
}
