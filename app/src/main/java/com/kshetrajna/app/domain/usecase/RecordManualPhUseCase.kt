package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.ManualPhRepository
import java.util.UUID

/**
 * Domain use case validating and recording a manual pH measurement entry.
 * Enforces exact domain boundaries [0.0, 14.0] and preserves MANUAL provenance semantics.
 */
open class RecordManualPhUseCase(
    private val manualPhRepository: ManualPhRepository,
) {
    open suspend operator fun invoke(
        nodeId: String,
        phInput: String,
        notes: String? = null,
        enteredByUserId: String? = null,
        timestampEpochMillis: Long = System.currentTimeMillis(),
    ): Resource<Unit> {
        val trimmedInput = phInput.trim()
        if (trimmedInput.isEmpty()) {
            return Resource.Error("pH value cannot be empty.")
        }

        val parsedValue = trimmedInput.toFloatOrNull()
            ?: return Resource.Error("Please enter a valid numeric pH value.")

        if (parsedValue !in 0.0f..14.0f) {
            return Resource.Error("pH value must be within valid physical range [0.0, 14.0].")
        }

        val entry = try {
            ManualPH(
                id = "mph_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}",
                nodeId = nodeId,
                timestampEpochMillis = timestampEpochMillis,
                phValue = parsedValue,
                notes = notes?.trim()?.ifEmpty { null },
                enteredByUserId = enteredByUserId,
                syncStatus = SyncStatus.PENDING,
                category = MeasurementCategory.MANUAL
            )
        } catch (e: IllegalArgumentException) {
            return Resource.Error(e.message ?: "Invalid pH value.")
        }

        return manualPhRepository.recordManualPh(entry)
    }
}
