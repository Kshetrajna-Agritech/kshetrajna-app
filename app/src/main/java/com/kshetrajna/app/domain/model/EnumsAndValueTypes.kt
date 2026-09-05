package com.kshetrajna.app.domain.model

/**
 * Synchronization states as defined in DATA_MODEL.md and OFFLINE_SYNC.md.
 */
enum class SyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED
}

/**
 * Measurement classifications as defined in DATA_MODEL.md.
 * Ensures explicit distinction between direct physical measurements, manual entries,
 * model inferences, forecasts, device states, and safety interlocks.
 */
enum class MeasurementCategory {
    MEASURED,
    MANUAL,
    INFERRED,
    EXTERNAL_FORECAST,
    DEVICE_STATE,
    SAFETY
}

/**
 * Overall system safety status matching SAFETY_STATES.md.
 */
enum class SystemSafetyStatus {
    NORMAL,
    WARNING,
    LOCKED,
    FAULT,
    OFFLINE
}

/**
 * Command lifecycle stages matching SAFETY_STATES.md.
 * Strictly prevents equating an app request with physical actuation.
 */
enum class CommandLifecycleStatus {
    COMMAND_REQUESTED,
    COMMAND_SENT,
    COMMAND_ACCEPTED,
    ACTUATOR_RUNNING,
    ACTUATOR_STOPPED,
    COMMAND_REJECTED
}

/**
 * Physical actuator operation state.
 */
enum class ActuatorStatus {
    STOPPED,
    RUNNING,
    FAULTED,
    LOCKED_OUT
}

/**
 * Supported irrigation control command types.
 */
enum class IrrigationCommandType {
    START_IRRIGATION,
    STOP_IRRIGATION,
    PAUSE_IRRIGATION
}

/**
 * Hardware and environmental safety interlock fault types supported by firmware architecture.
 */
enum class SafetyFaultType {
    STRAY_CURRENT_SOIL_FAULT,
    ROOT_ZONE_THERMAL_SHOCK,
    INLINE_SALINITY_FERTIGATION,
    UNKNOWN_HARDWARE_FAULT
}

/**
 * Severity level for user alerts.
 */
enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Source transport for sensor readings.
 */
enum class SensorSource {
    LORA_FIELD_NODE,
    BLE_DIRECT,
    LOCAL_SIMULATION
}
