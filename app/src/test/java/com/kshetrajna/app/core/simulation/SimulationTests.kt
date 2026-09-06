package com.kshetrajna.app.core.simulation

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultFarmRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.Farm
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationTests {

    @Test
    fun `SimulationEngine is 100 percent deterministic across identical runs`() {
        val clock1 = SimulationClock(1000000L)
        val engine1 = SimulationEngine(clock1, seed = 42L)

        val clock2 = SimulationClock(1000000L)
        val engine2 = SimulationEngine(clock2, seed = 42L)

        val reading1 = engine1.generateSensorReading("node-1", SimulationScenario.DRY_SOIL, step = 2)
        val reading2 = engine2.generateSensorReading("node-1", SimulationScenario.DRY_SOIL, step = 2)

        assertEquals(reading1, reading2)
        assertEquals(25.0f, reading1.soilMoisturePercent!!, 0.001f)
    }

    @Test
    fun `SimulationClock time manipulation is controllable and deterministic`() {
        val clock = SimulationClock(1000L)
        assertEquals(1000L, clock.currentTimeEpochMillis())

        clock.advanceTimeBy(5000L)
        assertEquals(6000L, clock.currentTimeEpochMillis())

        clock.setTime(20000L)
        assertEquals(20000L, clock.currentTimeEpochMillis())
    }

    @Test
    fun `NORMAL_FARM scenario generates optimal telemetry, online node, and normal safety`() = runTest {
        val clock = SimulationClock(10000L)
        val engine = SimulationEngine(clock)

        val node = engine.generateNode(SimulationScenario.NORMAL_FARM)
        val reading = engine.generateSensorReading(node.id, SimulationScenario.NORMAL_FARM)
        val safety = engine.generateSafetyState(node.id, SimulationScenario.NORMAL_FARM)

        assertTrue(node.isOnline)
        assertEquals(35.0f, reading.soilMoisturePercent!!, 0.001f)
        assertEquals(SystemSafetyStatus.NORMAL, safety.status)
        assertFalse(safety.isLockedOut)
    }

    @Test
    fun `DRY_SOIL scenario deterministically decreases soil moisture over steps`() = runTest {
        val engine = SimulationEngine()

        val readingStep0 = engine.generateSensorReading("n-1", SimulationScenario.DRY_SOIL, step = 0)
        val readingStep2 = engine.generateSensorReading("n-1", SimulationScenario.DRY_SOIL, step = 2)
        val readingStep5 = engine.generateSensorReading("n-1", SimulationScenario.DRY_SOIL, step = 5)

        assertEquals(35.0f, readingStep0.soilMoisturePercent!!, 0.001f)
        assertEquals(25.0f, readingStep2.soilMoisturePercent!!, 0.001f)
        assertEquals(10.0f, readingStep5.soilMoisturePercent!!, 0.001f)
        assertTrue(readingStep5.soilMoisturePercent!! < readingStep0.soilMoisturePercent)
    }

    @Test
    fun `RAIN_EVENT scenario generates active rainfall and high humidity`() = runTest {
        val engine = SimulationEngine()

        val weatherStep1 = engine.generateWeatherData("f-1", SimulationScenario.RAIN_EVENT, step = 1)
        val readingStep1 = engine.generateSensorReading("n-1", SimulationScenario.RAIN_EVENT, step = 1)

        assertEquals(20.0f, weatherStep1.rainfallMm!!, 0.001f)
        assertEquals(92.0f, weatherStep1.humidityPercent!!, 0.001f)
        assertEquals(44.0f, readingStep1.soilMoisturePercent!!, 0.001f)
    }

    @Test
    fun `HIGH_EC_WARNING scenario generates elevated soil EC and WARNING safety status`() = runTest {
        val engine = SimulationEngine()

        val reading = engine.generateSensorReading("n-1", SimulationScenario.HIGH_EC_WARNING, step = 1)
        val safety = engine.generateSafetyState("n-1", SimulationScenario.HIGH_EC_WARNING, step = 1)

        assertEquals(5.0f, reading.soilEcDsPerM!!, 0.001f)
        assertEquals(SystemSafetyStatus.WARNING, safety.status)
    }

    @Test
    fun `SAFETY_LOCKOUT scenario generates LOCKED status, active fault, and rejected command`() = runTest {
        val engine = SimulationEngine()

        val safety = engine.generateSafetyState("n-1", SimulationScenario.SAFETY_LOCKOUT)
        val command = engine.generateIrrigationCommand("n-1", SimulationScenario.SAFETY_LOCKOUT)
        val alert = engine.generateAlert("n-1", SimulationScenario.SAFETY_LOCKOUT)

        assertEquals(SystemSafetyStatus.LOCKED, safety.status)
        assertTrue(safety.isLockedOut)
        assertEquals(1, safety.activeFaults.size)

        assertEquals(CommandLifecycleStatus.COMMAND_REJECTED, command.lifecycleStatus)
        assertNotNull(command.rejectionReason)

        assertNotNull(alert)
    }

    @Test
    fun `NODE_OFFLINE scenario generates offline node status`() = runTest {
        val engine = SimulationEngine()

        val node = engine.generateNode(SimulationScenario.NODE_OFFLINE, step = 1)

        assertFalse(node.isOnline)
        assertTrue(node.lastSeenEpochMillis!! < engine.clock.currentTimeEpochMillis())
    }

    @Test
    fun `IRRIGATION_COMMAND_LIFECYCLE scenario models command progression distinct from actuator state`() = runTest {
        val engine = SimulationEngine()

        val cmdStep0 = engine.generateIrrigationCommand("n-1", SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE, step = 0)
        val stateStep0 = engine.generateIrrigationState("n-1", SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE, step = 0)

        assertEquals(CommandLifecycleStatus.COMMAND_REQUESTED, cmdStep0.lifecycleStatus)
        assertEquals(ActuatorStatus.STOPPED, stateStep0.status) // Pump NOT running on request!

        val cmdStep3 = engine.generateIrrigationCommand("n-1", SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE, step = 3)
        val stateStep3 = engine.generateIrrigationState("n-1", SimulationScenario.IRRIGATION_COMMAND_LIFECYCLE, step = 3)

        assertEquals(CommandLifecycleStatus.ACTUATOR_RUNNING, cmdStep3.lifecycleStatus)
        assertEquals(ActuatorStatus.RUNNING, stateStep3.status)
        assertEquals(8.5f, stateStep3.activeFlowRateLpm!!, 0.001f)
    }

    @Test
    fun `SimulationManager seeds scenario into LocalDataSource and streams via production repositories`() = runTest {
        val clock = SimulationClock(50000L)
        val manager = SimulationManager(clock = clock)
        val localDataSource = InMemoryLocalDataSource()

        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val farmRepo = DefaultFarmRepository(localDataSource)

        // Seed DRY_SOIL scenario into local data source
        manager.setScenario(SimulationScenario.DRY_SOIL, localDataSource)

        val farms = farmRepo.observeFarms().first()
        assertTrue(farms is Resource.Success<*>)
        assertEquals("sim_farm_01", (farms as Resource.Success<List<Farm>>).data.first().id)

        val readings = telemetryRepo.observeReadingsForNode("sim_node_01").first()
        assertTrue(readings is Resource.Success<*>)
        assertEquals(35.0f, (readings as Resource.Success<List<SensorReading>>).data.first().soilMoisturePercent!!, 0.001f)

        // Advance simulation step
        manager.advanceStep(localDataSource)

        val updatedReadings = telemetryRepo.observeReadingsForNode("sim_node_01").first()
        assertTrue(updatedReadings is Resource.Success<*>)
        assertEquals(30.0f, (updatedReadings as Resource.Success<List<SensorReading>>).data.last().soilMoisturePercent!!, 0.001f)
    }
}
