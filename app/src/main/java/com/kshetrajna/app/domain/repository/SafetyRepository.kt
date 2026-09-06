package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.SafetyState
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for node safety status and alerts.
 */
interface SafetyRepository {
    fun observeLatestSafetyState(nodeId: String): Flow<Resource<SafetyState?>>
    suspend fun saveSafetyState(state: SafetyState): Resource<Unit>
    fun observeAlerts(): Flow<Resource<List<Alert>>>
    suspend fun saveAlert(alert: Alert): Resource<Unit>
    suspend fun acknowledgeAlert(alert: Alert): Resource<Unit>
}
