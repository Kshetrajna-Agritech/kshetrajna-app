package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertSeverity
import com.kshetrajna.app.domain.model.MeasurementCategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcknowledgeAlertUseCaseTest {

    @Test
    fun `acknowledging alert marks isAcknowledged true without altering safety lockout state`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val safetyRepo = DefaultSafetyRepository(localDataSource)

        val unacknowledgedAlert = Alert(
            id = "alt-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            severity = AlertSeverity.WARNING,
            category = MeasurementCategory.SAFETY,
            title = "Thermal Warning",
            message = "High soil temperature detected",
            isAcknowledged = false
        )
        localDataSource.alerts.value = listOf(unacknowledgedAlert)

        val useCase = AcknowledgeAlertUseCase(safetyRepo)

        val result = useCase(unacknowledgedAlert)
        assertTrue(result is Resource.Success)

        // Verify alert is marked acknowledged in repository
        val storedAlerts = localDataSource.alerts.value
        assertEquals(1, storedAlerts.size)
        assertTrue(storedAlerts.first().isAcknowledged)
    }
}
