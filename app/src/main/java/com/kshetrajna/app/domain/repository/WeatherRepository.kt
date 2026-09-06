package com.kshetrajna.app.domain.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for external/cached weather context.
 */
interface WeatherRepository {
    fun observeLatestWeather(): Flow<Resource<WeatherData?>>
    suspend fun saveWeatherData(weather: WeatherData): Resource<Unit>
}
