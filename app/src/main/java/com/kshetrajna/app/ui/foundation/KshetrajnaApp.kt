package com.kshetrajna.app.ui.foundation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kshetrajna.app.ui.navigation.KshetrajnaDestination
import com.kshetrajna.app.ui.navigation.KshetrajnaNavGraph

/**
 * Top-level Application Scaffold establishing UI layout and navigation foundation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KshetrajnaApp(
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
            NavigationBar {
                KshetrajnaDestination.topLevelDestinations.forEach { destination ->
                    NavigationBarItem(
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
                        label = { Text(text = destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        KshetrajnaNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
