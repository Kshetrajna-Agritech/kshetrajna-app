package com.kshetrajna.app.ui.soil

import com.kshetrajna.app.domain.model.SoilTelemetryData

/**
 * Data holder for Soil Telemetry screen success state.
 */
data class SoilUiStateData(
    val telemetryData: SoilTelemetryData = SoilTelemetryData(),
    val isOfflineNode: Boolean = false,
)
