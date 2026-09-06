package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SoilAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for node and telemetry data access.
 */
interface TelemetryRepository {
    fun observeNodes(farmId: String? = null): Flow<Resource<List<Node>>>
    fun observeReadingsForNode(nodeId: String): Flow<Resource<List<SensorReading>>>
    suspend fun saveSensorReading(reading: SensorReading): Resource<Unit>
    fun observeSoilAnalyses(nodeId: String): Flow<Resource<List<SoilAnalysis>>>
    suspend fun saveSoilAnalysis(analysis: SoilAnalysis): Resource<Unit>
}
