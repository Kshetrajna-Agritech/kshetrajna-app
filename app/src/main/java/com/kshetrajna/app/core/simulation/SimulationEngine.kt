package com.kshetrajna.app.core.simulation

import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.Farm
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SensorSource
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData

/**
 * Deterministic data generator for Kshetrajna simulation scenarios.
 * Guarantees that running the same scenario with the same seed produces identical data.
 */
class SimulationEngine(
    val clock: SimulationClock = SimulationClock(),
    val seed: Long = 42L
) {

    fun generateFarm(): Farm = Farm(
        id = "sim_farm_01",
        name = "Kshetrajna Demo Farm",
        location = "Mandya, Karnataka",
        createdAtEpochMillis = clock.currentTimeEpochMillis()
    )

    fun generateCropProfile(): CropProfile = CropProfile(
        id = "sim_crop_tomato",
        cropName = "Tomato",
        growthStage = "Vegetative",
        targetSoilMoistureMinPercent = 25.0f,
        targetSoilMoistureMaxPercent = 45.0f,
        targetPhMin = 6.0f,
        targetPhMax = 7.0f
    )

    fun generateNode(scenario: SimulationScenario, step: Int = 0): Node {
        val isOnline = scenario != SimulationScenario.NODE_OFFLINE
        val lastSeen = if (isOnline) {
            clock.currentTimeEpochMillis()
        } else {
            clock.currentTimeEpochMillis() - (3600_000L * (step + 1))
        }
        return Node(
            id = "sim_node_01",
            farmId = "sim_farm_01",
            name = "Field Zone 1 Node",
            hardwareAddress = "ESP32:SIM:01",
            isOnline = isOnline,
            lastSeenEpochMillis = lastSeen
        )
    }

    fun generateSensorReading(nodeId: String, scenario: SimulationScenario, step: Int = 0): SensorReading {
        val timestamp = clock.currentTimeEpochMillis()
        val (moisture, temp, ec) = when (scenario) {
            SimulationScenario.NORMAL_FARM -> Triple(35.0f + (step % 3), 25.0f + (step % 2), 1.2f)
            SimulationScenario.DRY_SOIL -> Triple((35.0f - (step * 5.0f)).coerceAtLeast(5.0f), 28.0f + step, 1.1f)
            SimulationScenario.RAIN_EVENT -> Triple((40.0f + (step * 4.0f)).coerceAtMost(95.0f), 22.0f, 0.9f)
            SimulationScenario.HIGH_EC_WARNING -> Triple(30.0f, 26.0f, 4.8f + (step * 0.2f))
            SimulationScenario.SAFETY_LOCKOUT -> Triple(20.0f, 38.0f + step, 1.5f)
            SimulationScenario.NODE_OFFLINE -> Triple(30.0f, 25.0f, 1.2f)
            SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE -> Triple(28.0f + (step * 2.0f), 25.0f, 1.3f)
        }

        return SensorReading(
            id = "sim_sr_${scenario.name.lowercase()}_$step",
            nodeId = nodeId,
            timestampEpochMillis = timestamp,
            soilMoisturePercent = moisture,
            soilTemperatureCelsius = temp,
            soilEcDsPerM = ec,
            airTemperatureCelsius = 30.0f,
            airHumidityPercent = 65.0f,
            source = SensorSource.LOCAL_SIMULATION,
            category = MeasurementCategory.MEASURED
        )
    }

    fun generateWeatherData(farmId: String, scenario: SimulationScenario, step: Int = 0): WeatherData {
        val timestamp = clock.currentTimeEpochMillis()
        val (rain, condition) = when (scenario) {
            SimulationScenario.RAIN_EVENT -> Pair(15.0f + (step * 5.0f), "Heavy Rain")
            else -> Pair(0.0f, "Clear / Part Cloud")
        }

        return WeatherData(
            id = "sim_weather_$step",
            farmId = farmId,
            retrievedAtEpochMillis = timestamp,
            rainfallMm = rain,
            temperatureCelsius = 29.0f,
            humidityPercent = if (scenario == SimulationScenario.RAIN_EVENT) 92.0f else 60.0f,
            conditionText = condition,
            sourceName = "Deterministic Simulation Engine",
            isCached = true,
            category = MeasurementCategory.EXTERNAL_FORECAST
        )
    }

    fun generateSafetyState(nodeId: String, scenario: SimulationScenario, step: Int = 0): SafetyState {
        val timestamp = clock.currentTimeEpochMillis()
        val (status, faults) = when (scenario) {
            SimulationScenario.SAFETY_LOCKOUT -> Pair(
                SystemSafetyStatus.LOCKED,
                listOf(
                    SafetyFault(
                        type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                        message = "Simulated soil stray-current fault detected (ACS712 threshold exceeded)",
                        triggeredAtEpochMillis = timestamp
                    )
                )
            )
            SimulationScenario.HIGH_EC_WARNING -> Pair(
                SystemSafetyStatus.WARNING,
                listOf(
                    SafetyFault(
                        type = SafetyFaultType.INLINE_SALINITY_FERTIGATION,
                        message = "Simulated inline salinity warning (EC elevated)",
                        triggeredAtEpochMillis = timestamp
                    )
                )
            )
            else -> Pair(SystemSafetyStatus.NORMAL, emptyList())
        }

        return SafetyState(
            id = "sim_safety_$step",
            nodeId = nodeId,
            timestampEpochMillis = timestamp,
            status = status,
            activeFaults = faults,
            category = MeasurementCategory.SAFETY
        )
    }

    fun generateIrrigationState(nodeId: String, scenario: SimulationScenario, step: Int = 0): IrrigationState {
        val timestamp = clock.currentTimeEpochMillis()
        val (status, flow) = when (scenario) {
            SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE -> {
                if (step >= 3) {
                    Pair(ActuatorStatus.RUNNING, 8.5f)
                } else {
                    Pair(ActuatorStatus.STOPPED, 0.0f)
                }
            }
            SimulationScenario.SAFETY_LOCKOUT -> Pair(ActuatorStatus.LOCKED_OUT, 0.0f)
            else -> Pair(ActuatorStatus.STOPPED, 0.0f)
        }

        return IrrigationState(
            id = "sim_state_$step",
            nodeId = nodeId,
            timestampEpochMillis = timestamp,
            status = status,
            activeFlowRateLpm = flow,
            category = MeasurementCategory.DEVICE_STATE
        )
    }

    fun generateIrrigationCommand(nodeId: String, scenario: SimulationScenario, step: Int = 0): IrrigationCommand {
        val timestamp = clock.currentTimeEpochMillis()
        val (lifecycle, reason) = when (scenario) {
            SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE -> {
                when (step) {
                    0 -> Pair(CommandLifecycleStatus.COMMAND_REQUESTED, null)
                    1 -> Pair(CommandLifecycleStatus.COMMAND_SENT, null)
                    2 -> Pair(CommandLifecycleStatus.COMMAND_ACCEPTED, null)
                    3 -> Pair(CommandLifecycleStatus.ACTUATOR_RUNNING, null)
                    else -> Pair(CommandLifecycleStatus.ACTUATOR_STOPPED, null)
                }
            }
            SimulationScenario.SAFETY_LOCKOUT -> Pair(
                CommandLifecycleStatus.COMMAND_REJECTED,
                "Simulated safety interlock lockout active"
            )
            else -> Pair(CommandLifecycleStatus.COMMAND_REQUESTED, null)
        }

        return IrrigationCommand(
            id = "sim_cmd_$step",
            nodeId = nodeId,
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = lifecycle,
            requestedAtEpochMillis = timestamp,
            respondedAtEpochMillis = if (lifecycle != CommandLifecycleStatus.COMMAND_REQUESTED) timestamp + 500L else null,
            rejectionReason = reason
        )
    }

    fun generateAlert(nodeId: String, scenario: SimulationScenario, step: Int = 0): Alert? {
        val timestamp = clock.currentTimeEpochMillis()
        return when (scenario) {
            SimulationScenario.SAFETY_LOCKOUT -> Alert(
                id = "sim_alert_lockout_$step",
                nodeId = nodeId,
                timestampEpochMillis = timestamp,
                severity = AlertSeverity.CRITICAL,
                category = MeasurementCategory.SAFETY,
                title = "Safety Lockout Triggered",
                message = "Simulated ACS712 stray-current interlock activated. Pump commands rejected.",
                affectedZone = "Zone 1"
            )
            SimulationScenario.HIGH_EC_WARNING -> Alert(
                id = "sim_alert_ec_$step",
                nodeId = nodeId,
                timestampEpochMillis = timestamp,
                severity = AlertSeverity.WARNING,
                category = MeasurementCategory.SAFETY,
                title = "High Salinity Warning",
                message = "Soil EC exceeded 4.5 dS/m in Zone 1.",
                affectedZone = "Zone 1"
            )
            else -> null
        }
    }

    fun generateNpkResult(nodeId: String, step: Int = 0): NpkResult {
        val timestamp = clock.currentTimeEpochMillis()
        return NpkResult(
            id = "sim_npk_$step",
            nodeId = nodeId,
            timestampEpochMillis = timestamp,
            inferredNitrogenPpm = 18.0f + (step % 4),
            inferredPhosphorusPpm = 8.5f + (step % 2),
            inferredPotassiumPpm = 22.0f + (step % 5),
            modelVersion = "simulated_model_v1",
            confidenceScore = 0.85f,
            category = MeasurementCategory.INFERRED
        )
    }
}
