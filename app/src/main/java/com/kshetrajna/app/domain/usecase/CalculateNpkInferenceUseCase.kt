package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.NpkResult

/**
 * Domain use case boundary for executing NPK inference.
 * Currently returns explicit error/unconfigured status because the scientific NPK inference engine is TBD.
 */
open class CalculateNpkInferenceUseCase {
    open suspend operator fun invoke(
        nodeId: String,
        soilEcDsPerM: Float?,
        soilTemperatureCelsius: Float?,
        soilMoisturePercent: Float?,
        phValue: Float?
    ): Resource<NpkResult> {
        return Resource.Error(
            "NPK inference engine model formula and calibration constants are pending approved contract specification (TBD)."
        )
    }
}
