package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationState
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for irrigation state and command tracking.
 */
interface IrrigationRepository {
    fun observeLatestIrrigationState(nodeId: String): Flow<Resource<IrrigationState?>>
    suspend fun saveIrrigationState(state: IrrigationState): Resource<Unit>
    fun observeCommandsForNode(nodeId: String): Flow<Resource<List<IrrigationCommand>>>
    suspend fun sendIrrigationCommand(command: IrrigationCommand): Resource<Unit>
}
