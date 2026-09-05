package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Foundation implementation of [TelemetryRepository].
 * Connects domain repository contracts to local/remote data sources.
 */
class DefaultTelemetryRepository(
    private val localDataSource: LocalDataSource
) : TelemetryRepository {
    override fun observeNodes(): Flow<Resource<List<Node>>> {
        return localDataSource.getNodes().map { nodes ->
            Resource.Success(nodes)
        }
    }
}
