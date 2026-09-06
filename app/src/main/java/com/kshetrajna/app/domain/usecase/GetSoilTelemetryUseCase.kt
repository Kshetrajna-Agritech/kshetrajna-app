package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SoilTelemetryData
import com.kshetrajna.app.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Domain use case fetching and preparing soil telemetry data for a target node.
 * Returns chronological historical readings and the latest reading from the repository.
 */
open class GetSoilTelemetryUseCase(
    private val telemetryRepository: TelemetryRepository,
) {
    @Suppress("UNCHECKED_CAST")
    open operator fun invoke(targetNodeId: String = "sim_node_01"): Flow<Resource<SoilTelemetryData>> {
        return combine(
            telemetryRepository.observeNodes(),
            telemetryRepository.observeReadingsForNode(targetNodeId),
        ) { flows ->
            val nodesRes = flows[0] as Resource<List<Node>>
            val readingsRes = flows[1] as Resource<List<SensorReading>>

            when {
                nodesRes is Resource.Error -> Resource.Error(nodesRes.message, nodesRes.cause)
                readingsRes is Resource.Error -> Resource.Error(readingsRes.message, readingsRes.cause)
                nodesRes is Resource.Loading || readingsRes is Resource.Loading -> Resource.Loading
                else -> {
                    val nodes = (nodesRes as? Resource.Success)?.data ?: emptyList()
                    val readings = (readingsRes as? Resource.Success)?.data ?: emptyList()

                    val targetNode = nodes.find { it.id == targetNodeId } ?: nodes.firstOrNull()

                    // Chronological ordering (oldest to newest) for trends/charts
                    val chronologicalReadings = readings.sortedBy { it.timestampEpochMillis }
                    val latest = readings.maxByOrNull { it.timestampEpochMillis }

                    val data = SoilTelemetryData(
                        node = targetNode,
                        latestReading = latest,
                        historyReadings = chronologicalReadings,
                    )
                    Resource.Success(data)
                }
            }
        }
    }
}
