package com.kshetrajna.app.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kshetrajna.app.AndroidTestLocalDataSource
import com.kshetrajna.app.AndroidTestRemoteDataSource
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultSyncRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.domain.model.ActuatorStatus
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.DashboardData
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.Node
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.model.WeatherData
import com.kshetrajna.app.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class FakeGetDashboardDataUseCase(
    private val dashboardData: DashboardData
) : GetDashboardDataUseCase(
    telemetryRepository = DummyRepository.telemetryRepo,
    weatherRepository = DummyRepository.weatherRepo,
    irrigationRepository = DummyRepository.irrigationRepo,
    safetyRepository = DummyRepository.safetyRepo,
    syncRepository = DummyRepository.syncRepo
) {
    override fun invoke(targetNodeId: String): Flow<Resource<DashboardData>> {
        return flowOf(Resource.Success(dashboardData))
    }
}

object DummyRepository {
    private val local = AndroidTestLocalDataSource()
    private val remote = AndroidTestRemoteDataSource()

    val telemetryRepo = DefaultTelemetryRepository(localDataSource = local)
    val weatherRepo = DefaultWeatherRepository(localDataSource = local)
    val irrigationRepo = DefaultIrrigationRepository(localDataSource = local)
    val safetyRepo = DefaultSafetyRepository(localDataSource = local)
    val syncRepo = DefaultSyncRepository(localDataSource = local, remoteDataSource = remote)
}

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreenRendersAllSectionsAndSafetyStatus() {
        val sampleData = DashboardData(
            node = Node(id = "n-1", farmId = "f-1", name = "Test Zone Node", isOnline = true),
            latestReading = SensorReading(id = "sr-1", nodeId = "n-1", timestampEpochMillis = 1000L, soilMoisturePercent = 38.5f),
            latestWeather = WeatherData(id = "w-1", farmId = "f-1", retrievedAtEpochMillis = 1000L, rainfallMm = 2.5f),
            irrigationState = IrrigationState(id = "st-1", nodeId = "n-1", timestampEpochMillis = 1000L, status = ActuatorStatus.STOPPED),
            latestCommand = IrrigationCommand(id = "c-1", nodeId = "n-1", commandType = IrrigationCommandType.START_IRRIGATION, lifecycleStatus = CommandLifecycleStatus.COMMAND_SENT, requestedAtEpochMillis = 1000L),
            safetyState = SafetyState(id = "saf-1", nodeId = "n-1", timestampEpochMillis = 1000L, status = SystemSafetyStatus.NORMAL),
            syncStatus = SyncStatus.SYNCED
        )

        val useCase = FakeGetDashboardDataUseCase(sampleData)
        val viewModel = DashboardViewModel(getDashboardDataUseCase = useCase)

        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("SAFETY STATUS: NORMAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Data Synchronization").assertIsDisplayed()
        composeTestRule.onNodeWithText("Soil Telemetry (Test Zone Node)").assertIsDisplayed()
        composeTestRule.onNodeWithText("MEASURED TELEMETRY").assertIsDisplayed()
        composeTestRule.onNodeWithText("38.5 %").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weather Context").assertIsDisplayed()
        composeTestRule.onNodeWithText("EXTERNAL FORECAST (CACHED)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Irrigation Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("PUMP STOPPED").assertIsDisplayed()
    }

    @Test
    fun dashboardScreenDisplaysSafetyLockoutAndActiveFaults() {
        val lockoutData = DashboardData(
            node = Node(id = "n-1", farmId = "f-1", name = "Test Zone Node", isOnline = true),
            safetyState = SafetyState(
                id = "saf-1",
                nodeId = "n-1",
                timestampEpochMillis = 1000L,
                status = SystemSafetyStatus.LOCKED,
                activeFaults = listOf(
                    SafetyFault(
                        type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                        message = "ACS712 Stray current fault active",
                        triggeredAtEpochMillis = 1000L
                    )
                )
            ),
            syncStatus = SyncStatus.PENDING
        )

        val useCase = FakeGetDashboardDataUseCase(lockoutData)
        val viewModel = DashboardViewModel(getDashboardDataUseCase = useCase)

        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("SAFETY STATUS: LOCKED OUT").assertIsDisplayed()
        composeTestRule.onNodeWithText("• ACS712 Stray current fault active").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending Offline Sync").assertIsDisplayed()
    }
}
