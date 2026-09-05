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
) {
    init {
        require(phValue in 0.0f..14.0f) {
            "Manual pH value ($phValue) must be within valid physical range [0.0, 14.0]"
        }
    }
}

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
