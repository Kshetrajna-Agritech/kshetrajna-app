package com.kshetrajna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.kshetrajna.app.core.simulation.SimulationScenario
import com.kshetrajna.app.data.local.KshetrajnaDatabase
import com.kshetrajna.app.data.local.RoomLocalDataSource
import com.kshetrajna.app.data.remote.DefaultRemoteDataSource
import com.kshetrajna.app.data.remote.RemoteDataSource
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultSyncRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.simulation.SimulatedDataSourceSeeder
import com.kshetrajna.app.domain.usecase.GetDashboardDataUseCase
import com.kshetrajna.app.ui.dashboard.DashboardViewModel
import com.kshetrajna.app.ui.foundation.KshetrajnaApp
import com.kshetrajna.app.ui.theme.KshetrajnaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KshetrajnaDatabase.getInstance(this)
        val localDataSource = RoomLocalDataSource(database)
        val remoteDataSource: RemoteDataSource = DefaultRemoteDataSource()

        val telemetryRepo = DefaultTelemetryRepository(localDataSource)
        val weatherRepo = DefaultWeatherRepository(localDataSource)
        val irrigationRepo = DefaultIrrigationRepository(localDataSource)
        val safetyRepo = DefaultSafetyRepository(localDataSource)
        val syncRepo = DefaultSyncRepository(localDataSource, remoteDataSource)

        val getDashboardDataUseCase = GetDashboardDataUseCase(
            telemetryRepository = telemetryRepo,
            weatherRepository = weatherRepo,
            irrigationRepository = irrigationRepo,
            safetyRepository = safetyRepo,
            syncRepository = syncRepo
        )

        dashboardViewModel = DashboardViewModel(
            getDashboardDataUseCase = getDashboardDataUseCase
        )

        // Seed initial simulation scenario data into local database for offline dashboard rendering
        lifecycleScope.launch(Dispatchers.IO) {
            val seeder = SimulatedDataSourceSeeder()
            seeder.seedScenario(localDataSource, SimulationScenario.NORMAL_FARM, steps = 1)
        }

        setContent {
            KshetrajnaTheme {
                KshetrajnaApp(dashboardViewModel = dashboardViewModel)
            }
        }
    }
}
