package com.kshetrajna.app.ui.soil

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.usecase.GetSoilTelemetryUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Soil Telemetry screen.
 * Consumes [GetSoilTelemetryUseCase] and exposes a single coherent UI state flow.
 */
class SoilViewModel(
    private val getSoilTelemetryUseCase: GetSoilTelemetryUseCase,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<SoilUiStateData>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {

    init {
        loadSoilTelemetry()
    }

    fun loadSoilTelemetry(nodeId: String = "sim_node_01") {
        viewModelScope.launch(dispatchers.io) {
            updateState(UiState.Loading)
            getSoilTelemetryUseCase(nodeId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val telemetryData = resource.data
                        val isOffline = telemetryData.node?.isOnline == false
                        if (telemetryData.latestReading == null && telemetryData.historyReadings.isEmpty()) {
                            updateState(UiState.Empty)
                        } else {
                            val uiStateData = SoilUiStateData(
                                telemetryData = telemetryData,
                                isOfflineNode = isOffline,
                            )
                            updateState(UiState.Success(uiStateData))
                        }
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
}
