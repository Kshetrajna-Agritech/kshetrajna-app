package com.kshetrajna.app.core.simulation

/**
 * Controllable simulation clock providing deterministic timestamps for simulation and testing.
 */
class SimulationClock(
    initialEpochMillis: Long = 1_700_000_000_000L
) {
    private var currentMillis: Long = initialEpochMillis

    fun currentTimeEpochMillis(): Long = currentMillis

    fun advanceTimeBy(millis: Long) {
        require(millis >= 0) { "Cannot rewind time with negative delta ($millis)" }
        currentMillis += millis
    }

    fun setTime(epochMillis: Long) {
        currentMillis = epochMillis
    }
}
