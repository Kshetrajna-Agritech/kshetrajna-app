package com.kshetrajna.app.ui

import com.kshetrajna.app.ui.navigation.KshetrajnaDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationDestinationTest {

    @Test
    fun `topLevelDestinations contains all required UI spec destinations`() {
        val destinations = KshetrajnaDestination.topLevelDestinations
        assertEquals(8, destinations.size)

        val routes = destinations.map { it.route }.toSet()
        assertTrue(routes.contains("dashboard"))
        assertTrue(routes.contains("soil"))
        assertTrue(routes.contains("manual_ph"))
        assertTrue(routes.contains("fertility"))
        assertTrue(routes.contains("weather"))
        assertTrue(routes.contains("irrigation"))
        assertTrue(routes.contains("alerts"))
        assertTrue(routes.contains("settings"))
    }
}
