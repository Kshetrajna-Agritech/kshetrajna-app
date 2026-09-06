package com.kshetrajna.app.domain.model

/**
 * Domain data model aggregating dashboard telemetry, weather, irrigation, safety, and sync information.
 */
data class DashboardData(
    val node: Node? = null,
    val latestReading: SensorReading? = null,
    val latestWeather: WeatherData? = null,
    val irrigationState: IrrigationState? = null,
    val latestCommand: IrrigationCommand? = null,
    val safetyState: SafetyState? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val alerts: List<Alert> = emptyList()
)
