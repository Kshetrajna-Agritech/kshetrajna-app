package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.repository.SafetyRepository

/**
 * Domain use case acknowledging user notification for an alert.
 * Strictly preserves ACKNOWLEDGED != RESOLVED semantics (acknowledgement does not clear hardware safety lockout).
 */
open class AcknowledgeAlertUseCase(
    private val safetyRepository: SafetyRepository,
) {
    open suspend operator fun invoke(alert: Alert): Resource<Unit> {
        return safetyRepository.acknowledgeAlert(alert)
    }
}
