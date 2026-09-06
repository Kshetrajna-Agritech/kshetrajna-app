package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.DashboardData
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.WeatherData
import com.kshetrajna.app.domain.repository.IrrigationRepository
import com.kshetrajna.app.domain.repository.SafetyRepository
import com.kshetrajna.app.domain.repository.SyncRepository
import com.kshetrajna.app.domain.repository.TelemetryRepository
import com.kshetrajna.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Domain use case coordinating dashboard telemetry, weather, irrigation, safety, and sync flows.
 * Preserves UI -> ViewModel -> Use Case -> Repository -> Data Sources architecture.
 */
open class GetDashboardDataUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val weatherRepository: WeatherRepository,
    private val irrigationRepository: IrrigationRepository,
    private val safetyRepository: SafetyRepository,
    private val syncRepository: SyncRepository,
) {

    @Suppress("UNCHECKED_CAST")
    open operator fun invoke(targetNodeId: String = "sim_node_01"): Flow<Resource<DashboardData>> {
        return combine(
            telemetryRepository.observeNodes(),
            telemetryRepository.observeReadingsForNode(targetNodeId),
            weatherRepository.observeLatestWeather(),
            irrigationRepository.observeLatestIrrigationState(targetNodeId),
            irrigationRepository.observeCommandsForNode(targetNodeId),
            safetyRepository.observeLatestSafetyState(targetNodeId),
            safetyRepository.observeAlerts(),
            syncRepository.observeSyncStatus(),
        ) { flows ->
            val nodesRes = flows[0] as Resource<List<Node>>
            val readingsRes = flows[1] as Resource<List<SensorReading>>
            val weatherRes = flows[2] as Resource<WeatherData?>
            val irrigationStateRes = flows[3] as Resource<IrrigationState?>
            val commandsRes = flows[4] as Resource<List<IrrigationCommand>>
            val safetyRes = flows[5] as Resource<SafetyState?>
            val alertsRes = flows[6] as Resource<List<Alert>>
            val syncRes = flows[7] as Resource<SyncStatus>

            val node = (nodesRes as? Resource.Success)?.data?.find { it.id == targetNodeId }
                ?: (nodesRes as? Resource.Success)?.data?.firstOrNull()

            val latestReading = (readingsRes as? Resource.Success)?.data?.firstOrNull()
            val latestWeather = (weatherRes as? Resource.Success)?.data
            val irrigationState = (irrigationStateRes as? Resource.Success)?.data
            val latestCommand = (commandsRes as? Resource.Success)?.data?.firstOrNull()
            val safetyState = (safetyRes as? Resource.Success)?.data
            val alerts = (alertsRes as? Resource.Success)?.data ?: emptyList()
            val syncStatus = (syncRes as? Resource.Success)?.data ?: SyncStatus.SYNCED

            val data = DashboardData(
                node = node,
                latestReading = latestReading,
                latestWeather = latestWeather,
                irrigationState = irrigationState,
                latestCommand = latestCommand,
                safetyState = safetyState,
                syncStatus = syncStatus,
                alerts = alerts,
            )

            Resource.Success(data)
        }
    }
}
