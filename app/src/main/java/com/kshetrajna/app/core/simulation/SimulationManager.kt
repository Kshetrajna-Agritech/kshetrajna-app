package com.kshetrajna.app.core.simulation

import com.kshetrajna.app.data.local.LocalDataSource
import com.kshetrajna.app.data.simulation.SimulatedDataSourceSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller managing active simulation scenarios and controllable simulation step progression.
 */
class SimulationManager(
    val clock: SimulationClock = SimulationClock(),
    val engine: SimulationEngine = SimulationEngine(clock = clock),
    private val seeder: SimulatedDataSourceSeeder = SimulatedDataSourceSeeder(engine = engine)
) {
    private val _activeScenario = MutableStateFlow(SimulationScenario.NORMAL_FARM)
    val activeScenario: StateFlow<SimulationScenario> = _activeScenario.asStateFlow()

    private var currentStep = 0

    suspend fun setScenario(scenario: SimulationScenario, localDataSource: LocalDataSource) {
        _activeScenario.value = scenario
        currentStep = 0
        seeder.seedScenario(localDataSource, scenario, steps = 1)
    }

    suspend fun advanceStep(localDataSource: LocalDataSource) {
        currentStep++
        seeder.seedScenario(localDataSource, _activeScenario.value, steps = currentStep + 1)
    }

    fun getCurrentStep(): Int = currentStep
}
