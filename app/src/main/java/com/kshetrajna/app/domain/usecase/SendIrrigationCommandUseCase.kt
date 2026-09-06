package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.CommandLifecycleStatus
import com.kshetrajna.app.domain.model.IrrigationCommand
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.model.SafetyState
import com.kshetrajna.app.domain.model.SyncRecord
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.IrrigationRepository
import com.kshetrajna.app.domain.repository.SafetyRepository
import com.kshetrajna.app.domain.repository.SyncRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

/**
 * Domain use case handling irrigation command requests.
 * Evaluates hardware safety interlocks, records lifecycle status, and manages local offline persistence.
 */
open class SendIrrigationCommandUseCase(
    private val irrigationRepository: IrrigationRepository,
    private val safetyRepository: SafetyRepository,
    private val syncRepository: SyncRepository,
) {
    open suspend operator fun invoke(
        nodeId: String,
        commandType: IrrigationCommandType,
        timestampEpochMillis: Long = System.currentTimeMillis(),
    ): Resource<Unit> {
        // 1. Fetch latest safety state to enforce hardware safety authority
        val safetyRes = safetyRepository.observeLatestSafetyState(nodeId).firstOrNull()
        val safetyState: SafetyState? = (safetyRes as? Resource.Success)?.data

        // 2. Evaluate safety interlocks before command creation
        if (safetyState != null && safetyState.isLockedOut) {
            val faultMsg = safetyState.activeFaults.firstOrNull()?.message ?: "Hardware safety interlock active"
            val rejectionReason = "Command blocked by safety interlock (${safetyState.status}): $faultMsg"

            val rejectedCommand = IrrigationCommand(
                id = "cmd_${timestampEpochMillis}_${UUID.randomUUID().toString().take(8)}",
                nodeId = nodeId,
                commandType = commandType,
                lifecycleStatus = CommandLifecycleStatus.COMMAND_REJECTED,
                requestedAtEpochMillis = timestampEpochMillis,
                respondedAtEpochMillis = timestampEpochMillis,
                rejectionReason = rejectionReason
            )

            // Record rejected command locally
            irrigationRepository.sendIrrigationCommand(rejectedCommand)

            return Resource.Error(rejectionReason)
        }

        // 3. Create requested command if safety interlocks permit
        val commandId = "cmd_${timestampEpochMillis}_${UUID.randomUUID().toString().take(8)}"
        val requestedCommand = IrrigationCommand(
            id = commandId,
            nodeId = nodeId,
            commandType = commandType,
            lifecycleStatus = CommandLifecycleStatus.COMMAND_REQUESTED,
            requestedAtEpochMillis = timestampEpochMillis
        )

        val sendResult = irrigationRepository.sendIrrigationCommand(requestedCommand)
        if (sendResult is Resource.Error) {
            return sendResult
        }

        // 4. Queue SyncRecord for asynchronous sync
        val syncRecord = SyncRecord(
            id = "sync_${timestampEpochMillis}_${UUID.randomUUID().toString().take(8)}",
            entityType = "IrrigationCommand",
            entityId = commandId,
            syncStatus = SyncStatus.PENDING,
            createdAtEpochMillis = timestampEpochMillis
        )
        syncRepository.saveSyncRecord(syncRecord)

        return Resource.Success(Unit)
    }
}
