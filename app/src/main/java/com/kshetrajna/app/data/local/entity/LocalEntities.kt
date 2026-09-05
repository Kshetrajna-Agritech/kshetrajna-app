package com.kshetrajna.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SensorSource
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String?,
    val createdAtEpochMillis: Long
)

@Entity(tableName = "crop_profiles")
data class CropProfileEntity(
    @PrimaryKey val id: String,
    val cropName: String,
    val growthStage: String?,
    val targetSoilMoistureMinPercent: Float?,
    val targetSoilMoistureMaxPercent: Float?,
    val targetPhMin: Float?,
    val targetPhMax: Float?
)

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val name: String,
    val hardwareAddress: String?,
    val isOnline: Boolean,
    val lastSeenEpochMillis: Long?
)

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val soilMoisturePercent: Float?,
    val soilTemperatureCelsius: Float?,
    val soilEcDsPerM: Float?,
    val airTemperatureCelsius: Float?,
    val airHumidityPercent: Float?,
    val source: SensorSource,
    val category: MeasurementCategory
)

@Entity(tableName = "manual_ph_entries")
data class ManualPHEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val phValue: Float,
    val notes: String?,
    val enteredByUserId: String?,
    val syncStatus: SyncStatus,
    val category: MeasurementCategory
)

@Entity(tableName = "weather_data")
data class WeatherDataEntity(
    @PrimaryKey val id: String,
    val farmId: String?,
    val retrievedAtEpochMillis: Long,
    val rainfallMm: Float?,
    val temperatureCelsius: Float?,
    val humidityPercent: Float?,
    val conditionText: String?,
    val sourceName: String,
    val isCached: Boolean,
    val category: MeasurementCategory
)

@Entity(tableName = "soil_analyses")
data class SoilAnalysisEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val sampledAtEpochMillis: Long,
    val soilType: String?,
    val organicMatterPercent: Float?,
    val labName: String?,
    val notes: String?
)

@Entity(tableName = "npk_results")
data class NpkResultEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val inferredNitrogenPpm: Float?,
    val inferredPhosphorusPpm: Float?,
    val inferredPotassiumPpm: Float?,
    val modelVersion: String?,
    val confidenceScore: Float?,
    val category: MeasurementCategory
)

@Entity(tableName = "irrigation_states")
data class IrrigationStateEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val status: ActuatorStatus,
    val activeFlowRateLpm: Float?,
    val category: MeasurementCategory
)

@Entity(tableName = "irrigation_commands")
data class IrrigationCommandEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val commandType: IrrigationCommandType,
    val lifecycleStatus: CommandLifecycleStatus,
    val requestedAtEpochMillis: Long,
    val respondedAtEpochMillis: Long?,
    val rejectionReason: String?
)

@Entity(tableName = "safety_states")
data class SafetyStateEntity(
    @PrimaryKey val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val status: SystemSafetyStatus,
    val activeFaults: List<SafetyFault>,
    val category: MeasurementCategory
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val nodeId: String?,
    val timestampEpochMillis: Long,
    val severity: AlertSeverity,
    val category: MeasurementCategory,
    val title: String,
    val message: String,
    val affectedZone: String?,
    val isAcknowledged: Boolean
)

@Entity(tableName = "sync_records")
data class SyncRecordEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val syncStatus: SyncStatus,
    val createdAtEpochMillis: Long,
    val syncedAtEpochMillis: Long?,
    val retryCount: Int,
    val lastErrorMessage: String?
)
