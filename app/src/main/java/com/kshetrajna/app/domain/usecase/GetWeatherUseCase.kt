package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.domain.model.WeatherData
import com.kshetrajna.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

/**
 * Domain use case providing observable external weather context from local persistence/cache.
 * Weather information is presented strictly as informational context without making irrigation decisions.
 */
open class GetWeatherUseCase(
    private val weatherRepository: WeatherRepository,
) {
    open operator fun invoke(): Flow<Resource<WeatherData?>> {
        return weatherRepository.observeLatestWeather()
    }
}
