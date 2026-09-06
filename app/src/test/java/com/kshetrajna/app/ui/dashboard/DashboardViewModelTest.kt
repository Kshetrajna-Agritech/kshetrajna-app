package com.kshetrajna.app.ui.dashboard

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.simulation.SimulationClock
import com.kshetrajna.app.core.simulation.SimulationEngine
import com.kshetrajna.app.core.simulation.SimulationScenario
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultSyncRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.repository.FakeRemoteDataSource
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.data.simulation.SimulatedDataSourceSeeder
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.usecase.GetDashboardDataUseCase
import com.kshetrajna.app.ui.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private fun createViewModel(
        localDataSource: InMemoryLocalDataSource,
        remoteDataSource: FakeRemoteDataSource = FakeRemoteDataSource(),
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): DashboardViewModel {
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val weatherRepo = DefaultWeatherRepository(localDataSource)
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val syncRepo = DefaultSyncRepository(localDataSource, remoteDataSource)

        val useCase = GetDashboardDataUseCase(
            telemetryRepository = telemetryRepo,
            weatherRepository = weatherRepo,
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            syncRepository = syncRepo
        )

        return DashboardViewModel(
            getDashboardDataUseCase = useCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `loadDashboardData populates Success state from seeded local persistence`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.NORMAL_FARM, steps = 1)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<DashboardUiState>).data
        assertNotNull(uiData.data)
        assertEquals("sim_node_01", uiData.data!!.node?.id)
        assertEquals(35.0f, uiData.data!!.latestReading?.soilMoisturePercent!!, 0.001f)
        assertEquals(SystemSafetyStatus.NORMAL, uiData.data!!.safetyState?.status)
        assertFalse(uiData.isOfflineNode)
    }

    @Test
    fun `NODE_OFFLINE scenario marks isOfflineNode true in DashboardUiState`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.NODE_OFFLINE, steps = 1)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<DashboardUiState>).data
        assertTrue(uiData.isOfflineNode)
        assertFalse(uiData.data!!.node!!.isOnline)
    }

    @Test
    fun `SAFETY_LOCKOUT scenario exposes LOCKED safety status and faults`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.SAFETY_LOCKOUT, steps = 1)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<DashboardUiState>).data
        assertEquals(SystemSafetyStatus.LOCKED, uiData.data!!.safetyState?.status)
        assertTrue(uiData.data!!.safetyState!!.isLockedOut)
        assertEquals(1, uiData.data!!.safetyState!!.activeFaults.size)
    }

    @Test
    fun `Irrigation command requested does NOT claim pump is running`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE, steps = 1)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<DashboardUiState>).data
        assertEquals(CommandLifecycleStatus.COMMAND_REQUESTED, uiData.data!!.latestCommand?.lifecycleStatus)
        assertEquals(ActuatorStatus.STOPPED, uiData.data!!.irrigationState?.status)
    }

    @Test
    fun `Dashboard handles missing weather cleanly without crashing`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.NORMAL_FARM, steps = 1)
        localDataSource.weatherData.value = null // Clear weather data

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<DashboardUiState>).data
        assertNull(uiData.data!!.latestWeather)
    }

    @Test
    fun `Pending sync records reflect PENDING sync status on dashboard`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.NORMAL_FARM, steps = 1)

        localDataSource.syncRecords.value = listOf(
            SyncRecord(
                id = "sr-1",
                entityType = "ManualPH",
                entityId = "mph-1",
                syncStatus = SyncStatus.PENDING,
                createdAtEpochMillis = 1000L
            )
        )

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<DashboardUiState>).data
        assertEquals(SyncStatus.PENDING, uiData.data!!.syncStatus)
    }
}
