package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetIrrigationDataUseCaseTest {

    @Test
    fun `irrigation data loads command history, actuator state, and safety interlocks correctly`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val weatherRepo = DefaultWeatherRepository(localDataSource)

        val command1 = IrrigationCommand(
            id = "cmd-1",
            nodeId = "sim_node_01",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_REQUESTED,
            requestedAtEpochMillis = 1000L
        )
        val command2 = IrrigationCommand(
            id = "cmd-2",
            nodeId = "sim_node_01",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_ACCEPTED,
            requestedAtEpochMillis = 2000L
        )
        localDataSource.irrigationCommands.value = listOf(command1, command2)

        val state = IrrigationState(
            id = "st-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 2100L,
            status = ActuatorStatus.STOPPED,
            activeFlowRateLpm = 0.0f
        )
        localDataSource.irrigationStates.value = listOf(state)

        val safety = SafetyState(
            id = "saf-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 2100L,
            status = SystemSafetyStatus.NORMAL
        )
        localDataSource.safetyStates.value = listOf(safety)

        val useCase = GetIrrigationDataUseCase(
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            telemetryRepository = telemetryRepo,
            weatherRepository = weatherRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val data = (resource as Resource.Success).data
        assertEquals(2, data.commandHistory.size)
        // Verify chronological sorting (newest first)
        assertEquals("cmd-2", data.commandHistory[0].id)
        assertEquals(CommandLifecycleStatus.COMMAND_ACCEPTED, data.latestCommand?.lifecycleStatus)

        // Verify COMMAND_ACCEPTED != ACTUATOR_RUNNING invariant
        assertEquals(ActuatorStatus.STOPPED, data.latestIrrigationState?.status)

        assertNotNull(data.safetyState)
        assertFalse(data.safetyState!!.isLockedOut)
    }

    @Test
    fun `weather and soil telemetry are loaded for informational context only without altering irrigation decisions`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val weatherRepo = DefaultWeatherRepository(localDataSource)

        val reading = SensorReading(
            id = "sr-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            soilMoisturePercent = 15.0f // Very dry soil
        )
        localDataSource.readings.value = listOf(reading)

        val weather = WeatherData(
            id = "w-1",
            farmId = "sim_farm_01",
            retrievedAtEpochMillis = 1000L,
            rainfallMm = 50.0f // Heavy rain forecast
        )
        localDataSource.weatherData.value = weather

        val useCase = GetIrrigationDataUseCase(
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            telemetryRepository = telemetryRepo,
            weatherRepository = weatherRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val data = (resource as Resource.Success).data
        assertEquals(15.0f, data.latestReading?.soilMoisturePercent ?: 0f, 0.001f)
        assertEquals(50.0f, data.latestWeather?.rainfallMm ?: 0f, 0.001f)

        // Verify that weather and moisture context do NOT invent automatic commands
        assertTrue(data.commandHistory.isEmpty())
        assertNull(data.latestCommand)
    }
}
