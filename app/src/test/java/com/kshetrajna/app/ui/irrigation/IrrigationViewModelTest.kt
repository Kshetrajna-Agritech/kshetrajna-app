package com.kshetrajna.app.ui.irrigation

import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultSyncRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.repository.FakeRemoteDataSource
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.usecase.GetIrrigationDataUseCase
import com.kshetrajna.app.domain.usecase.SendIrrigationCommandUseCase
import com.kshetrajna.app.ui.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IrrigationViewModelTest {

    private fun createViewModel(
        localDataSource: InMemoryLocalDataSource,
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): IrrigationViewModel {
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val weatherRepo = DefaultWeatherRepository(localDataSource)
        val syncRepo = DefaultSyncRepository(localDataSource, FakeRemoteDataSource())

        val getIrrigationDataUseCase = GetIrrigationDataUseCase(
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            telemetryRepository = telemetryRepo,
            weatherRepository = weatherRepo
        )
        val sendIrrigationCommandUseCase = SendIrrigationCommandUseCase(
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            syncRepository = syncRepo
        )

        return IrrigationViewModel(
            getIrrigationDataUseCase = getIrrigationDataUseCase,
            sendIrrigationCommandUseCase = sendIrrigationCommandUseCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `viewModel loads command history and separates command status from physical actuator status`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val command = IrrigationCommand(
            id = "cmd-1",
            nodeId = "sim_node_01",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_ACCEPTED,
            requestedAtEpochMillis = 1000L
        )
        localDataSource.irrigationCommands.value = listOf(command)

        val state = IrrigationState(
            id = "st-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = ActuatorStatus.STOPPED,
            activeFlowRateLpm = 0.0f
        )
        localDataSource.irrigationStates.value = listOf(state)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Success)

        val data = (uiState as UiState.Success<IrrigationUiStateData>).data.irrigationData
        assertEquals(CommandLifecycleStatus.COMMAND_ACCEPTED, data.latestCommand?.lifecycleStatus)
        assertEquals(ActuatorStatus.STOPPED, data.latestIrrigationState?.status)
    }

    @Test
    fun `requesting irrigation when safety is normal records requested command successfully`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val safetyNormal = SafetyState(
            id = "saf-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.NORMAL
        )
        localDataSource.safetyStates.value = listOf(safetyNormal)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        viewModel.requestCommand(IrrigationCommandType.START_IRRIGATION)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Success)

        val uiData = (uiState as UiState.Success<IrrigationUiStateData>).data
        assertNotNull(uiData.commandActionMessage)
        assertNull(uiData.commandErrorMessage)
        assertEquals(1, localDataSource.irrigationCommands.value.size)
    }

    @Test
    fun `requesting irrigation when safety is locked rejects command and displays error message`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val safetyLocked = SafetyState(
            id = "saf-locked",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.LOCKED,
            activeFaults = listOf(
                SafetyFault(
                    type = SafetyFaultType.ROOT_ZONE_THERMAL_SHOCK,
                    message = "Soil temperature exceeded thermal shock limit",
                    triggeredAtEpochMillis = 1000L
                )
            )
        )
        localDataSource.safetyStates.value = listOf(safetyLocked)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        viewModel.requestCommand(IrrigationCommandType.START_IRRIGATION)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Success)

        val uiData = (uiState as UiState.Success<IrrigationUiStateData>).data
        assertNull(uiData.commandActionMessage)
        assertNotNull(uiData.commandErrorMessage)
        assertTrue(uiData.commandErrorMessage!!.contains("LOCKED"))

        // Verify rejected command was saved with rejection reason
        val savedCommand = localDataSource.irrigationCommands.value.first()
        assertEquals(CommandLifecycleStatus.COMMAND_REJECTED, savedCommand.lifecycleStatus)
        assertTrue(savedCommand.rejectionReason!!.contains("thermal shock"))
    }
}
