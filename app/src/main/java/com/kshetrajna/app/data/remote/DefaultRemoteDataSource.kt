package com.kshetrajna.app.data.remote

import com.kshetrajna.app.core.result.Resource

/**
 * Foundation implementation stub for RemoteDataSource.
 * Remote API integration will be implemented when approved backend API contracts are provided.
 */
class DefaultRemoteDataSource : RemoteDataSource {
    override suspend fun syncPendingData(): Resource<Unit> {
        return Resource.Success(Unit)
    }
}
