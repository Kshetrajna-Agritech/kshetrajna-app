package com.kshetrajna.app.data.local

import com.kshetrajna.app.data.local.mapper.toDomain
import com.kshetrajna.app.data.local.mapper.toEntity
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
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

    override fun getNodes(): Flow<List<Node>> {
        return database.nodeDao().observeAllNodes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertNode(node: Node) {
        database.nodeDao().insertNode(node.toEntity())
    }

    fun observeReadingsForNode(nodeId: String): Flow<List<SensorReading>> {
        return database.sensorReadingDao().observeReadingsForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertSensorReading(reading: SensorReading) {
        database.sensorReadingDao().insertReading(reading.toEntity())
    }

    fun observeManualPhForNode(nodeId: String): Flow<List<ManualPH>> {
        return database.manualPHDao().observeManualPhForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertManualPh(entry: ManualPH) {
        database.manualPHDao().insertManualPh(entry.toEntity())
    }

    suspend fun updateManualPh(entry: ManualPH) {
        database.manualPHDao().updateManualPh(entry.toEntity())
    }

    fun observeLatestWeather(): Flow<WeatherData?> {
        return database.weatherDataDao().observeLatestWeather().map { it?.toDomain() }
    }

    suspend fun insertWeatherData(weather: WeatherData) {
        database.weatherDataDao().insertWeatherData(weather.toEntity())
    }

    fun observeLatestNpkForNode(nodeId: String): Flow<NpkResult?> {
        return database.npkResultDao().observeLatestNpkForNode(nodeId).map { it?.toDomain() }
    }

    suspend fun insertNpkResult(result: NpkResult) {
        database.npkResultDao().insertNpkResult(result.toEntity())
    }

    fun observeLatestIrrigationState(nodeId: String): Flow<IrrigationState?> {
        return database.irrigationDao().observeLatestStateForNode(nodeId).map { it?.toDomain() }
    }

    suspend fun insertIrrigationState(state: IrrigationState) {
        database.irrigationDao().insertState(state.toEntity())
    }

    fun observeCommandsForNode(nodeId: String): Flow<List<IrrigationCommand>> {
        return database.irrigationDao().observeCommandsForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertIrrigationCommand(command: IrrigationCommand) {
        database.irrigationDao().insertCommand(command.toEntity())
    }

    suspend fun updateIrrigationCommand(command: IrrigationCommand) {
        database.irrigationDao().updateCommand(command.toEntity())
    }

    fun observeLatestSafetyState(nodeId: String): Flow<SafetyState?> {
        return database.safetyDao().observeLatestSafetyStateForNode(nodeId).map { it?.toDomain() }
    }

    suspend fun insertSafetyState(state: SafetyState) {
        database.safetyDao().insertSafetyState(state.toEntity())
    }

    fun observeAllAlerts(): Flow<List<Alert>> {
        return database.safetyDao().observeAllAlerts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertAlert(alert: Alert) {
        database.safetyDao().insertAlert(alert.toEntity())
    }

    suspend fun getPendingSyncRecords(): List<SyncRecord> {
        return database.syncRecordDao().getRecordsByStatus(SyncStatus.PENDING).map { it.toDomain() }
    }

    suspend fun insertSyncRecord(record: SyncRecord) {
        database.syncRecordDao().insertSyncRecord(record.toEntity())
    }

    suspend fun updateSyncRecord(record: SyncRecord) {
        database.syncRecordDao().updateSyncRecord(record.toEntity())
    }
}
