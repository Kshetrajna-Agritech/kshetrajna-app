package com.kshetrajna.app.ui.fertility

import com.kshetrajna.app.core.state.UiState
import com.kshetrajna.app.data.repository.DefaultFertilityRepository
import com.kshetrajna.app.data.repository.DefaultManualPhRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.SensorReading
import com.kshetrajna.app.domain.usecase.CalculateNpkInferenceUseCase
import com.kshetrajna.app.domain.usecase.GetFertilityDataUseCase
import com.kshetrajna.app.ui.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FertilityViewModelTest {

    private fun createViewModel(
        localDataSource: InMemoryLocalDataSource,
        testDispatchers: TestDispatcherProvider = TestDispatcherProvider()
    ): FertilityViewModel {
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val manualPhRepo = DefaultManualPhRepository(localDataSource)
        val fertilityRepo = DefaultFertilityRepository(localDataSource)

        val getFertilityDataUseCase = GetFertilityDataUseCase(
            telemetryRepository = telemetryRepo,
            manualPhRepository = manualPhRepo,
            fertilityRepository = fertilityRepo
        )
        val calculateUseCase = CalculateNpkInferenceUseCase()

        return FertilityViewModel(
            getFertilityDataUseCase = getFertilityDataUseCase,
            calculateNpkInferenceUseCase = calculateUseCase,
            dispatchers = testDispatchers
        )
    }

    @Test
    fun `viewModel loads supporting inputs and communicates model TBD status`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val reading = SensorReading(
            id = "sr-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            soilEcDsPerM = 1.2f,
            soilTemperatureCelsius = 25.0f,
            soilMoisturePercent = 30.0f,
            category = MeasurementCategory.MEASURED
        )
        localDataSource.readings.value = listOf(reading)

        val manualPh = ManualPH(
            id = "mph-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1100L,
            phValue = 6.5f,
            category = MeasurementCategory.MANUAL
        )
        localDataSource.manualPhs.value = listOf(manualPh)

        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<FertilityUiStateData>).data
        val fertilityData = uiData.fertilityData

        assertNotNull(fertilityData.latestReading)
        assertEquals(1.2f, fertilityData.latestReading?.soilEcDsPerM ?: 0f, 0.001f)
        assertEquals(MeasurementCategory.MEASURED, fertilityData.latestReading?.category)

        assertNotNull(fertilityData.latestManualPh)
        assertEquals(6.5f, fertilityData.latestManualPh?.phValue ?: 0f, 0.001f)
        assertEquals(MeasurementCategory.MANUAL, fertilityData.latestManualPh?.category)

        assertFalse(fertilityData.isModelConfigured)
        assertNull(uiData.calculationError)
    }

    @Test
    fun `requesting inference when model is TBD displays explicit error message`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val viewModel = createViewModel(localDataSource)
        advanceUntilIdle()

        viewModel.onRequestInference()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)

        val uiData = (state as UiState.Success<FertilityUiStateData>).data
        assertNotNull(uiData.calculationError)
        assertTrue(uiData.calculationError!!.contains("pending approved contract specification"))
    }
}
