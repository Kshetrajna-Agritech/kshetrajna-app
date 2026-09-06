package com.kshetrajna.app.ui.weather

import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.core.coroutine.DefaultDispatcherProvider
import com.kshetrajna.app.core.coroutine.DispatcherProvider
import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.domain.usecase.GetWeatherUseCase
import com.kshetrajna.app.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Weather Context screen.
 * Observes cached/external weather data via [GetWeatherUseCase] without executing irrigation decisions.
 */
class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : BaseViewModel<WeatherUiStateData>(
    initialState = UiState.Loading,
    dispatchers = dispatchers,
) {
    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch(dispatchers.io) {
            getWeatherUseCase().collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val currentData = (uiState.value as? UiState.Success)?.data ?: WeatherUiStateData()
                        updateState(
                            UiState.Success(
                                currentData.copy(
                                    weather = resource.data,
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
}
