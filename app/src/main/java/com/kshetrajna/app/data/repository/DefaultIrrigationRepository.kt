package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationState
import com.kshetrajna.app.domain.repository.IrrigationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Implementation of [IrrigationRepository].
 * Preserves strict separation between command requests and physical actuator state.
 */
class DefaultIrrigationRepository(
    private val localDataSource: LocalDataSource,
) : IrrigationRepository {

    override fun observeLatestIrrigationState(nodeId: String): Flow<Resource<IrrigationState?>> {
        return localDataSource.observeLatestIrrigationState(nodeId).map { state ->
            Resource.Success(state) as Resource<IrrigationState?>
        }.catch { e ->
            emit(Resource.Error("Failed to observe irrigation state: ${e.message}", e))
        }
    }

    override suspend fun saveIrrigationState(state: IrrigationState): Resource<Unit> {
        return try {
            localDataSource.insertIrrigationState(state)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save irrigation state locally: ${e.message}", e)
        }
    }

    override fun observeCommandsForNode(nodeId: String): Flow<Resource<List<IrrigationCommand>>> {
        return localDataSource.observeCommandsForNode(nodeId).map { commands ->
            Resource.Success(commands) as Resource<List<IrrigationCommand>>
        }.catch { e ->
            emit(Resource.Error("Failed to observe irrigation commands: ${e.message}", e))
        }
    }

    override suspend fun sendIrrigationCommand(command: IrrigationCommand): Resource<Unit> {
        return try {
            // Ensure command lifecycle status reflects command transmission/request, NOT physical actuation
            val pendingCommand = if (command.lifecycleStatus == CommandLifecycleStatus.ACTUATOR_RUNNING) {
                command.copy(lifecycleStatus = CommandLifecycleStatus.COMMAND_SENT)
            } else {
                command
            }
            localDataSource.insertIrrigationCommand(pendingCommand)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to record irrigation command request locally: ${e.message}", e)
        }
    }
}
