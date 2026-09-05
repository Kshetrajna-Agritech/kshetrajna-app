package com.kshetrajna.app.data.local

import com.kshetrajna.app.data.local.mapper.toDomain
import com.kshetrajna.app.data.local.mapper.toEntity
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.Farm
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SensorSource
import com.kshetrajna.app.domain.model.SoilAnalysis
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityMapperTest {

    @Test
    fun `Farm mapping is bidirectional and preserves values`() {
        val domain = Farm(id = "f-1", name = "Green Acres", location = "Sector 4", createdAtEpochMillis = 1000L)
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
    }

    @Test
    fun `CropProfile mapping preserves nullable thresholds`() {
        val domain = CropProfile(id = "cp-1", cropName = "Wheat")
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertNull(mappedBack.targetSoilMoistureMinPercent)
    }

    @Test
    fun `Node mapping is bidirectional`() {
        val domain = Node(id = "n-1", farmId = "f-1", name = "Node North", hardwareAddress = "AA:BB:CC", isOnline = true, lastSeenEpochMillis = 2000L)
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
    }

    @Test
    fun `SensorReading mapping preserves MEASURED category and timestamp`() {
        val domain = SensorReading(
            id = "sr-1",
            nodeId = "n-1",
            timestampEpochMillis = 5000L,
            soilMoisturePercent = 42.5f,
            soilTemperatureCelsius = 24.1f,
            soilEcDsPerM = 1.2f,
            source = SensorSource.LORA_FIELD_NODE
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertEquals(MeasurementCategory.MEASURED, mappedBack.category)
        assertEquals(5000L, mappedBack.timestampEpochMillis)
    }

    @Test
    fun `ManualPH mapping preserves MANUAL category and sync status`() {
        val domain = ManualPH(
            id = "mph-1",
            nodeId = "n-1",
            timestampEpochMillis = 6000L,
            phValue = 6.8f,
            notes = "Soil test kit A",
            syncStatus = SyncStatus.PENDING
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertEquals(MeasurementCategory.MANUAL, mappedBack.category)
        assertEquals(SyncStatus.PENDING, mappedBack.syncStatus)
    }

    @Test
    fun `WeatherData mapping preserves EXTERNAL_FORECAST category`() {
        val domain = WeatherData(
            id = "w-1",
            farmId = "f-1",
            retrievedAtEpochMillis = 7000L,
            rainfallMm = 12.0f,
            temperatureCelsius = 28.0f
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertEquals(MeasurementCategory.EXTERNAL_FORECAST, mappedBack.category)
    }

    @Test
    fun `SoilAnalysis mapping preserves lab details`() {
        val domain = SoilAnalysis(id = "sa-1", nodeId = "n-1", sampledAtEpochMillis = 8000L, soilType = "Clay Loam", labName = "AgriLab")
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
    }

    @Test
    fun `NpkResult mapping preserves INFERRED category`() {
        val domain = NpkResult(
            id = "npk-1",
            nodeId = "n-1",
            timestampEpochMillis = 9000L,
            inferredNitrogenPpm = 15.0f,
            modelVersion = "v1.0"
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertEquals(MeasurementCategory.INFERRED, mappedBack.category)
    }

    @Test
    fun `IrrigationState and Command mappings distinguish request from actuator state`() {
        val state = IrrigationState(
            id = "ist-1",
            nodeId = "n-1",
            timestampEpochMillis = 10000L,
            status = ActuatorStatus.RUNNING,
            activeFlowRateLpm = 5.2f
        )
        val command = IrrigationCommand(
            id = "cmd-1",
            nodeId = "n-1",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_SENT,
            requestedAtEpochMillis = 9990L
        )

        val mappedState = state.toEntity().toDomain()
        val mappedCommand = command.toEntity().toDomain()

        assertEquals(MeasurementCategory.DEVICE_STATE, mappedState.category)
        assertEquals(ActuatorStatus.RUNNING, mappedState.status)
        assertEquals(CommandLifecycleStatus.COMMAND_SENT, mappedCommand.lifecycleStatus)
    }

    @Test
    fun `SafetyState mapping preserves fault list and LOCKED status`() {
        val fault = SafetyFault(type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT, message = "Current leak", triggeredAtEpochMillis = 11000L)
        val domain = SafetyState(
            id = "safe-1",
            nodeId = "n-1",
            timestampEpochMillis = 11000L,
            status = SystemSafetyStatus.LOCKED,
            activeFaults = listOf(fault)
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertTrue(mappedBack.isLockedOut)
        assertEquals(1, mappedBack.activeFaults.size)
        assertEquals(SafetyFaultType.STRAY_CURRENT_SOIL_FAULT, mappedBack.activeFaults.first().type)
    }

    @Test
    fun `Alert mapping preserves notification metadata`() {
        val domain = Alert(
            id = "alt-1",
            nodeId = "n-1",
            timestampEpochMillis = 12000L,
            severity = AlertSeverity.CRITICAL,
            category = MeasurementCategory.SAFETY,
            title = "Hardware Fault",
            message = "Current fault detected",
            affectedZone = "Zone A"
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
    }

    @Test
    fun `SyncRecord mapping preserves offline sync state and retry count`() {
        val domain = SyncRecord(
            id = "sync-1",
            entityType = "ManualPH",
            entityId = "mph-1",
            syncStatus = SyncStatus.PENDING,
            createdAtEpochMillis = 13000L,
            retryCount = 2
        )
        val entity = domain.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(domain, mappedBack)
        assertEquals(SyncStatus.PENDING, mappedBack.syncStatus)
        assertEquals(2, mappedBack.retryCount)
    }
}
