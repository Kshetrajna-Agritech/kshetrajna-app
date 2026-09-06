package com.kshetrajna.app.data.local

import com.kshetrajna.app.data.local.converter.Converters
import com.kshetrajna.app.domain.model.SafetyFault
import com.kshetrajna.app.domain.model.SafetyFaultType
import com.kshetrajna.app.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `SyncStatus serialization and deserialization`() {
        val serialized = converters.fromSyncStatus(SyncStatus.PENDING)
        assertEquals("PENDING", serialized)

        val deserialized = converters.toSyncStatus("FAILED")
        assertEquals(SyncStatus.FAILED, deserialized)
    }

    @Test
    fun `SafetyFault list serialization and deserialization`() {
        val originalFaults = listOf(
            SafetyFault(
                type = SafetyFaultType.STRAY_CURRENT_SOIL_FAULT,
                message = "Current fault detected",
                triggeredAtEpochMillis = 1000L
            ),
            SafetyFault(
                type = SafetyFaultType.ROOT_ZONE_THERMAL_SHOCK,
                message = "Soil temperature high",
                triggeredAtEpochMillis = 2000L
            )
        )

        val serialized = converters.fromSafetyFaultList(originalFaults)
        val deserialized = converters.toSafetyFaultList(serialized)

        assertEquals(2, deserialized.size)
        assertEquals(SafetyFaultType.STRAY_CURRENT_SOIL_FAULT, deserialized[0].type)
        assertEquals("Current fault detected", deserialized[0].message)
        assertEquals(1000L, deserialized[0].triggeredAtEpochMillis)

        assertEquals(SafetyFaultType.ROOT_ZONE_THERMAL_SHOCK, deserialized[1].type)
        assertEquals("Soil temperature high", deserialized[1].message)
        assertEquals(2000L, deserialized[1].triggeredAtEpochMillis)
    }

    @Test
    fun `SafetyFault empty or null list handling`() {
        assertEquals("", converters.fromSafetyFaultList(null))
        assertEquals("", converters.fromSafetyFaultList(emptyList()))

        assertTrue(converters.toSafetyFaultList(null).isEmpty())
        assertTrue(converters.toSafetyFaultList("").isEmpty())
    }
}
