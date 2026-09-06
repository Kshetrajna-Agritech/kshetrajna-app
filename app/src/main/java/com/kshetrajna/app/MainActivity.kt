package com.kshetrajna.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kshetrajna.app.core.simulation.SimulationScenario
import com.kshetrajna.app.data.local.KshetrajnaDatabase
import com.kshetrajna.app.data.local.RoomLocalDataSource
import com.kshetrajna.app.data.remote.DefaultRemoteDataSource
import com.kshetrajna.app.data.remote.RemoteDataSource
import com.kshetrajna.app.data.repository.DefaultIrrigationRepository
import com.kshetrajna.app.data.repository.DefaultManualPhRepository
import com.kshetrajna.app.data.repository.DefaultSafetyRepository
import com.kshetrajna.app.data.repository.DefaultSyncRepository
import com.kshetrajna.app.data.repository.DefaultTelemetryRepository
import com.kshetrajna.app.data.repository.DefaultWeatherRepository
import com.kshetrajna.app.data.simulation.SimulatedDataSourceSeeder
import com.kshetrajna.app.domain.usecase.GetDashboardDataUseCase
import com.kshetrajna.app.domain.usecase.GetManualPhEntriesUseCase
import com.kshetrajna.app.domain.usecase.GetSoilTelemetryUseCase
import com.kshetrajna.app.domain.usecase.RecordManualPhUseCase
import com.kshetrajna.app.ui.dashboard.DashboardViewModel
import com.kshetrajna.app.ui.foundation.KshetrajnaApp
import com.kshetrajna.app.ui.manualph.ManualPhViewModel
import com.kshetrajna.app.ui.soil.SoilViewModel
import com.kshetrajna.app.ui.theme.KshetrajnaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = KshetrajnaDatabase.getInstance(applicationContext)
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
                    syncRepository = syncRepo,
                )

                return DashboardViewModel(
                    getDashboardDataUseCase = getDashboardDataUseCase
                ) as T
            }
        }
    }

    private val soilViewModel: SoilViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = KshetrajnaDatabase.getInstance(applicationContext)
                val localDataSource = RoomLocalDataSource(database)
                val telemetryRepo = DefaultTelemetryRepository(localDataSource)

                val getSoilTelemetryUseCase = GetSoilTelemetryUseCase(
                    telemetryRepository = telemetryRepo,
                )

                return SoilViewModel(
                    getSoilTelemetryUseCase = getSoilTelemetryUseCase
                ) as T
            }
        }
    }

    private val manualPhViewModel: ManualPhViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = KshetrajnaDatabase.getInstance(applicationContext)
                val localDataSource = RoomLocalDataSource(database)
                val manualPhRepo = DefaultManualPhRepository(localDataSource)

                val getManualPhEntriesUseCase = GetManualPhEntriesUseCase(manualPhRepo)
                val recordManualPhUseCase = RecordManualPhUseCase(manualPhRepo)

                return ManualPhViewModel(
                    getManualPhEntriesUseCase = getManualPhEntriesUseCase,
                    recordManualPhUseCase = recordManualPhUseCase
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed initial simulation scenario data safely on IO dispatcher
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = KshetrajnaDatabase.getInstance(applicationContext)
                val localDataSource = RoomLocalDataSource(database)
                val seeder = SimulatedDataSourceSeeder()
                seeder.seedScenario(localDataSource, SimulationScenario.NORMAL_FARM, steps = 1)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error seeding initial simulation scenario", e)
            }
        }

        setContent {
            KshetrajnaTheme {
                KshetrajnaApp(
                    dashboardViewModel = dashboardViewModel,
                    soilViewModel = soilViewModel,
                    manualPhViewModel = manualPhViewModel,
                )
            }
        }
    }
}
