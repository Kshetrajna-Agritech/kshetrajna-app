package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.AlertsAndSafetyData
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.repository.IrrigationRepository
import com.kshetrajna.app.domain.repository.SafetyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Domain use case aggregating safety state, active alerts, alert history, and related irrigation state.
 */
open class GetAlertsAndSafetyUseCase(
    private val safetyRepository: SafetyRepository,
    private val irrigationRepository: IrrigationRepository,
) {
    @Suppress("UNCHECKED_CAST")
    open operator fun invoke(nodeId: String = "sim_node_01"): Flow<Resource<AlertsAndSafetyData>> {
        return combine(
            safetyRepository.observeLatestSafetyState(nodeId),
            safetyRepository.observeAlerts(),
            irrigationRepository.observeLatestIrrigationState(nodeId)
        ) { flows ->
            val safetyRes = flows[0] as Resource<SafetyState?>
            val alertsRes = flows[1] as Resource<List<Alert>>
            val irrigationRes = flows[2] as Resource<IrrigationState?>

            val safetyState = (safetyRes as? Resource.Success)?.data
            val alerts = (alertsRes as? Resource.Success)?.data ?: emptyList()
            val sortedAlerts = alerts.sortedByDescending { it.timestampEpochMillis }
            val activeAlerts = sortedAlerts.filter { !it.isAcknowledged }
            val irrigationState = (irrigationRes as? Resource.Success)?.data

            val data = AlertsAndSafetyData(
                nodeId = nodeId,
                safetyState = safetyState,
                activeAlerts = activeAlerts,
                alertHistory = sortedAlerts,
                latestIrrigationState = irrigationState
            )

            Resource.Success(data)
        }
    }
}
