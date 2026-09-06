package com.kshetrajna.app.ui.fertility

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.usecase.CalculateNpkInferenceUseCase
import com.kshetrajna.app.domain.usecase.GetFertilityDataUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Fertility / NPK Inferences screen.
 * Coordinates observation of supporting inputs (EC, temp, moisture, pH) and model availability status.
 */
class FertilityViewModel(
    private val getFertilityDataUseCase: GetFertilityDataUseCase,
    private val calculateNpkInferenceUseCase: CalculateNpkInferenceUseCase = CalculateNpkInferenceUseCase(),
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<FertilityUiStateData>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {
    private var currentNodeId: String = "sim_node_01"

    init {
        loadFertilityData(currentNodeId)
    }

    fun loadFertilityData(nodeId: String = "sim_node_01") {
        currentNodeId = nodeId
        viewModelScope.launch(dispatchers.io) {
            getFertilityDataUseCase(nodeId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val currentData = (uiState.value as? UiState.Success)?.data ?: FertilityUiStateData()
                        updateState(
                            UiState.Success(
                                currentData.copy(
                                    fertilityData = resource.data,
                                )
                            )
                        )
                    }
                    is Resource.Error -> {
                        updateState(UiState.Error(resource.message, resource.cause))
                    }
                    is Resource.Loading -> {
                        updateState(UiState.Loading)
                    }
                }
            }
        }
    }

    fun onRequestInference() {
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        val data = currentData.fertilityData
        viewModelScope.launch(dispatchers.io) {
            updateState(UiState.Success(currentData.copy(isCalculating = true, calculationError = null)))
            val result = calculateNpkInferenceUseCase(
                nodeId = currentNodeId,
                soilEcDsPerM = data.latestReading?.soilEcDsPerM,
                soilTemperatureCelsius = data.latestReading?.soilTemperatureCelsius,
                soilMoisturePercent = data.latestReading?.soilMoisturePercent,
                phValue = data.latestManualPh?.phValue
            )
            when (result) {
                is Resource.Error -> {
                    val latest = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latest.copy(
                                isCalculating = false,
                                calculationError = result.message
                            )
                        )
                    )
                }
                is Resource.Success -> {
                    val latest = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latest.copy(
                                isCalculating = false,
                                calculationError = null
                            )
                        )
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() {
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        updateState(UiState.Success(currentData.copy(calculationError = null)))
    }
}
