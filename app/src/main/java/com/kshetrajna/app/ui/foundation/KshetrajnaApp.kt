package com.kshetrajna.app.ui.foundation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kshetrajna.app.ui.alerts.AlertsViewModel
import com.kshetrajna.app.ui.dashboard.DashboardViewModel
import com.kshetrajna.app.ui.fertility.FertilityViewModel
import com.kshetrajna.app.ui.irrigation.IrrigationViewModel
import com.kshetrajna.app.ui.manualph.ManualPhViewModel
import com.kshetrajna.app.ui.navigation.KshetrajnaDestination
import com.kshetrajna.app.ui.navigation.KshetrajnaNavGraph
import com.kshetrajna.app.ui.soil.SoilViewModel
import com.kshetrajna.app.ui.weather.WeatherViewModel

/**
 * Top-level Application Scaffold establishing UI layout and navigation foundation.
 * M6.2 Polish: Enhanced horizontally scrollable bottom navigation with smooth auto-scroll,
 * edge fade indicators, M3 styling, and single-line unclipped labels for all 8 destinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KshetrajnaApp(
    dashboardViewModel: DashboardViewModel,
    soilViewModel: SoilViewModel,
    manualPhViewModel: ManualPhViewModel,
    fertilityViewModel: FertilityViewModel,
    weatherViewModel: WeatherViewModel,
    irrigationViewModel: IrrigationViewModel,
    alertsViewModel: AlertsViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: KshetrajnaDestination.Dashboard.route

    val currentDestination = KshetrajnaDestination.topLevelDestinations.find { it.route == currentRoute }
        ?: KshetrajnaDestination.Dashboard

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val selectedIndex = KshetrajnaDestination.topLevelDestinations.indexOfFirst { it.route == currentRoute }

    // Auto-scroll selected item into view smoothly
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            val itemWidthPx = with(density) { 88.dp.toPx() }
            val screenWidthPx = with(density) { 360.dp.toPx() }
            val targetScrollPx = ((selectedIndex * itemWidthPx) - (screenWidthPx / 2) + (itemWidthPx / 2))
                .coerceAtLeast(0f)
                .toInt()
            scrollState.animateScrollTo(targetScrollPx)
        }
    }

    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = currentDestination.title) }
            )
        },
        bottomBar = {
            Surface(
                color = surfaceVariantColor,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .graphicsLayer { alpha = 0.99f }
                            .drawWithContent {
                                drawContent()
                                // Subtle left edge fade
                                if (scrollState.value > 0) {
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(surfaceVariantColor, Color.Transparent),
                                            startX = 0f,
                                            endX = 24.dp.toPx()
                                        )
                                    )
                                }
                                // Subtle right edge fade
                                if (scrollState.value < scrollState.maxValue) {
                                    drawRect(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, surfaceVariantColor),
                                            startX = size.width - 24.dp.toPx(),
                                            endX = size.width
                                        )
                                    )
                                }
                            }
                            .horizontalScroll(scrollState)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KshetrajnaDestination.topLevelDestinations.forEach { destination ->
                            NavigationBarItem(
                                modifier = Modifier.width(88.dp),
                                selected = currentRoute == destination.route,
                                onClick = {
                                    if (currentRoute != destination.route) {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.shortTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        KshetrajnaNavGraph(
            navController = navController,
            dashboardViewModel = dashboardViewModel,
            soilViewModel = soilViewModel,
            manualPhViewModel = manualPhViewModel,
            fertilityViewModel = fertilityViewModel,
            weatherViewModel = weatherViewModel,
            irrigationViewModel = irrigationViewModel,
            alertsViewModel = alertsViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
