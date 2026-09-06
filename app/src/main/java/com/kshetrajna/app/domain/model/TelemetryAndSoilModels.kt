package com.kshetrajna.app.domain.model

/**
 * Measured sensor telemetry recorded by LoRa field nodes or BLE hardware.
 */
data class SensorReading(
    val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val soilMoisturePercent: Float? = null,
    val soilTemperatureCelsius: Float? = null,
    val soilEcDsPerM: Float? = null,
    val airTemperatureCelsius: Float? = null,
    val airHumidityPercent: Float? = null,
    val source: SensorSource = SensorSource.LORA_FIELD_NODE,
    val category: MeasurementCategory = MeasurementCategory.MEASURED
)

/**
 * Consolidated domain model representing latest telemetry and chronological history for a node.
 */
data class SoilTelemetryData(
    val node: Node? = null,
    val latestReading: SensorReading? = null,
    val historyReadings: List<SensorReading> = emptyList()
)

/**
 * App-entered manual pH measurement.
 * Strictly distinct from automated sensor telemetry.
 */
data class ManualPH(
    val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val phValue: Float,
    val notes: String? = null,
    val enteredByUserId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val category: MeasurementCategory = MeasurementCategory.MANUAL
)

/**
 * Laboratory soil sample analysis record.
 */
data class SoilAnalysis(
    val id: String,
    val nodeId: String,
    val sampledAtEpochMillis: Long,
    val soilType: String? = null,
    val organicMatterPercent: Float? = null,
    val labName: String? = null,
    val notes: String? = null
)

/**
 * Inferred N/P/K fertility status.
 * Represents derived/inferred model output, NEVER direct sensor or laboratory measurements.
 */
data class NpkResult(
    val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val inferredNitrogenPpm: Float? = null,
    val inferredPhosphorusPpm: Float? = null,
    val inferredPotassiumPpm: Float? = null,
    val modelVersion: String? = null,
    val confidenceScore: Float? = null,
    val category: MeasurementCategory = MeasurementCategory.INFERRED
)

/**
 * Contextual inputs (EC, soil temperature, soil moisture, manual pH) and latest NPK inference status.
 * Preserves strict measurement provenance and explicit model availability state.
 */
data class FertilityData(
    val nodeId: String = "sim_node_01",
    val latestReading: SensorReading? = null,
    val latestManualPh: ManualPH? = null,
    val latestNpkResult: NpkResult? = null,
    val isModelConfigured: Boolean = false,
    val modelStatusMessage: String = "NPK inference engine model formula and calibration constants are pending approved contract specification (TBD)."
)
