package com.kshetrajna.app.ui.soil

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.simulation.SimulationClock
import com.kshetrajna.app.core.simulation.SimulationEngine
import com.kshetrajna.app.core.simulation.SimulationScenario
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.data.simulation.SimulatedDataSourceSeeder
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SensorSource
import com.kshetrajna.app.domain.model.SoilAnalysis
import com.kshetrajna.app.domain.repository.TelemetryRepository
import com.kshetrajna.app.domain.usecase.GetSoilTelemetryUseCase
import com.kshetrajna.app.ui.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SoilViewModelTest {

    private fun createViewModel(
        telemetryRepository: TelemetryRepository,
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): SoilViewModel {
        val useCase = GetSoilTelemetryUseCase(telemetryRepository = telemetryRepository)
        return SoilViewModel(
            getSoilTelemetryUseCase = useCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `loadSoilTelemetry populates Success state with chronological history and latest reading`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.NORMAL_FARM, steps = 1)

        val repository = DefaultTelemetryRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<SoilUiStateData>).data
        val data = uiData.telemetryData

        assertNotNull(data.node)
        assertEquals("sim_node_01", data.node?.id)
        assertNotNull(data.latestReading)
        assertEquals(35.0f, data.latestReading!!.soilMoisturePercent!!, 0.001f)
        assertFalse(uiData.isOfflineNode)

        // Verify chronological order (oldest to newest)
        val history = data.historyReadings
        assertTrue(history.isNotEmpty())
        for (i in 0 until history.size - 1) {
            assertTrue(history[i].timestampEpochMillis <= history[i + 1].timestampEpochMillis)
        }
    }

    @Test
    fun `Empty telemetry history results in UiState Empty`() = runTest {
        val localDataSource = InMemoryLocalDataSource() // Empty local source
        val repository = DefaultTelemetryRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Empty but got $state", state is UiState.Empty)
    }

    @Test
    fun `NODE_OFFLINE scenario marks isOfflineNode true in SoilUiStateData`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val seeder = SimulatedDataSourceSeeder(SimulationEngine(SimulationClock(1000L)))
        seeder.seedScenario(localDataSource, SimulationScenario.NODE_OFFLINE, steps = 1)

        val repository = DefaultTelemetryRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<SoilUiStateData>).data
        assertTrue(uiData.isOfflineNode)
        assertFalse(uiData.telemetryData.node!!.isOnline)
    }

    @Test
    fun `Repository error triggers UiState Error`() = runTest {
        val failingRepo = object : TelemetryRepository {
            override fun observeNodes(farmId: String?): Flow<Resource<List<Node>>> {
                return flowOf(Resource.Error("Database disk read error"))
            }

            override fun observeReadingsForNode(nodeId: String): Flow<Resource<List<SensorReading>>> {
                return flowOf(Resource.Error("Database disk read error"))
            }

            override suspend fun saveSensorReading(reading: SensorReading): Resource<Unit> {
                return Resource.Error("Error")
            }

            override fun observeSoilAnalyses(nodeId: String): Flow<Resource<List<SoilAnalysis>>> {
                return flowOf(Resource.Error("Error"))
            }

            override suspend fun saveSoilAnalysis(analysis: SoilAnalysis): Resource<Unit> {
                return Resource.Error("Error")
            }
        }

        val viewModel = createViewModel(failingRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Database disk read error", (state as UiState.Error).message)
    }

    @Test
    fun `Simulated data provenance remains visibly simulated`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val reading = SensorReading(
            id = "sr-sim-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            soilMoisturePercent = 42.0f,
            soilTemperatureCelsius = 22.5f,
            soilEcDsPerM = 1.1f,
            source = SensorSource.LOCAL_SIMULATION
        )
        localDataSource.readings.value = listOf(reading)
        localDataSource.nodes.value = listOf(
            Node(id = "sim_node_01", farmId = "f-1", name = "Demo Zone Node", isOnline = true)
        )

        val repository = DefaultTelemetryRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<SoilUiStateData>).data
        assertEquals(SensorSource.LOCAL_SIMULATION, uiData.telemetryData.latestReading!!.source)
    }

    @Test
    fun `Missing telemetry metrics handle null fields cleanly`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val incompleteReading = SensorReading(
            id = "sr-incomplete",
            nodeId = "sim_node_01",
            timestampEpochMillis = 2000L,
            soilMoisturePercent = null,
            soilTemperatureCelsius = null,
            soilEcDsPerM = null
        )
        localDataSource.readings.value = listOf(incompleteReading)
        localDataSource.nodes.value = listOf(
            Node(id = "sim_node_01", farmId = "f-1", name = "Test Node", isOnline = true)
        )

        val repository = DefaultTelemetryRepository(localDataSource)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<SoilUiStateData>).data
        val latest = uiData.telemetryData.latestReading
        assertNotNull(latest)
        assertNull(latest!!.soilMoisturePercent)
        assertNull(latest.soilTemperatureCelsius)
        assertNull(latest.soilEcDsPerM)
    }
}
