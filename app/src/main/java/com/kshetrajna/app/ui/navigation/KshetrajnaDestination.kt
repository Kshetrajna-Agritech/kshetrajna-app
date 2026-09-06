package com.kshetrajna.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level screen destinations matching UI_SPEC.md requirements.
 */
sealed class KshetrajnaDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Dashboard : KshetrajnaDestination("dashboard", "Dashboard", Icons.Default.Home)
    data object Soil : KshetrajnaDestination("soil", "Soil Telemetry", Icons.Default.Info)
    data object ManualPH : KshetrajnaDestination("manual_ph", "Manual pH", Icons.Default.Edit)
    data object Fertility : KshetrajnaDestination("fertility", "Fertility & NPK", Icons.Default.Info)
    data object Weather : KshetrajnaDestination("weather", "Weather Context", Icons.Default.Refresh)
    data object Irrigation : KshetrajnaDestination("irrigation", "Irrigation", Icons.Default.Refresh)
    data object Alerts : KshetrajnaDestination("alerts", "Alerts", Icons.Default.Notifications)
    data object Settings : KshetrajnaDestination("settings", "Settings", Icons.Default.Settings)

    companion object {
        val topLevelDestinations: List<KshetrajnaDestination>
            get() = listOf(
                Dashboard,
                Soil,
                ManualPH,
                Fertility,
                Weather,
                Irrigation,
                Alerts,
                Settings,
            )
    }
}
