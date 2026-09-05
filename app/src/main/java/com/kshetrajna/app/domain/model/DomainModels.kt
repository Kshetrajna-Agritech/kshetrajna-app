package com.kshetrajna.app.domain.model

/**
 * Domain entity representing a farm node.
 */
data class Node(
    val id: String,
    val name: String,
    val isOnline: Boolean = false
)

/**
 * Synchronization states as defined in DATA_MODEL.md.
 */
enum class SyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED
}

/**
 * Measurement classifications as defined in DATA_MODEL.md.
 */
enum class MeasurementCategory {
    MEASURED,
    MANUAL,
    INFERRED,
    EXTERNAL_FORECAST,
    DEVICE_STATE,
    SAFETY
}
