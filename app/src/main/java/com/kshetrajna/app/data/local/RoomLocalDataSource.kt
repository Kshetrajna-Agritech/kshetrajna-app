package com.kshetrajna.app.data.local

import com.kshetrajna.app.data.local.mapper.toDomain
import com.kshetrajna.app.data.local.mapper.toEntity
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
import kotlinx.coroutines.flow.map

/**
 * Concrete Room-backed implementation of local data source operations.
 * Primary source of truth for offline-first architecture.
 */
class RoomLocalDataSource(
    private val database: KshetrajnaDatabase,
) : LocalDataSource {

    override fun getFarms(): Flow<List<Farm>> {
        return database.farmDao().observeFarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getFarmById(id: String): Farm? {
        return database.farmDao().getFarmById(id)?.toDomain()
    }

    override suspend fun insertFarm(farm: Farm) {
        database.farmDao().insertFarm(farm.toEntity())
    }

    override fun getCropProfiles(): Flow<List<CropProfile>> {
        return database.cropProfileDao().observeProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCropProfileById(id: String): CropProfile? {
        return database.cropProfileDao().getProfileById(id)?.toDomain()
    }

    override suspend fun insertCropProfile(profile: CropProfile) {
        database.cropProfileDao().insertProfile(profile.toEntity())
    }

    override fun getNodes(): Flow<List<Node>> {
        return database.nodeDao().observeAllNodes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNodesForFarm(farmId: String): Flow<List<Node>> {
        return database.nodeDao().observeNodesForFarm(farmId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNodeById(id: String): Node? {
        return database.nodeDao().getNodeById(id)?.toDomain()
    }

    override suspend fun insertNode(node: Node) {
        database.nodeDao().insertNode(node.toEntity())
    }

    override fun observeReadingsForNode(nodeId: String): Flow<List<SensorReading>> {
        return database.sensorReadingDao().observeReadingsForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertSensorReading(reading: SensorReading) {
        database.sensorReadingDao().insertReading(reading.toEntity())
    }

    override fun observeManualPhForNode(nodeId: String): Flow<List<ManualPH>> {
        return database.manualPHDao().observeManualPhForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertManualPh(entry: ManualPH) {
        database.manualPHDao().insertManualPh(entry.toEntity())
    }

    override suspend fun updateManualPh(entry: ManualPH) {
        database.manualPHDao().updateManualPh(entry.toEntity())
    }

    override fun observeLatestWeather(): Flow<WeatherData?> {
        return database.weatherDataDao().observeLatestWeather().map { it?.toDomain() }
    }

    override suspend fun insertWeatherData(weather: WeatherData) {
        database.weatherDataDao().insertWeatherData(weather.toEntity())
    }

    override fun observeSoilAnalysesForNode(nodeId: String): Flow<List<SoilAnalysis>> {
        return database.soilAnalysisDao().observeAnalysesForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertSoilAnalysis(analysis: SoilAnalysis) {
        database.soilAnalysisDao().insertAnalysis(analysis.toEntity())
    }

    override fun observeLatestNpkForNode(nodeId: String): Flow<NpkResult?> {
        return database.npkResultDao().observeLatestNpkForNode(nodeId).map { it?.toDomain() }
    }

    override suspend fun insertNpkResult(result: NpkResult) {
        database.npkResultDao().insertNpkResult(result.toEntity())
    }

    override fun observeLatestIrrigationState(nodeId: String): Flow<IrrigationState?> {
        return database.irrigationDao().observeLatestStateForNode(nodeId).map { it?.toDomain() }
    }

    override suspend fun insertIrrigationState(state: IrrigationState) {
        database.irrigationDao().insertState(state.toEntity())
    }

    override fun observeCommandsForNode(nodeId: String): Flow<List<IrrigationCommand>> {
        return database.irrigationDao().observeCommandsForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertIrrigationCommand(command: IrrigationCommand) {
        database.irrigationDao().insertCommand(command.toEntity())
    }

    override suspend fun updateIrrigationCommand(command: IrrigationCommand) {
        database.irrigationDao().updateCommand(command.toEntity())
    }

    override fun observeLatestSafetyState(nodeId: String): Flow<SafetyState?> {
        return database.safetyDao().observeLatestSafetyStateForNode(nodeId).map { it?.toDomain() }
    }

    override suspend fun insertSafetyState(state: SafetyState) {
        database.safetyDao().insertSafetyState(state.toEntity())
    }

    override fun observeAllAlerts(): Flow<List<Alert>> {
        return database.safetyDao().observeAllAlerts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertAlert(alert: Alert) {
        database.safetyDao().insertAlert(alert.toEntity())
    }

    override suspend fun getPendingSyncRecords(): List<SyncRecord> {
        return database.syncRecordDao().getRecordsByStatus(SyncStatus.PENDING).map { it.toDomain() }
    }

    override suspend fun insertSyncRecord(record: SyncRecord) {
        database.syncRecordDao().insertSyncRecord(record.toEntity())
    }

    override suspend fun updateSyncRecord(record: SyncRecord) {
        database.syncRecordDao().updateSyncRecord(record.toEntity())
    }
}
