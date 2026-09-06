package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAlertsAndSafetyUseCaseTest {

    @Test
    fun `safety state and active alerts load correctly preserving severity and provenance`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)

        val safety = SafetyState(
            id = "saf-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            status = SystemSafetyStatus.LOCKED,
            activeFaults = listOf(
                SafetyFault(
                    type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                    message = "Leakage current detected",
                    triggeredAtEpochMillis = 1000L
                )
            )
        )
        localDataSource.safetyStates.value = listOf(safety)

        val alert1 = Alert(
            id = "alt-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            severity = AlertSeverity.CRITICAL,
            category = MeasurementCategory.SAFETY,
            title = "Stray Current Fault",
            message = "Soil leakage current exceeds threshold",
            isAcknowledged = false
        )
        val alert2 = Alert(
            id = "alt-2",
            nodeId = "sim_node_01",
            timestampEpochMillis = 2000L,
            severity = AlertSeverity.INFO,
            category = MeasurementCategory.MEASURED,
            title = "Routine Check",
            message = "Telemetry update received",
            isAcknowledged = true
        )
        localDataSource.alerts.value = listOf(alert1, alert2)

        val useCase = GetAlertsAndSafetyUseCase(
            safetyRepository = safetyRepo,
            irrigationRepository = irrigationRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val data = (resource as Resource.Success).data
        assertNotNull(data.safetyState)
        assertEquals(SystemSafetyStatus.LOCKED, data.safetyState?.status)
        assertTrue(data.safetyState?.isLockedOut == true)

        // Verify active alerts (unacknowledged only)
        assertEquals(1, data.activeAlerts.size)
        assertEquals("alt-1", data.activeAlerts.first().id)
        assertEquals(AlertSeverity.CRITICAL, data.activeAlerts.first().severity)

        // Verify full alert history (sorted chronologically newest first)
        assertEquals(2, data.alertHistory.size)
        assertEquals("alt-2", data.alertHistory[0].id)
        assertEquals("alt-1", data.alertHistory[1].id)
    }

    @Test
    fun `no safety data state is represented as null and distinct from NORMAL`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)

        localDataSource.safetyStates.value = emptyList()
        localDataSource.alerts.value = emptyList()

        val useCase = GetAlertsAndSafetyUseCase(
            safetyRepository = safetyRepo,
            irrigationRepository = irrigationRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val data = (resource as Resource.Success).data
        assertNull(data.safetyState) // NO SAFETY DATA != SAFETY NORMAL
        assertTrue(data.activeAlerts.isEmpty())
        assertTrue(data.alertHistory.isEmpty())
    }
}
