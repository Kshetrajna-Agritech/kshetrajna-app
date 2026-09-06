package com.kshetrajna.app.ui.dashboard

import com.kshetrajna.app.domain.model.DashboardData

/**
 * Coherent UI state representation for the Dashboard screen.
 */
data class DashboardUiState(
    val data: DashboardData? = null,
    val isOfflineNode: Boolean = false,
    val errorMessage: String? = null,
)
