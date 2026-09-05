package com.kshetrajna.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel establishing state management patterns for future screens.
 */
abstract class BaseViewModel<T>(
    initialState: UiState<T> = UiState.Loading,
    protected val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected fun updateState(newState: UiState<T>) {
        _uiState.value = newState
    }

    protected fun launchOnIO(block: suspend () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            block()
        }
    }
}

/**
 * Concrete foundation ViewModel for general app state.
 */
class FoundationViewModel(
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : BaseViewModel<String>(
    initialState = UiState.Success("Kshetrajna Application Foundation Ready"),
    dispatchers = dispatchers
)
