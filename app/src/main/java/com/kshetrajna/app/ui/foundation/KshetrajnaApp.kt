package com.kshetrajna.app.ui.foundation

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kshetrajna.app.ui.dashboard.DashboardViewModel
import com.kshetrajna.app.ui.fertility.FertilityViewModel
import com.kshetrajna.app.ui.manualph.ManualPhViewModel
import com.kshetrajna.app.ui.navigation.KshetrajnaDestination
import com.kshetrajna.app.ui.navigation.KshetrajnaNavGraph
import com.kshetrajna.app.ui.soil.SoilViewModel

/**
 * Top-level Application Scaffold establishing UI layout and navigation foundation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KshetrajnaApp(
    dashboardViewModel: DashboardViewModel,
    soilViewModel: SoilViewModel,
    manualPhViewModel: ManualPhViewModel,
    fertilityViewModel: FertilityViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: KshetrajnaDestination.Dashboard.route

    val currentDestination = KshetrajnaDestination.topLevelDestinations.find { it.route == currentRoute }
        ?: KshetrajnaDestination.Dashboard

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = currentDestination.title) }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KshetrajnaDestination.topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            modifier = Modifier.width(80.dp),
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
                            alwaysShowLabel = true
                        )
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}
