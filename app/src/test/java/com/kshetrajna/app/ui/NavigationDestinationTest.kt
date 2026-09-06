package com.kshetrajna.app.ui

import com.kshetrajna.app.ui.navigation.KshetrajnaDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationDestinationTest {

    @Test
    fun `topLevelDestinations contains all required UI spec destinations and no elements are null`() {
        val destinations = KshetrajnaDestination.topLevelDestinations
        assertEquals(8, destinations.size)

        destinations.forEach { destination ->
            assertNotNull("Destination element in topLevelDestinations must not be null", destination)
            assertNotNull("Route must not be null", destination.route)
            assertNotNull("Title must not be null", destination.title)
        }

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
