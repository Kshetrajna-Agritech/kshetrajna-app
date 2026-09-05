package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource

/**
 * Foundation contract for asynchronous domain use cases.
 * Enforces `UI -> ViewModel -> Use Case -> Repository -> Data Sources` dependency order.
 */
interface UseCase<in Params, out ResultType> {
    suspend operator fun invoke(params: Params): Resource<ResultType>
}

/**
 * UseCase contract for parameterless operations.
 */
interface NoParamsUseCase<out ResultType> {
    suspend operator fun invoke(): Resource<ResultType>
}
