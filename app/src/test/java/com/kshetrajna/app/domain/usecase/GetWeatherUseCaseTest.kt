package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.WeatherData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWeatherUseCaseTest {

    @Test
    fun `latest weather data loads successfully and preserves external forecast provenance`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val weatherRepo = DefaultWeatherRepository(localDataSource)

        val weather = WeatherData(
            id = "w-1",
            farmId = "sim_farm_01",
            retrievedAtEpochMillis = 1000L,
            temperatureCelsius = 28.5f,
            humidityPercent = 65.0f,
            rainfallMm = 2.0f,
            conditionText = "Partly Cloudy",
            sourceName = "External Forecast",
            isCached = true,
            category = MeasurementCategory.EXTERNAL_FORECAST
        )
        localDataSource.weatherData.value = weather

        val useCase = GetWeatherUseCase(weatherRepo)

        val resource = useCase().first()
        assertTrue(resource is Resource.Success)

        val data = (resource as Resource.Success).data
        assertNotNull(data)
        assertEquals("w-1", data?.id)
        assertEquals(28.5f, data?.temperatureCelsius ?: 0f, 0.001f)
        assertEquals(65.0f, data?.humidityPercent ?: 0f, 0.001f)
        assertEquals(2.0f, data?.rainfallMm ?: 0f, 0.001f)
        assertEquals("Partly Cloudy", data?.conditionText)
        assertEquals(MeasurementCategory.EXTERNAL_FORECAST, data?.category)
        assertTrue(data?.isCached == true)
    }

    @Test
    fun `missing optional weather fields are preserved as null without zero substitution`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val weatherRepo = DefaultWeatherRepository(localDataSource)

        val weatherPartial = WeatherData(
            id = "w-partial",
            farmId = "sim_farm_01",
            retrievedAtEpochMillis = 1000L,
            temperatureCelsius = null,
            humidityPercent = null,
            rainfallMm = null,
            conditionText = null
        )
        localDataSource.weatherData.value = weatherPartial

        val useCase = GetWeatherUseCase(weatherRepo)

        val resource = useCase().first()
        assertTrue(resource is Resource.Success)

        val data = (resource as Resource.Success).data
        assertNotNull(data)
        assertNull(data?.temperatureCelsius)
        assertNull(data?.humidityPercent)
        assertNull(data?.rainfallMm)
        assertNull(data?.conditionText)
    }

    @Test
    fun `offline with no weather data returns null gracefully`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val weatherRepo = DefaultWeatherRepository(localDataSource)

        localDataSource.weatherData.value = null

        val useCase = GetWeatherUseCase(weatherRepo)

        val resource = useCase().first()
        assertTrue(resource is Resource.Success)
        assertNull((resource as Resource.Success).data)
    }
}
