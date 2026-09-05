package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Node
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for node and telemetry data access.
 */
interface TelemetryRepository {
    fun observeNodes(): Flow<Resource<List<Node>>>
}
