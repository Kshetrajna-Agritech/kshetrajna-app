package com.kshetrajna.app.data.local

import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.Farm
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SoilAnalysis
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

/**
 * Contract interface for local persistence operations.
 * Forms the primary data source boundary for Kshetrajna's offline-first architecture.
 */
interface LocalDataSource {
    fun getFarms(): Flow<List<Farm>>
    suspend fun getFarmById(id: String): Farm?
    suspend fun insertFarm(farm: Farm)

    fun getCropProfiles(): Flow<List<CropProfile>>
    suspend fun getCropProfileById(id: String): CropProfile?
    suspend fun insertCropProfile(profile: CropProfile)

    fun getNodes(): Flow<List<Node>>
    fun getNodesForFarm(farmId: String): Flow<List<Node>>
    suspend fun getNodeById(id: String): Node?
    suspend fun insertNode(node: Node)

    fun observeReadingsForNode(nodeId: String): Flow<List<SensorReading>>
    suspend fun insertSensorReading(reading: SensorReading)

    fun observeManualPhForNode(nodeId: String): Flow<List<ManualPH>>
    suspend fun insertManualPh(entry: ManualPH)
    suspend fun updateManualPh(entry: ManualPH)

    fun observeLatestWeather(): Flow<WeatherData?>
    suspend fun insertWeatherData(weather: WeatherData)

    fun observeSoilAnalysesForNode(nodeId: String): Flow<List<SoilAnalysis>>
    suspend fun insertSoilAnalysis(analysis: SoilAnalysis)

    fun observeLatestNpkForNode(nodeId: String): Flow<NpkResult?>
    suspend fun insertNpkResult(result: NpkResult)

    fun observeLatestIrrigationState(nodeId: String): Flow<IrrigationState?>
    suspend fun insertIrrigationState(state: IrrigationState)

    fun observeCommandsForNode(nodeId: String): Flow<List<IrrigationCommand>>
    suspend fun insertIrrigationCommand(command: IrrigationCommand)
    suspend fun updateIrrigationCommand(command: IrrigationCommand)

    fun observeLatestSafetyState(nodeId: String): Flow<SafetyState?>
    suspend fun insertSafetyState(state: SafetyState)

    fun observeAllAlerts(): Flow<List<Alert>>
    suspend fun insertAlert(alert: Alert)

    suspend fun getPendingSyncRecords(): List<SyncRecord>
    suspend fun insertSyncRecord(record: SyncRecord)
    suspend fun updateSyncRecord(record: SyncRecord)
}
