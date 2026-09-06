package com.kshetrajna.app.ui.weather

import com.kshetrajna.app.domain.model.WeatherData

data class WeatherUiStateData(
    val weather: WeatherData? = null,
    val farmId: String = "sim_farm_01",
    val isRefreshing: Boolean = false,
    val message: String? = null
)
