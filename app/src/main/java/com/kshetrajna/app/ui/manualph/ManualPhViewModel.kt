package com.kshetrajna.app.ui.manualph

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.usecase.GetManualPhEntriesUseCase
import com.kshetrajna.app.domain.usecase.RecordManualPhUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Manual pH screen.
 * Coordinates input validation, local persistence via [RecordManualPhUseCase], and entry observation via [GetManualPhEntriesUseCase].
 */
class ManualPhViewModel(
    private val getManualPhEntriesUseCase: GetManualPhEntriesUseCase,
    private val recordManualPhUseCase: RecordManualPhUseCase,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<ManualPhUiStateData>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {

    private var currentNodeId: String = "sim_node_01"

    init {
        loadEntries(currentNodeId)
    }

    fun loadEntries(nodeId: String = "sim_node_01") {
        currentNodeId = nodeId
        viewModelScope.launch(dispatchers.io) {
            getManualPhEntriesUseCase(nodeId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val entries = resource.data
                        val currentData = (uiState.value as? UiState.Success)?.data ?: ManualPhUiStateData()
                        val updatedData = currentData.copy(
                            entries = entries,
                            latestEntry = entries.firstOrNull(),
                        )
                        updateState(UiState.Success(updatedData))
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

    fun onPhInputChanged(input: String) {
        val currentData = (uiState.value as? UiState.Success)?.data ?: ManualPhUiStateData()
        updateState(
            UiState.Success(
                currentData.copy(
                    inputPhText = input,
                    validationError = null,
                    saveSuccessMessage = null,
                )
            )
        )
    }

    fun onNotesInputChanged(input: String) {
        val currentData = (uiState.value as? UiState.Success)?.data ?: ManualPhUiStateData()
        updateState(
            UiState.Success(
                currentData.copy(
                    inputNotesText = input,
                    validationError = null,
                )
            )
        )
    }

    fun saveManualPh(nodeId: String = currentNodeId) {
        val currentData = (uiState.value as? UiState.Success)?.data ?: ManualPhUiStateData()
        val phInput = currentData.inputPhText
        val notesInput = currentData.inputNotesText

        viewModelScope.launch(dispatchers.io) {
            updateState(UiState.Success(currentData.copy(isSaving = true, validationError = null)))

            val result = recordManualPhUseCase(
                nodeId = nodeId,
                phInput = phInput,
                notes = notesInput,
            )

            when (result) {
                is Resource.Success -> {
                    val latestData = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latestData.copy(
                                isSaving = false,
                                inputPhText = "",
                                inputNotesText = "",
                                validationError = null,
                                saveSuccessMessage = "Manual pH measurement saved locally (Pending Sync).",
                            )
                        )
                    )
                }
                is Resource.Error -> {
                    val latestData = (uiState.value as? UiState.Success)?.data ?: currentData
                    updateState(
                        UiState.Success(
                            latestData.copy(
                                isSaving = false,
                                validationError = result.message,
                                saveSuccessMessage = null,
                            )
                        )
                    )
                }
                is Resource.Loading -> {
                    // Handled via isSaving = true
                }
            }
        }
    }

    fun clearMessages() {
        val currentData = (uiState.value as? UiState.Success)?.data ?: return
        updateState(
            UiState.Success(
                currentData.copy(
                    validationError = null,
                    saveSuccessMessage = null,
                )
            )
        )
    }
}
