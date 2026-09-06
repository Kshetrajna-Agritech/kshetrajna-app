package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.CropProfile
import com.kshetrajna.app.domain.model.Farm
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryTests {

    @Test
    fun `ManualPhRepository records manual pH locally and queues PENDING SyncRecord`() = runTest {
        val local = InMemoryLocalDataSource()
        val repo = DefaultManualPhRepository(local)

        val entry = ManualPH(
            id = "mph-100",
            nodeId = "node-1",
            timestampEpochMillis = 5000L,
            phValue = 6.8f,
            notes = "Local pH entry test"
        )

        val result = repo.recordManualPh(entry)
        assertTrue(result is Resource.Success<*>)

        val storedPhsRes = repo.observeManualPhForNode("node-1").first()
        assertTrue(storedPhsRes is Resource.Success<*>)
        val phList = (storedPhsRes as Resource.Success<List<ManualPH>>).data
        assertEquals(1, phList.size)
        assertEquals(6.8f, phList.first().phValue, 0.001f)
        assertEquals(SyncStatus.PENDING, phList.first().syncStatus)

        assertEquals(1, local.syncRecords.value.size)
        val syncRecord = local.syncRecords.value.first()
        assertEquals("mph-100", syncRecord.entityId)
        assertEquals("ManualPH", syncRecord.entityType)
        assertEquals(SyncStatus.PENDING, syncRecord.syncStatus)
    }

    @Test
    fun `TelemetryRepository observes and stores nodes and readings`() = runTest {
        val local = InMemoryLocalDataSource()
        val repo = DefaultTelemetryRepository(local)

        val node = Node(id = "n-1", farmId = "f-1", name = "North Node", isOnline = true)
        local.insertNode(node)

        val reading = SensorReading(
            id = "sr-1",
            nodeId = "n-1",
            timestampEpochMillis = 10000L,
            soilMoisturePercent = 40.0f
        )
        val saveResult = repo.saveSensorReading(reading)
        assertTrue(saveResult is Resource.Success<*>)

        val nodesResult = repo.observeNodes("f-1").first()
        assertTrue(nodesResult is Resource.Success<*>)
        assertEquals(1, (nodesResult as Resource.Success<List<Node>>).data.size)

        val readingsResult = repo.observeReadingsForNode("n-1").first()
        assertTrue(readingsResult is Resource.Success<*>)
        val readingsList = (readingsResult as Resource.Success<List<SensorReading>>).data
        assertEquals(1, readingsList.size)
        assertEquals(40.0f, readingsList.first().soilMoisturePercent!!, 0.001f)
    }

    @Test
    fun `IrrigationRepository stores command without claiming pump is running`() = runTest {
        val local = InMemoryLocalDataSource()
        val repo = DefaultIrrigationRepository(local)

        val command = IrrigationCommand(
            id = "cmd-1",
            nodeId = "n-1",
            commandType = IrrigationCommandType.START_IRRIGATION,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_REQUESTED,
            requestedAtEpochMillis = 12000L
        )

        val result = repo.sendIrrigationCommand(command)
        assertTrue(result is Resource.Success<*>)

        val commandsResult = repo.observeCommandsForNode("n-1").first()
        assertTrue(commandsResult is Resource.Success<*>)
        val storedCommand = (commandsResult as Resource.Success<List<IrrigationCommand>>).data.first()
        assertEquals(CommandLifecycleStatus.COMMAND_REQUESTED, storedCommand.lifecycleStatus)

        val stateResult = repo.observeLatestIrrigationState("n-1").first()
        assertTrue(stateResult is Resource.Success<*>)
        assertEquals(null, (stateResult as Resource.Success).data)
    }

    @Test
    fun `SafetyRepository stores safety status and manages alert acknowledgement`() = runTest {
        val local = InMemoryLocalDataSource()
        val repo = DefaultSafetyRepository(local)

        val fault = SafetyFault(type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT, message = "Fault test", triggeredAtEpochMillis = 1000L)
        val safetyState = SafetyState(
            id = "s-1",
            nodeId = "n-1",
            timestampEpochMillis = 10000L,
            status = SystemSafetyStatus.FAULT,
            activeFaults = listOf(fault)
        )
        repo.saveSafetyState(safetyState)

        val alert = Alert(
            id = "a-1",
            nodeId = "n-1",
            timestampEpochMillis = 10000L,
            severity = AlertSeverity.CRITICAL,
            category = MeasurementCategory.SAFETY,
            title = "Fault Alert",
            message = "Safety fault triggered",
            isAcknowledged = false
        )
        repo.saveAlert(alert)

        val observedState = repo.observeLatestSafetyState("n-1").first()
        assertTrue((observedState as Resource.Success<SafetyState?>).data!!.isLockedOut)

        val initialAlerts = repo.observeAlerts().first()
        assertFalse((initialAlerts as Resource.Success<List<Alert>>).data.first().isAcknowledged)

        repo.acknowledgeAlert(alert)
        val updatedAlerts = repo.observeAlerts().first()
        assertTrue((updatedAlerts as Resource.Success<List<Alert>>).data.first().isAcknowledged)
    }

    @Test
    fun `WeatherRepository stores and retrieves cached weather data`() = runTest {
        val local = InMemoryLocalDataSource()
        val repo = DefaultWeatherRepository(local)

        val weather = WeatherData(
            id = "w-1",
            farmId = "f-1",
            retrievedAtEpochMillis = 15000L,
            rainfallMm = 5.0f,
            isCached = true
        )
        repo.saveWeatherData(weather)

        val result = repo.observeLatestWeather().first()
        assertTrue(result is Resource.Success<*>)
        assertEquals(5.0f, (result as Resource.Success<WeatherData?>).data!!.rainfallMm!!, 0.001f)
    }

    @Test
    fun `SyncRepository reflects PENDING state when records exist and propagates sync failure`() = runTest {
        val local = InMemoryLocalDataSource()
        val remote = FakeRemoteDataSource(shouldFail = true)
        val repo = DefaultSyncRepository(local, remote)

        val initialSyncStatus = repo.observeSyncStatus().first()
        assertEquals(SyncStatus.SYNCED, (initialSyncStatus as Resource.Success<SyncStatus>).data)

        local.syncRecords.value = listOf(
            SyncRecord(
                id = "sr-1",
                entityType = "ManualPH",
                entityId = "mph-1",
                syncStatus = SyncStatus.PENDING,
                createdAtEpochMillis = 1000L
            )
        )

        val pendingSyncStatus = repo.observeSyncStatus().first()
        assertEquals(SyncStatus.PENDING, (pendingSyncStatus as Resource.Success<SyncStatus>).data)

        val syncResult = repo.triggerSync()
        assertTrue(syncResult is Resource.Error)
        assertEquals("Network unreachable", (syncResult as Resource.Error).message)
    }

    @Test
    fun `FarmRepository stores and retrieves farms and crop profiles`() = runTest {
        val local = InMemoryLocalDataSource()
        val repo = DefaultFarmRepository(local)

        val farm = Farm(id = "f-1", name = "Test Farm", createdAtEpochMillis = 1000L)
        val profile = CropProfile(id = "cp-1", cropName = "Tomato")

        repo.saveFarm(farm)
        repo.saveCropProfile(profile)

        val farmsResult = repo.observeFarms().first()
        val profilesResult = repo.observeCropProfiles().first()

        assertEquals("Test Farm", (farmsResult as Resource.Success<List<Farm>>).data.first().name)
        assertEquals("Tomato", (profilesResult as Resource.Success<List<CropProfile>>).data.first().cropName)
    }
}
