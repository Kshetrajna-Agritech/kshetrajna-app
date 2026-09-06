package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationData
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.WeatherData
import com.kshetrajna.app.domain.repository.IrrigationRepository
import com.kshetrajna.app.domain.repository.SafetyRepository
import com.kshetrajna.app.domain.repository.TelemetryRepository
import com.kshetrajna.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Domain use case aggregating irrigation state, command history, safety state, and environmental context.
 */
open class GetIrrigationDataUseCase(
    private val irrigationRepository: IrrigationRepository,
    private val safetyRepository: SafetyRepository,
    private val telemetryRepository: TelemetryRepository,
    private val weatherRepository: WeatherRepository,
) {
    @Suppress("UNCHECKED_CAST")
    open operator fun invoke(nodeId: String = "sim_node_01"): Flow<Resource<IrrigationData>> {
        return combine(
            irrigationRepository.observeLatestIrrigationState(nodeId),
            irrigationRepository.observeCommandsForNode(nodeId),
            safetyRepository.observeLatestSafetyState(nodeId),
            telemetryRepository.observeReadingsForNode(nodeId),
            weatherRepository.observeLatestWeather()
        ) { flows ->
            val irrigationStateRes = flows[0] as Resource<IrrigationState?>
            val commandsRes = flows[1] as Resource<List<IrrigationCommand>>
            val safetyRes = flows[2] as Resource<SafetyState?>
            val readingsRes = flows[3] as Resource<List<SensorReading>>
            val weatherRes = flows[4] as Resource<WeatherData?>

            val irrigationState = (irrigationStateRes as? Resource.Success)?.data
            val commands = (commandsRes as? Resource.Success)?.data ?: emptyList()
            val sortedCommands = commands.sortedByDescending { it.requestedAtEpochMillis }
            val safetyState = (safetyRes as? Resource.Success)?.data
            val latestReading = (readingsRes as? Resource.Success)?.data?.firstOrNull()
            val latestWeather = (weatherRes as? Resource.Success)?.data

            val data = IrrigationData(
                nodeId = nodeId,
                latestIrrigationState = irrigationState,
                commandHistory = sortedCommands,
                latestCommand = sortedCommands.firstOrNull(),
                safetyState = safetyState,
                latestReading = latestReading,
                latestWeather = latestWeather
            )

            Resource.Success(data)
        }
    }
}
