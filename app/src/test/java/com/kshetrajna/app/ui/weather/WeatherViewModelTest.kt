package com.kshetrajna.app.ui.weather

import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.WeatherData
import com.kshetrajna.app.domain.usecase.GetWeatherUseCase
import com.kshetrajna.app.ui.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private fun createViewModel(
        localDataSource: InMemoryLocalDataSource,
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): WeatherViewModel {
        val weatherRepo = DefaultWeatherRepository(localDataSource)
        val getWeatherUseCase = GetWeatherUseCase(weatherRepo)

        return WeatherViewModel(
            getWeatherUseCase = getWeatherUseCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `viewModel loads cached weather data and preserves external provenance`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val weather = WeatherData(
            id = "w-1",
            farmId = "sim_farm_01",
            retrievedAtEpochMillis = 1000L,
            temperatureCelsius = 29.0f,
            humidityPercent = 60.0f,
            rainfallMm = 0.0f,
            conditionText = "Clear",
            sourceName = "External Forecast",
            isCached = true,
            category = MeasurementCategory.EXTERNAL_FORECAST
        )
        localDataSource.weatherData.value = weather

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<WeatherUiStateData>).data
        val loadedWeather = uiData.weather

        assertNotNull(loadedWeather)
        assertEquals("w-1", loadedWeather?.id)
        assertEquals(29.0f, loadedWeather?.temperatureCelsius ?: 0f, 0.001f)
        assertEquals(MeasurementCategory.EXTERNAL_FORECAST, loadedWeather?.category)
        assertTrue(loadedWeather?.isCached == true)
    }

    @Test
    fun `viewModel handles empty weather state gracefully`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        localDataSource.weatherData.value = null

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<WeatherUiStateData>).data
        assertNull(uiData.weather)
    }
}
