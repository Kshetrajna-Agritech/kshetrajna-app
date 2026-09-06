package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import com.kshetrajna.app.data.repository.DefaultFertilityRepository
import com.kshetrajna.app.data.repository.DefaultManualPhRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.InMemoryLocalDataSource
import com.kshetrajna.app.domain.model.ManualPH
import com.kshetrajna.app.domain.model.MeasurementCategory
import com.kshetrajna.app.domain.model.NpkResult
import com.kshetrajna.app.domain.model.SensorReading
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFertilityDataUseCaseTest {

    @Test
    fun `supporting measurements load correctly and preserve measurement provenance`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val manualPhRepo = DefaultManualPhRepository(localDataSource)
        val fertilityRepo = DefaultFertilityRepository(localDataSource)

        val reading = SensorReading(
            id = "sr-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            soilEcDsPerM = 1.4f,
            soilTemperatureCelsius = 24.5f,
            soilMoisturePercent = 32.0f,
            category = MeasurementCategory.MEASURED
        )
        localDataSource.readings.value = listOf(reading)

        val manualPh = ManualPH(
            id = "mph-1",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1100L,
            phValue = 6.8f,
            category = MeasurementCategory.MANUAL
        )
        localDataSource.manualPhs.value = listOf(manualPh)

        val useCase = GetFertilityDataUseCase(
            telemetryRepository = telemetryRepo,
            manualPhRepository = manualPhRepo,
            fertilityRepository = fertilityRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val fertilityData = (resource as Resource.Success).data
        val latestReading = fertilityData.latestReading
        assertNotNull(latestReading)
        assertEquals(1.4f, latestReading?.soilEcDsPerM ?: 0f, 0.001f)
        assertEquals(24.5f, latestReading?.soilTemperatureCelsius ?: 0f, 0.001f)
        assertEquals(32.0f, latestReading?.soilMoisturePercent ?: 0f, 0.001f)
        assertEquals(MeasurementCategory.MEASURED, latestReading?.category)

        val latestManualPh = fertilityData.latestManualPh
        assertNotNull(latestManualPh)
        assertEquals(6.8f, latestManualPh?.phValue ?: 0f, 0.001f)
        assertEquals(MeasurementCategory.MANUAL, latestManualPh?.category)

        assertFalse(fertilityData.isModelConfigured)
        assertTrue(fertilityData.modelStatusMessage.contains("pending approved contract specification"))
    }

    @Test
    fun `missing supporting measurements are handled gracefully without zero substitution`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val manualPhRepo = DefaultManualPhRepository(localDataSource)
        val fertilityRepo = DefaultFertilityRepository(localDataSource)

        val readingPartial = SensorReading(
            id = "sr-partial",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            soilEcDsPerM = null,
            soilTemperatureCelsius = null,
            soilMoisturePercent = null,
            category = MeasurementCategory.MEASURED
        )
        localDataSource.readings.value = listOf(readingPartial)
        localDataSource.manualPhs.value = emptyList()

        val useCase = GetFertilityDataUseCase(
            telemetryRepository = telemetryRepo,
            manualPhRepository = manualPhRepo,
            fertilityRepository = fertilityRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val fertilityData = (resource as Resource.Success).data
        val latestReading = fertilityData.latestReading
        assertNotNull(latestReading)
        assertNull(latestReading?.soilEcDsPerM)
        assertNull(latestReading?.soilTemperatureCelsius)
        assertNull(latestReading?.soilMoisturePercent)
        assertNull(fertilityData.latestManualPh)
    }

    @Test
    fun `model unavailable state is explicitly reported`() = runTest {
        val localDataSource = InMemoryLocalDataSource()
        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val manualPhRepo = DefaultManualPhRepository(localDataSource)
        val fertilityRepo = DefaultFertilityRepository(localDataSource)

        val npkResultTbd = NpkResult(
            id = "npk-tbd",
            nodeId = "sim_node_01",
            timestampEpochMillis = 1000L,
            inferredNitrogenPpm = null,
            inferredPhosphorusPpm = null,
            inferredPotassiumPpm = null,
            modelVersion = "TBD_UNCONFIGURED",
            category = MeasurementCategory.INFERRED
        )
        localDataSource.npkResults.value = listOf(npkResultTbd)

        val useCase = GetFertilityDataUseCase(
            telemetryRepository = telemetryRepo,
            manualPhRepository = manualPhRepo,
            fertilityRepository = fertilityRepo
        )

        val resource = useCase("sim_node_01").first()
        assertTrue(resource is Resource.Success)

        val fertilityData = (resource as Resource.Success).data
        assertFalse(fertilityData.isModelConfigured)
        val latestNpk = fertilityData.latestNpkResult
        assertNotNull(latestNpk)
        assertNull(latestNpk?.inferredNitrogenPpm)
        assertNull(latestNpk?.inferredPhosphorusPpm)
        assertNull(latestNpk?.inferredPotassiumPpm)
        assertEquals(MeasurementCategory.INFERRED, latestNpk?.category)
    }
}
