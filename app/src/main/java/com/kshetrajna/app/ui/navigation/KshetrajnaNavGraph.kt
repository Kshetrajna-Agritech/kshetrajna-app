package com.kshetrajna.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kshetrajna.app.ui.dashboard.DashboardScreen
import com.kshetrajna.app.ui.dashboard.DashboardViewModel
import com.kshetrajna.app.ui.screens.AlertsScreen
import com.kshetrajna.app.ui.screens.FertilityScreen
import com.kshetrajna.app.ui.screens.IrrigationScreen
import com.kshetrajna.app.ui.screens.ManualPhScreen
import com.kshetrajna.app.ui.screens.SettingsScreen
import com.kshetrajna.app.ui.screens.SoilScreen
import com.kshetrajna.app.ui.screens.WeatherScreen

/**
 * Root navigation graph mapping top-level destinations to screen composables.
 */
@Composable
fun KshetrajnaNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
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
            SoilScreen()
        }
        composable(KshetrajnaDestination.ManualPH.route) {
            ManualPhScreen()
        }
        composable(KshetrajnaDestination.Fertility.route) {
            FertilityScreen()
        }
        composable(KshetrajnaDestination.Weather.route) {
            WeatherScreen()
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
