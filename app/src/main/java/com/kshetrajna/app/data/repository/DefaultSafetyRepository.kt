package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.repository.SafetyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Implementation of [SafetyRepository].
 */
class DefaultSafetyRepository(
    private val localDataSource: LocalDataSource,
) : SafetyRepository {

    override fun observeLatestSafetyState(nodeId: String): Flow<Resource<SafetyState?>> {
        return localDataSource.observeLatestSafetyState(nodeId).map { state ->
            Resource.Success(state) as Resource<SafetyState?>
        }.catch { e ->
            emit(Resource.Error("Failed to observe safety state: ${e.message}", e))
        }
    }

    override suspend fun saveSafetyState(state: SafetyState): Resource<Unit> {
        return try {
            localDataSource.insertSafetyState(state)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save safety state locally: ${e.message}", e)
        }
    }

    override fun observeAlerts(): Flow<Resource<List<Alert>>> {
        return localDataSource.observeAllAlerts().map { alerts ->
            Resource.Success(alerts) as Resource<List<Alert>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe alerts: ${e.message}", e))
        }
    }

    override suspend fun saveAlert(alert: Alert): Resource<Unit> {
        return try {
            localDataSource.insertAlert(alert)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save alert locally: ${e.message}", e)
        }
    }

    override suspend fun acknowledgeAlert(alert: Alert): Resource<Unit> {
        return try {
            val updatedAlert = alert.copy(isAcknowledged = true)
            localDataSource.insertAlert(updatedAlert)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to acknowledge alert locally: ${e.message}", e)
        }
    }
}
