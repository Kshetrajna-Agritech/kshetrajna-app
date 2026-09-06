package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SyncStatus
import com.kshetrajna.app.domain.repository.ManualPhRepository
import java.util.UUID

/**
 * Domain use case validating and recording a manual pH measurement entry.
 * Enforces input format validation (non-empty, valid numeric float) and preserves MANUAL provenance semantics.
 * Note: Numerical domain range for pH remains TBD pending contract specification.
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

        val entry = ManualPH(
            id = "mph_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}",
            nodeId = nodeId,
            timestampEpochMillis = timestampEpochMillis,
            phValue = parsedValue,
            notes = notes?.trim()?.ifEmpty { null },
            enteredByUserId = enteredByUserId,
            syncStatus = SyncStatus.PENDING,
            category = MeasurementCategory.MANUAL
        )

        return manualPhRepository.recordManualPh(entry)
    }
}
