package com.kshetrajna.app.core.state

/**
 * Standard UI state hierarchy representing common UI states as specified in UI_SPEC.md.
 * Screens consume instances of [UiState] exposed via StateFlow from ViewModels.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
    data class Stale<T>(val data: T, val message: String? = null) : UiState<T>
    data class SafetyLocked(val reason: String) : UiState<Nothing>
}
