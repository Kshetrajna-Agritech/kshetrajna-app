package com.kshetrajna.app.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.usecase.GetDashboardDataUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for the Kshetrajna Dashboard screen.
 * Consumes [GetDashboardDataUseCase] and exposes a single coherent UI state flow.
 */
class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<DashboardUiState>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {

    init {
        loadDashboardData()
    }

    fun loadDashboardData(nodeId: String = "sim_node_01") {
        viewModelScope.launch(dispatchers.io) {
            updateState(UiState.Loading)
            getDashboardDataUseCase(nodeId).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val dashboardData = resource.data
                        val isOffline = dashboardData.node?.isOnline == false
                        val uiStateData = DashboardUiState(
                            data = dashboardData,
                            isOfflineNode = isOffline,
                        )
                        updateState(UiState.Success(uiStateData))
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
