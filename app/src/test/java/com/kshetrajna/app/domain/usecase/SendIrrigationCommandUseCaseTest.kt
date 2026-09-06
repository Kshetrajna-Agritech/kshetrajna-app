package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultSyncRepository
import com.kshetrajna.app.data.repository.FakeRemoteDataSource
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SendIrrigationCommandUseCaseTest {

    @Test
    fun `when safety status is NORMAL command request is saved locally and queued for sync`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val syncRepo = DefaultSyncRepository(localDataSource, FakeRemoteDataSource())

        val safetyNormal = SafetyState(
            id = "saf-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.NORMAL
        )
        localDataSource.safetyStates.value = listOf(safetyNormal)

        val useCase = SendIrrigationCommandUseCase(
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            syncRepository = syncRepo
        )

        val result = useCase(nodeId = "sim_node_01", commandType = IrrigationCommandType.START_IRRIGATION)
        assertTrue(result is Resource.Success)

        // Verify command saved in local database
        val commands = localDataSource.irrigationCommands.value
        assertEquals(1, commands.size)
        val savedCommand = commands.first()
        assertEquals(IrrigationCommandType.START_IRRIGATION, savedCommand.commandType)
        assertEquals(CommandLifecycleStatus.COMMAND_REQUESTED, savedCommand.lifecycleStatus)

        // Verify SyncRecord queued
        val syncRecords = localDataSource.syncRecords.value
        assertEquals(1, syncRecords.size)
        assertEquals("IrrigationCommand", syncRecords.first().entityType)
        assertEquals(SyncStatus.PENDING, syncRecords.first().syncStatus)
    }

    @Test
    fun `when safety status is LOCKED command is rejected and saved locally with rejection reason`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val syncRepo = DefaultSyncRepository(localDataSource, FakeRemoteDataSource())

        val safetyLocked = SafetyState(
            id = "saf-locked",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.LOCKED,
            activeFaults = listOf(
                SafetyFault(
                    type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                    message = "Soil leakage current detected",
                    triggeredAtEpochMillis = 1000L
                )
            )
        )
        localDataSource.safetyStates.value = listOf(safetyLocked)

        val useCase = SendIrrigationCommandUseCase(
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            syncRepository = syncRepo
        )

        val result = useCase(nodeId = "sim_node_01", commandType = IrrigationCommandType.START_IRRIGATION)
        assertTrue(result is Resource.Error)

        val errorMsg = (result as Resource.Error).message
        assertTrue(errorMsg.contains("LOCKED"))

        // Verify rejected command saved in local database
        val commands = localDataSource.irrigationCommands.value
        assertEquals(1, commands.size)
        val rejectedCommand = commands.first()
        assertEquals(CommandLifecycleStatus.COMMAND_REJECTED, rejectedCommand.lifecycleStatus)
        assertNotNull(rejectedCommand.rejectionReason)
        assertTrue(rejectedCommand.rejectionReason!!.contains("Soil leakage current detected"))
    }
}
