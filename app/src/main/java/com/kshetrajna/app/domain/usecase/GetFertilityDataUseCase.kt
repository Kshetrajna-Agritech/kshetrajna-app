package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.FertilityData
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.repository.FertilityRepository
import com.kshetrajna.app.domain.repository.ManualPhRepository
import com.kshetrajna.app.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Domain use case aggregating inputs (measured EC, temperature, moisture, manual pH)
 * and latest NPK inference status for the Fertility screen.
 */
open class GetFertilityDataUseCase(
    private val telemetryRepository: TelemetryRepository,
    private val manualPhRepository: ManualPhRepository,
    private val fertilityRepository: FertilityRepository,
) {
    @Suppress("UNCHECKED_CAST")
    open operator fun invoke(nodeId: String = "sim_node_01"): Flow<Resource<FertilityData>> {
        return combine(
            telemetryRepository.observeReadingsForNode(nodeId),
            manualPhRepository.observeManualPhForNode(nodeId),
            fertilityRepository.observeLatestNpkForNode(nodeId)
        ) { flows ->
            val readingsRes = flows[0] as Resource<List<SensorReading>>
            val manualPhsRes = flows[1] as Resource<List<ManualPH>>
            val npkRes = flows[2] as Resource<NpkResult?>

            val readings = (readingsRes as? Resource.Success)?.data
            val manualPhs = (manualPhsRes as? Resource.Success)?.data
            val npkResult = (npkRes as? Resource.Success)?.data

            val latestReading = readings?.firstOrNull()
            val latestManualPh = manualPhs?.firstOrNull()

            val fertilityData = FertilityData(
                nodeId = nodeId,
                latestReading = latestReading,
                latestManualPh = latestManualPh,
                latestNpkResult = npkResult,
                isModelConfigured = false,
                modelStatusMessage = "NPK inference engine model formula and calibration constants are pending approved contract specification (TBD)."
            )

            Resource.Success(fertilityData)
        }
    }
}
