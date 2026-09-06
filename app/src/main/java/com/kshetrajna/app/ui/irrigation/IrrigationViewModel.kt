package com.kshetrajna.app.ui.irrigation

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.model.IrrigationCommandType
import com.kshetrajna.app.domain.usecase.GetIrrigationDataUseCase
import com.kshetrajna.app.domain.usecase.SendIrrigationCommandUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Irrigation Control screen.
 * Coordinates observation of command lifecycle, actuator state, and safety interlocks.
 */
class IrrigationViewModel(
    private val getIrrigationDataUseCase: GetIrrigationDataUseCase,
    private val sendIrrigationCommandUseCase: SendIrrigationCommandUseCase,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<IrrigationUiStateData>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {
    private var currentNodeId: String = "sim_node_01"

    init {
        loadIrrigationData(currentNodeId)
    }

    fun loadIrrigationData(nodeId: String = "sim_node_01") {
        currentNodeId = nodeId
        viewModelScope.launch(dispatchers.io) {
            getIrrigationDataUseCase(nodeId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val currentData = (uiState.value as? UiState.Success)?.data ?: IrrigationUiStateData()
                        updateState(
                            UiState.Success(
                                currentData.copy(
                                    irrigationData = resource.data,
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

    fun requestCommand(commandType: IrrigationCommandType) {
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch(dispatchers.io) {
            updateState(
                UiState.Success(
                    currentData.copy(
                        isSendingCommand = true,
                        commandActionMessage = null,
                        commandErrorMessage = null
                    )
                )
            )

            val result = sendIrrigationCommandUseCase(nodeId = currentNodeId, commandType = commandType)

            when (result) {
                is Resource.Success -> {
                    val latestData = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latestData.copy(
                                isSendingCommand = false,
                                commandActionMessage = "Irrigation command (${commandType.name}) requested locally (Pending Sync).",
                                commandErrorMessage = null
                            )
                        )
                    )
                }
                is Resource.Error -> {
                    val latestData = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latestData.copy(
                                isSendingCommand = false,
                                commandActionMessage = null,
                                commandErrorMessage = result.message
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
                    commandActionMessage = null,
                    commandErrorMessage = null
                )
            )
        )
    }
}
