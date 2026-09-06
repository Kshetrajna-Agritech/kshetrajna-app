package com.kshetrajna.app.core.simulation

/**
 * Explicit named simulation scenarios as mandated by M5 specification.
 */
enum class SimulationScenario(val displayName: String, val description: String) {
    NORMAL_FARM("Normal Farm Operation", "Optimal soil moisture, EC, online node, normal safety status"),
    DRY_SOIL("Dry Soil Depletion", "Deterministically decreasing soil moisture over simulation steps"),
    RAIN_EVENT("Rainfall Weather Event", "Elevated rainfall, high humidity, and rising soil moisture"),
    HIGH_EC_WARNING("High Electrical Conductivity", "Elevated soil EC condition indicating salinity risk"),
    SAFETY_LOCKOUT("Safety Fault & Lockout", "Safety system in LOCKED/FAULT status with active soil fault"),
    NODE_OFFLINE("Node Offline / Disconnected", "Field node in offline state with stale telemetry"),
    IRRIGATION_COMMAND_LIFECYCLE("Irrigation Command Lifecycle", "Step-by-step command progression distinct from actuator state")
}
