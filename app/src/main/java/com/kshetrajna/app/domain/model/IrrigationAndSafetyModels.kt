package com.kshetrajna.app.domain.model

/**
 * Actuator/irrigation physical device state.
 */
data class IrrigationState(
    val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val status: ActuatorStatus,
    val activeFlowRateLpm: Float? = null,
    val category: MeasurementCategory = MeasurementCategory.DEVICE_STATE,
)

/**
 * Irrigation command with full lifecycle tracking.
 * Strictly prevents equating command request/transmission with physical pump actuation.
 */
data class IrrigationCommand(
    val id: String,
    val nodeId: String,
    val commandType: IrrigationCommandType,
    val lifecycleStatus: CommandLifecycleStatus,
    val requestedAtEpochMillis: Long,
    val respondedAtEpochMillis: Long? = null,
    val rejectionReason: String? = null,
) {
    init {
        if (lifecycleStatus == CommandLifecycleStatus.COMMAND_REJECTED) {
            require(!rejectionReason.isNullOrBlank()) {
                "A rejection reason must be provided when command lifecycle status is COMMAND_REJECTED"
            }
        }
    }
}

/**
 * Individual safety fault record.
 */
data class SafetyFault(
    val type: SafetyFaultType,
    val message: String,
    val triggeredAtEpochMillis: Long,
)

/**
 * Node safety and lockout status.
 */
data class SafetyState(
    val id: String,
    val nodeId: String,
    val timestampEpochMillis: Long,
    val status: SystemSafetyStatus,
    val activeFaults: List<SafetyFault> = emptyList(),
    val category: MeasurementCategory = MeasurementCategory.SAFETY,
) {
    val isLockedOut: Boolean
        get() = status == SystemSafetyStatus.LOCKED ||
            status == SystemSafetyStatus.FAULT ||
            activeFaults.isNotEmpty()
}

/**
 * System and safety alert notification entity.
 */
data class Alert(
    val id: String,
    val nodeId: String? = null,
    val timestampEpochMillis: Long,
    val severity: AlertSeverity,
    val category: MeasurementCategory,
    val title: String,
    val message: String,
    val affectedZone: String? = null,
    val isAcknowledged: Boolean = false,
)
