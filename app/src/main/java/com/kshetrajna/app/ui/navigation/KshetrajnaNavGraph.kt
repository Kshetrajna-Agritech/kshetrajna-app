package com.kshetrajna.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kshetrajna.app.ui.dashboard.DashboardScreen
import com.kshetrajna.app.ui.dashboard.DashboardViewModel
import com.kshetrajna.app.ui.fertility.FertilityScreen
import com.kshetrajna.app.ui.fertility.FertilityViewModel
import com.kshetrajna.app.ui.manualph.ManualPhScreen
import com.kshetrajna.app.ui.manualph.ManualPhViewModel
import com.kshetrajna.app.ui.screens.AlertsScreen
import com.kshetrajna.app.ui.screens.IrrigationScreen
import com.kshetrajna.app.ui.screens.SettingsScreen
import com.kshetrajna.app.ui.soil.SoilScreen
import com.kshetrajna.app.ui.soil.SoilViewModel
import com.kshetrajna.app.ui.weather.WeatherScreen
import com.kshetrajna.app.ui.weather.WeatherViewModel

/**
 * Root navigation graph mapping top-level destinations to screen composables.
 */
@Composable
fun KshetrajnaNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    soilViewModel: SoilViewModel,
    manualPhViewModel: ManualPhViewModel,
    fertilityViewModel: FertilityViewModel,
    weatherViewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = KshetrajnaDestination.Dashboard.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(KshetrajnaDestination.Dashboard.route) {
            DashboardScreen(viewModel = dashboardViewModel)
        }
        composable(KshetrajnaDestination.Soil.route) {
            SoilScreen(viewModel = soilViewModel)
        }
        composable(KshetrajnaDestination.ManualPH.route) {
            ManualPhScreen(viewModel = manualPhViewModel)
        }
        composable(KshetrajnaDestination.Fertility.route) {
            FertilityScreen(viewModel = fertilityViewModel)
        }
        composable(KshetrajnaDestination.Weather.route) {
            WeatherScreen(viewModel = weatherViewModel)
        }
        composable(KshetrajnaDestination.Irrigation.route) {
            IrrigationScreen()
        }
        composable(KshetrajnaDestination.Alerts.route) {
            AlertsScreen()
        }
        composable(KshetrajnaDestination.Settings.route) {
            SettingsScreen()
        }
    }
}
