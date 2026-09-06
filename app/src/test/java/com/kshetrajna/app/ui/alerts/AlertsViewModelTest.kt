package com.kshetrajna.app.ui.alerts

import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SystemSafetyStatus
import com.kshetrajna.app.domain.usecase.AcknowledgeAlertUseCase
import com.kshetrajna.app.domain.usecase.GetAlertsAndSafetyUseCase
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
class AlertsViewModelTest {

    private fun createViewModel(
        localDataSource: InMemoryLocalDataSource,
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): AlertsViewModel {
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)

        val getAlertsAndSafetyUseCase = GetAlertsAndSafetyUseCase(
            safetyRepository = safetyRepo,
            irrigationRepository = irrigationRepo
        )
        val acknowledgeAlertUseCase = AcknowledgeAlertUseCase(
            safetyRepository = safetyRepo
        )

        return AlertsViewModel(
            getAlertsAndSafetyUseCase = getAlertsAndSafetyUseCase,
            acknowledgeAlertUseCase = acknowledgeAlertUseCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `viewModel loads safety state and separates active alerts from alert history`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val safetyLocked = SafetyState(
            id = "saf-locked",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.LOCKED,
            activeFaults = listOf(
                SafetyFault(
                    type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                    message = "Leakage current",
                    triggeredAtEpochMillis = 1000L
                )
            )
        )
        localDataSource.safetyStates.value = listOf(safetyLocked)

        val alert1 = Alert(
            id = "a-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            severity = AlertSeverity.CRITICAL,
            category = MeasurementCategory.SAFETY,
            title = "Stray Current",
            message = "Leakage current detected",
            isAcknowledged = false
        )
        val alert2 = Alert(
            id = "a-2",
            nodeId = "sim_node_01",
            timestampEpochMillis = 500L,
            severity = AlertSeverity.INFO,
            category = MeasurementCategory.SAFETY,
            title = "System Restored",
            message = "System back online",
            isAcknowledged = true
        )
        localDataSource.alerts.value = listOf(alert1, alert2)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Success)

        val uiData = (uiState as UiState.Success<AlertsUiStateData>).data.data
        assertEquals(SystemSafetyStatus.LOCKED, uiData.safetyState?.status)
        assertEquals(1, uiData.activeAlerts.size)
        assertEquals("a-1", uiData.activeAlerts.first().id)
        assertEquals(2, uiData.alertHistory.size)
    }

    @Test
    fun `acknowledging alert calls use case and updates action message`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val alert = Alert(
            id = "a-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            severity = AlertSeverity.WARNING,
            category = MeasurementCategory.SAFETY,
            title = "Thermal Warning",
            message = "High soil temp",
            isAcknowledged = false
        )
        localDataSource.alerts.value = listOf(alert)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        viewModel.acknowledgeAlert(alert)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState is UiState.Success)

        val uiData = (uiState as UiState.Success<AlertsUiStateData>).data
        assertNotNull(uiData.actionMessage)
        assertTrue(uiData.actionMessage!!.contains("acknowledged"))
        assertNull(uiData.errorMessage)

        // Verify stored alert in local data source is now acknowledged
        assertTrue(localDataSource.alerts.value.first().isAcknowledged)
    }
}
