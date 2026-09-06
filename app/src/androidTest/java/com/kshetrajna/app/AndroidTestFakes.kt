package com.kshetrajna.app

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.data.remote.RemoteDataSource
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
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AndroidTestLocalDataSource : LocalDataSource {
    val farms = MutableStateFlow<List<Farm>>(emptyList())
    val cropProfiles = MutableStateFlow<List<CropProfile>>(emptyList())
    val nodes = MutableStateFlow<List<Node>>(emptyList())
    val readings = MutableStateFlow<List<SensorReading>>(emptyList())
    val manualPhs = MutableStateFlow<List<ManualPH>>(emptyList())
    val weatherData = MutableStateFlow<WeatherData?>(null)
    val soilAnalyses = MutableStateFlow<List<SoilAnalysis>>(emptyList())
    val npkResults = MutableStateFlow<List<NpkResult>>(emptyList())
    val irrigationStates = MutableStateFlow<List<IrrigationState>>(emptyList())
    val irrigationCommands = MutableStateFlow<List<IrrigationCommand>>(emptyList())
    val safetyStates = MutableStateFlow<List<SafetyState>>(emptyList())
    val alerts = MutableStateFlow<List<Alert>>(emptyList())
    val syncRecords = MutableStateFlow<List<SyncRecord>>(emptyList())

    override fun getFarms(): Flow<List<Farm>> = farms
    override suspend fun getFarmById(id: String): Farm? = farms.value.find { it.id == id }
    override suspend fun insertFarm(farm: Farm) { farms.value = farms.value + farm }

    override fun getCropProfiles(): Flow<List<CropProfile>> = cropProfiles
    override suspend fun getCropProfileById(id: String): CropProfile? = cropProfiles.value.find { it.id == id }
    override suspend fun insertCropProfile(profile: CropProfile) { cropProfiles.value = cropProfiles.value + profile }

    override fun getNodes(): Flow<List<Node>> = nodes
    override fun getNodesForFarm(farmId: String): Flow<List<Node>> = nodes.map { list -> list.filter { it.farmId == farmId } }
    override suspend fun getNodeById(id: String): Node? = nodes.value.find { it.id == id }
    override suspend fun insertNode(node: Node) { nodes.value = nodes.value + node }

    override fun observeReadingsForNode(nodeId: String): Flow<List<SensorReading>> = readings.map { list -> list.filter { it.nodeId == nodeId } }
    override suspend fun insertSensorReading(reading: SensorReading) { readings.value = readings.value + reading }

    override fun observeManualPhForNode(nodeId: String): Flow<List<ManualPH>> = manualPhs.map { list -> list.filter { it.nodeId == nodeId } }
    override suspend fun insertManualPh(entry: ManualPH) { manualPhs.value = manualPhs.value + entry }
    override suspend fun updateManualPh(entry: ManualPH) { manualPhs.value = manualPhs.value.filter { it.id != entry.id } + entry }

    override fun observeLatestWeather(): Flow<WeatherData?> = weatherData
    override suspend fun insertWeatherData(weather: WeatherData) { weatherData.value = weather }

    override fun observeSoilAnalysesForNode(nodeId: String): Flow<List<SoilAnalysis>> = soilAnalyses.map { list -> list.filter { it.nodeId == nodeId } }
    override suspend fun insertSoilAnalysis(analysis: SoilAnalysis) { soilAnalyses.value = soilAnalyses.value + analysis }

    override fun observeLatestNpkForNode(nodeId: String): Flow<NpkResult?> = npkResults.map { list -> list.filter { it.nodeId == nodeId }.maxByOrNull { it.timestampEpochMillis } }
    override suspend fun insertNpkResult(result: NpkResult) { npkResults.value = npkResults.value + result }

    override fun observeLatestIrrigationState(nodeId: String): Flow<IrrigationState?> = irrigationStates.map { list -> list.filter { it.nodeId == nodeId }.maxByOrNull { it.timestampEpochMillis } }
    override suspend fun insertIrrigationState(state: IrrigationState) { irrigationStates.value = irrigationStates.value + state }

    override fun observeCommandsForNode(nodeId: String): Flow<List<IrrigationCommand>> = irrigationCommands.map { list -> list.filter { it.nodeId == nodeId } }
    override suspend fun insertIrrigationCommand(command: IrrigationCommand) { irrigationCommands.value = irrigationCommands.value + command }
    override suspend fun updateIrrigationCommand(command: IrrigationCommand) { irrigationCommands.value = irrigationCommands.value.filter { it.id != command.id } + command }

    override fun observeLatestSafetyState(nodeId: String): Flow<SafetyState?> = safetyStates.map { list -> list.filter { it.nodeId == nodeId }.maxByOrNull { it.timestampEpochMillis } }
    override suspend fun insertSafetyState(state: SafetyState) { safetyStates.value = safetyStates.value + state }

    override fun observeAllAlerts(): Flow<List<Alert>> = alerts
    override suspend fun insertAlert(alert: Alert) { alerts.value = alerts.value + alert }

    override suspend fun getPendingSyncRecords(): List<SyncRecord> = syncRecords.value.filter { it.syncStatus == SyncStatus.PENDING }
    override suspend fun insertSyncRecord(record: SyncRecord) { syncRecords.value = syncRecords.value + record }
    override suspend fun updateSyncRecord(record: SyncRecord) { syncRecords.value = syncRecords.value.filter { it.id != record.id } + record }
}

class AndroidTestRemoteDataSource : RemoteDataSource {
    override suspend fun syncPendingData(): Resource<Unit> = Resource.Success(Unit)
}
