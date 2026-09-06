package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.repository.ManualPhRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Domain use case observing recorded manual pH entries for a node in reverse chronological order.
 */
open class GetManualPhEntriesUseCase(
    private val manualPhRepository: ManualPhRepository,
) {
    open operator fun invoke(nodeId: String = "sim_node_01"): Flow<Resource<List<ManualPH>>> {
        return manualPhRepository.observeManualPhForNode(nodeId).map { resource ->
            when (resource) {
                is Resource.Success -> {
                    val sorted = resource.data.sortedByDescending { it.timestampEpochMillis }
                    Resource.Success(sorted)
                }
                is Resource.Error -> Resource.Error(resource.message, resource.cause)
                is Resource.Loading -> Resource.Loading
            }
        }
    }
}
