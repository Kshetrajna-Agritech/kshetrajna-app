package com.kshetrajna.app.domain.usecase

import com.kshetrajna.app.core.result.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateNpkInferenceUseCaseTest {

    @Test
    fun `executing inference returns explicit error indicating model formula is TBD`() = runTest {
        val useCase = CalculateNpkInferenceUseCase()

        val result = useCase(
            nodeId = "sim_node_01",
            soilEcDsPerM = 1.2f,
            soilTemperatureCelsius = 25.0f,
            soilMoisturePercent = 35.0f,
            phValue = 6.5f
        )

        assertTrue(result is Resource.Error)
        val errorMessage = (result as Resource.Error).message
        assertTrue(errorMessage.contains("pending approved contract specification"))
        assertTrue(errorMessage.contains("TBD"))
    }
}
