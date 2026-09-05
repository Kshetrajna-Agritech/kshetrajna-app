package com.kshetrajna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kshetrajna.app.data.local.entity.AlertEntity
import com.kshetrajna.app.data.local.entity.CropProfileEntity
import com.kshetrajna.app.data.local.entity.FarmEntity
import com.kshetrajna.app.data.local.entity.IrrigationCommandEntity
import com.kshetrajna.app.data.local.entity.IrrigationStateEntity
import com.kshetrajna.app.data.local.entity.ManualPHEntity
import com.kshetrajna.app.data.local.entity.NodeEntity
import com.kshetrajna.app.data.local.entity.NpkResultEntity
import com.kshetrajna.app.data.local.entity.SafetyStateEntity
import com.kshetrajna.app.data.local.entity.SensorReadingEntity
import com.kshetrajna.app.data.local.entity.SoilAnalysisEntity
import com.kshetrajna.app.data.local.entity.SyncRecordEntity
import com.kshetrajna.app.data.local.entity.WeatherDataEntity
import com.kshetrajna.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms WHERE id = :id")
    suspend fun getFarmById(id: String): FarmEntity?

    @Query("SELECT * FROM farms")
    fun observeFarms(): Flow<List<FarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)
}

@Dao
interface CropProfileDao {
    @Query("SELECT * FROM crop_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): CropProfileEntity?

    @Query("SELECT * FROM crop_profiles")
    fun observeProfiles(): Flow<List<CropProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CropProfileEntity)
}

@Dao
interface NodeDao {
    @Query("SELECT * FROM nodes WHERE id = :id")
    suspend fun getNodeById(id: String): NodeEntity?

    @Query("SELECT * FROM nodes WHERE farmId = :farmId")
    fun observeNodesForFarm(farmId: String): Flow<List<NodeEntity>>

    @Query("SELECT * FROM nodes")
    fun observeAllNodes(): Flow<List<NodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NodeEntity)
}

@Dao
interface SensorReadingDao {
    @Query("SELECT * FROM sensor_readings WHERE nodeId = :nodeId ORDER BY timestampEpochMillis DESC LIMIT :limit")
    fun observeReadingsForNode(nodeId: String, limit: Int = 100): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE nodeId = :nodeId ORDER BY timestampEpochMillis DESC LIMIT 1")
    fun observeLatestReadingForNode(nodeId: String): Flow<SensorReadingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: SensorReadingEntity)
}

@Dao
interface ManualPHDao {
    @Query("SELECT * FROM manual_ph_entries WHERE nodeId = :nodeId ORDER BY timestampEpochMillis DESC")
    fun observeManualPhForNode(nodeId: String): Flow<List<ManualPHEntity>>

    @Query("SELECT * FROM manual_ph_entries WHERE syncStatus = :status")
    suspend fun getEntriesBySyncStatus(status: SyncStatus = SyncStatus.PENDING): List<ManualPHEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManualPh(entry: ManualPHEntity)

    @Update
    suspend fun updateManualPh(entry: ManualPHEntity)
}

@Dao
interface WeatherDataDao {
    @Query("SELECT * FROM weather_data ORDER BY retrievedAtEpochMillis DESC LIMIT 1")
    fun observeLatestWeather(): Flow<WeatherDataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weather: WeatherDataEntity)
}

@Dao
interface SoilAnalysisDao {
    @Query("SELECT * FROM soil_analyses WHERE nodeId = :nodeId ORDER BY sampledAtEpochMillis DESC")
    fun observeAnalysesForNode(nodeId: String): Flow<List<SoilAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: SoilAnalysisEntity)
}

@Dao
interface NpkResultDao {
    @Query("SELECT * FROM npk_results WHERE nodeId = :nodeId ORDER BY timestampEpochMillis DESC LIMIT 1")
    fun observeLatestNpkForNode(nodeId: String): Flow<NpkResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpkResult(result: NpkResultEntity)
}

@Dao
interface IrrigationDao {
    @Query("SELECT * FROM irrigation_states WHERE nodeId = :nodeId ORDER BY timestampEpochMillis DESC LIMIT 1")
    fun observeLatestStateForNode(nodeId: String): Flow<IrrigationStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(state: IrrigationStateEntity)

    @Query("SELECT * FROM irrigation_commands WHERE nodeId = :nodeId ORDER BY requestedAtEpochMillis DESC")
    fun observeCommandsForNode(nodeId: String): Flow<List<IrrigationCommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: IrrigationCommandEntity)

    @Update
    suspend fun updateCommand(command: IrrigationCommandEntity)
}

@Dao
interface SafetyDao {
    @Query("SELECT * FROM safety_states WHERE nodeId = :nodeId ORDER BY timestampEpochMillis DESC LIMIT 1")
    fun observeLatestSafetyStateForNode(nodeId: String): Flow<SafetyStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafetyState(state: SafetyStateEntity)

    @Query("SELECT * FROM alerts ORDER BY timestampEpochMillis DESC")
    fun observeAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)
}

@Dao
interface SyncRecordDao {
    @Query("SELECT * FROM sync_records WHERE syncStatus = :status")
    suspend fun getRecordsByStatus(status: SyncStatus): List<SyncRecordEntity>

    @Query("SELECT * FROM sync_records WHERE id = :id")
    suspend fun getRecordById(id: String): SyncRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncRecord(record: SyncRecordEntity)

    @Update
    suspend fun updateSyncRecord(record: SyncRecordEntity)
}
