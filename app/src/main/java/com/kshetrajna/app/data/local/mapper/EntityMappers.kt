package com.kshetrajna.app.data.local.mapper

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

fun FarmEntity.toDomain() = Farm(
    id = id,
    name = name,
    location = location,
    createdAtEpochMillis = createdAtEpochMillis
)

fun Farm.toEntity() = FarmEntity(
    id = id,
    name = name,
    location = location,
    createdAtEpochMillis = createdAtEpochMillis
)

fun CropProfileEntity.toDomain() = CropProfile(
    id = id,
    cropName = cropName,
    growthStage = growthStage,
    targetSoilMoistureMinPercent = targetSoilMoistureMinPercent,
    targetSoilMoistureMaxPercent = targetSoilMoistureMaxPercent,
    targetPhMin = targetPhMin,
    targetPhMax = targetPhMax
)

fun CropProfile.toEntity() = CropProfileEntity(
    id = id,
    cropName = cropName,
    growthStage = growthStage,
    targetSoilMoistureMinPercent = targetSoilMoistureMinPercent,
    targetSoilMoistureMaxPercent = targetSoilMoistureMaxPercent,
    targetPhMin = targetPhMin,
    targetPhMax = targetPhMax
)

fun NodeEntity.toDomain() = Node(
    id = id,
    farmId = farmId,
    name = name,
    hardwareAddress = hardwareAddress,
    isOnline = isOnline,
    lastSeenEpochMillis = lastSeenEpochMillis
)

fun Node.toEntity() = NodeEntity(
    id = id,
    farmId = farmId,
    name = name,
    hardwareAddress = hardwareAddress,
    isOnline = isOnline,
    lastSeenEpochMillis = lastSeenEpochMillis
)

fun SensorReadingEntity.toDomain() = SensorReading(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    soilMoisturePercent = soilMoisturePercent,
    soilTemperatureCelsius = soilTemperatureCelsius,
    soilEcDsPerM = soilEcDsPerM,
    airTemperatureCelsius = airTemperatureCelsius,
    airHumidityPercent = airHumidityPercent,
    source = source,
    category = category
)

fun SensorReading.toEntity() = SensorReadingEntity(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    soilMoisturePercent = soilMoisturePercent,
    soilTemperatureCelsius = soilTemperatureCelsius,
    soilEcDsPerM = soilEcDsPerM,
    airTemperatureCelsius = airTemperatureCelsius,
    airHumidityPercent = airHumidityPercent,
    source = source,
    category = category
)

fun ManualPHEntity.toDomain() = ManualPH(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    phValue = phValue,
    notes = notes,
    enteredByUserId = enteredByUserId,
    syncStatus = syncStatus,
    category = category
)

fun ManualPH.toEntity() = ManualPHEntity(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    phValue = phValue,
    notes = notes,
    enteredByUserId = enteredByUserId,
    syncStatus = syncStatus,
    category = category
)

fun WeatherDataEntity.toDomain() = WeatherData(
    id = id,
    farmId = farmId,
    retrievedAtEpochMillis = retrievedAtEpochMillis,
    rainfallMm = rainfallMm,
    temperatureCelsius = temperatureCelsius,
    humidityPercent = humidityPercent,
    conditionText = conditionText,
    sourceName = sourceName,
    isCached = isCached,
    category = category
)

fun WeatherData.toEntity() = WeatherDataEntity(
    id = id,
    farmId = farmId,
    retrievedAtEpochMillis = retrievedAtEpochMillis,
    rainfallMm = rainfallMm,
    temperatureCelsius = temperatureCelsius,
    humidityPercent = humidityPercent,
    conditionText = conditionText,
    sourceName = sourceName,
    isCached = isCached,
    category = category
)

fun SoilAnalysisEntity.toDomain() = SoilAnalysis(
    id = id,
    nodeId = nodeId,
    sampledAtEpochMillis = sampledAtEpochMillis,
    soilType = soilType,
    organicMatterPercent = organicMatterPercent,
    labName = labName,
    notes = notes
)

fun SoilAnalysis.toEntity() = SoilAnalysisEntity(
    id = id,
    nodeId = nodeId,
    sampledAtEpochMillis = sampledAtEpochMillis,
    soilType = soilType,
    organicMatterPercent = organicMatterPercent,
    labName = labName,
    notes = notes
)

fun NpkResultEntity.toDomain() = NpkResult(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    inferredNitrogenPpm = inferredNitrogenPpm,
    inferredPhosphorusPpm = inferredPhosphorusPpm,
    inferredPotassiumPpm = inferredPotassiumPpm,
    modelVersion = modelVersion,
    confidenceScore = confidenceScore,
    category = category
)

fun NpkResult.toEntity() = NpkResultEntity(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    inferredNitrogenPpm = inferredNitrogenPpm,
    inferredPhosphorusPpm = inferredPhosphorusPpm,
    inferredPotassiumPpm = inferredPotassiumPpm,
    modelVersion = modelVersion,
    confidenceScore = confidenceScore,
    category = category
)

fun IrrigationStateEntity.toDomain() = IrrigationState(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    status = status,
    activeFlowRateLpm = activeFlowRateLpm,
    category = category
)

fun IrrigationState.toEntity() = IrrigationStateEntity(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    status = status,
    activeFlowRateLpm = activeFlowRateLpm,
    category = category
)

fun IrrigationCommandEntity.toDomain() = IrrigationCommand(
    id = id,
    nodeId = nodeId,
    commandType = commandType,
    lifecycleStatus = lifecycleStatus,
    requestedAtEpochMillis = requestedAtEpochMillis,
    respondedAtEpochMillis = respondedAtEpochMillis,
    rejectionReason = rejectionReason
)

fun IrrigationCommand.toEntity() = IrrigationCommandEntity(
    id = id,
    nodeId = nodeId,
    commandType = commandType,
    lifecycleStatus = lifecycleStatus,
    requestedAtEpochMillis = requestedAtEpochMillis,
    respondedAtEpochMillis = respondedAtEpochMillis,
    rejectionReason = rejectionReason
)

fun SafetyStateEntity.toDomain() = SafetyState(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    status = status,
    activeFaults = activeFaults,
    category = category
)

fun SafetyState.toEntity() = SafetyStateEntity(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    status = status,
    activeFaults = activeFaults,
    category = category
)

fun AlertEntity.toDomain() = Alert(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    severity = severity,
    category = category,
    title = title,
    message = message,
    affectedZone = affectedZone,
    isAcknowledged = isAcknowledged
)

fun Alert.toEntity() = AlertEntity(
    id = id,
    nodeId = nodeId,
    timestampEpochMillis = timestampEpochMillis,
    severity = severity,
    category = category,
    title = title,
    message = message,
    affectedZone = affectedZone,
    isAcknowledged = isAcknowledged
)

fun SyncRecordEntity.toDomain() = SyncRecord(
    id = id,
    entityType = entityType,
    entityId = entityId,
    syncStatus = syncStatus,
    createdAtEpochMillis = createdAtEpochMillis,
    syncedAtEpochMillis = syncedAtEpochMillis,
    retryCount = retryCount,
    lastErrorMessage = lastErrorMessage
)

fun SyncRecord.toEntity() = SyncRecordEntity(
    id = id,
    entityType = entityType,
    entityId = entityId,
    syncStatus = syncStatus,
    createdAtEpochMillis = createdAtEpochMillis,
    syncedAtEpochMillis = syncedAtEpochMillis,
    retryCount = retryCount,
    lastErrorMessage = lastErrorMessage
)
