package com.kshetrajna.app.data.repository

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.domain.model.WeatherData
import com.kshetrajna.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Implementation of [WeatherRepository].
 */
class DefaultWeatherRepository(
    private val localDataSource: LocalDataSource,
) : WeatherRepository {

    override fun observeLatestWeather(): Flow<Resource<WeatherData?>> {
        return localDataSource.observeLatestWeather().map { weather ->
            Resource.Success(weather) as Resource<WeatherData?>
        }.catch { e ->
            emit(Resource.Error("Failed to observe weather data: ${e.message}", e))
        }
    }

    override suspend fun saveWeatherData(weather: WeatherData): Resource<Unit> {
        return try {
            localDataSource.insertWeatherData(weather)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save weather data locally: ${e.message}", e)
        }
    }
}
