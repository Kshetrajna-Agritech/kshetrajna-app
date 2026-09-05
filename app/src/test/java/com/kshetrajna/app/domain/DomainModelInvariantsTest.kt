package com.kshetrajna.app.domain

import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelInvariantsTest {

    @Test
    fun `ManualPH enforces valid physical pH range and MANUAL category`() {
        val validPh = ManualPH(
            id = "mph-1",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            phValue = 6.5f
        )
        assertEquals(6.5f, validPh.phValue, 0.001f)
        assertEquals(MeasurementCategory.MANUAL, validPh.category)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ManualPH throws exception when pH value exceeds 14`() {
        ManualPH(
            id = "mph-invalid",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            phValue = 14.5f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ManualPH throws exception when pH value is below 0`() {
        ManualPH(
            id = "mph-invalid-neg",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            phValue = -0.5f
        )
    }

    @Test
    fun `Measurement categories strictly distinguish telemetry, manual, inferred, and safety`() {
        val sensorReading = SensorReading(
            id = "sr-1",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            soilMoisturePercent = 35.0f
        )
        val npkResult = NpkResult(
            id = "npk-1",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            inferredNitrogenPpm = 12.0f
        )
        val safetyState = SafetyState(
            id = "safe-1",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.NORMAL
        )

        assertEquals(MeasurementCategory.MEASURED, sensorReading.category)
        assertEquals(MeasurementCategory.INFERRED, npkResult.category)
        assertEquals(MeasurementCategory.SAFETY, safetyState.category)
    }

    @Test
    fun `SafetyState correctly computes isLockedOut`() {
        val normalState = SafetyState(
            id = "s-1",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.NORMAL
        )
        assertFalse(normalState.isLockedOut)

        val lockedState = SafetyState(
            id = "s-2",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.LOCKED
        )
        assertTrue(lockedState.isLockedOut)

        val faultStateWithActiveFaults = SafetyState(
            id = "s-3",
            nodeId = "node-1",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.WARNING,
            activeFaults = listOf(
                SafetyFault(
                    type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                    message = "Soil leakage current detected",
                    triggeredAtEpochMillis = 1000L
                )
            )
        )
        assertTrue(faultStateWithActiveFaults.isLockedOut)
    }

    @Test
    fun `IrrigationCommand lifecycle states represent command progression`() {
        val commandRequested = IrrigationCommand(
            id = "cmd-1",
            nodeId = "node-1",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_REQUESTED,
            requestedAtEpochMillis = 1000L
        )
        assertEquals(CommandLifecycleStatus.COMMAND_REQUESTED, commandRequested.lifecycleStatus)

        val commandRejected = IrrigationCommand(
            id = "cmd-2",
            nodeId = "node-1",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_REJECTED,
            requestedAtEpochMillis = 1000L,
            rejectionReason = "Safety interlock active"
        )
        assertEquals("Safety interlock active", commandRejected.rejectionReason)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `IrrigationCommand rejected status requires non-blank rejection reason`() {
        IrrigationCommand(
            id = "cmd-3",
            nodeId = "node-1",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_REJECTED,
            requestedAtEpochMillis = 1000L,
            rejectionReason = ""
        )
    }

    @Test
    fun `SyncRecord enforces non-negative retry count`() {
        val syncRecord = SyncRecord(
            id = "sr-1",
            entityType = "ManualPH",
            entityId = "mph-1",
            syncStatus = SyncStatus.PENDING,
            createdAtEpochMillis = 1000L,
            retryCount = 0
        )
        assertEquals(SyncStatus.PENDING, syncRecord.syncStatus)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `SyncRecord throws exception on negative retry count`() {
        SyncRecord(
            id = "sr-invalid",
            entityType = "ManualPH",
            entityId = "mph-1",
            syncStatus = SyncStatus.FAILED,
            createdAtEpochMillis = 1000L,
            retryCount = -1
        )
    }

    @Test
    fun `CropProfile permits optional nullable fields without hardcoded default thresholds`() {
        val profile = CropProfile(
            id = "cp-1",
            cropName = "Tomato"
        )
        assertNull(profile.targetSoilMoistureMinPercent)
        assertNull(profile.targetSoilMoistureMaxPercent)
        assertNull(profile.targetPhMin)
        assertNull(profile.targetPhMax)
    }
}
