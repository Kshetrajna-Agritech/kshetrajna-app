package com.kshetrajna.app.ui.alerts

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.model.Alert
import com.kshetrajna.app.domain.usecase.AcknowledgeAlertUseCase
import com.kshetrajna.app.domain.usecase.GetAlertsAndSafetyUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Alerts & Safety screen.
 * Coordinates observation of safety state, active alerts, alert history, and user alert acknowledgements.
 */
class AlertsViewModel(
    private val getAlertsAndSafetyUseCase: GetAlertsAndSafetyUseCase,
    private val acknowledgeAlertUseCase: AcknowledgeAlertUseCase,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<AlertsUiStateData>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {
    private var currentNodeId: String = "sim_node_01"

    init {
        loadData(currentNodeId)
    }

    fun loadData(nodeId: String = "sim_node_01") {
        currentNodeId = nodeId
        viewModelScope.launch(dispatchers.io) {
            getAlertsAndSafetyUseCase(nodeId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val currentData = (uiState.value as? UiState.Success)?.data ?: AlertsUiStateData()
                        updateState(
                            UiState.Success(
                                currentData.copy(
                                    data = resource.data,
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

    fun acknowledgeAlert(alert: Alert) {
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch(dispatchers.io) {
            val result = acknowledgeAlertUseCase(alert)
            when (result) {
                is Resource.Success -> {
                    val latestData = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latestData.copy(
                                actionMessage = "Alert '${alert.title}' acknowledged. (Note: Acknowledged != Resolved)",
                                errorMessage = null
                            )
                        )
                    )
                }
                is Resource.Error -> {
                    val latestData = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latestData.copy(
                                actionMessage = null,
                                errorMessage = result.message
                            )
                        )
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        updateState(
            UiState.Success(
                currentData.copy(
                    actionMessage = null,
                    errorMessage = null
                )
            )
        )
    }
}
