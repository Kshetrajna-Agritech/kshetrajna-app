package com.kshetrajna.app.domain.model

/**
 * Domain entity representing an agricultural farm.
 */
data class Farm(
    val id: String,
    val name: String,
    val location: String? = null,
    val createdAtEpochMillis: Long
)

/**
 * Domain entity representing a crop profile with optimal target bounds (optional/nullable where unknown).
 * No default/fake threshold values are hardcoded into the constructor.
 */
data class CropProfile(
    val id: String,
    val cropName: String,
    val growthStage: String? = null,
    val targetSoilMoistureMinPercent: Float? = null,
    val targetSoilMoistureMaxPercent: Float? = null,
    val targetPhMin: Float? = null,
    val targetPhMax: Float? = null
)

/**
 * Domain entity representing a field sensor/actuator node.
 */
data class Node(
    val id: String,
    val farmId: String,
    val name: String,
    val hardwareAddress: String? = null,
    val isOnline: Boolean = false,
    val lastSeenEpochMillis: Long? = null
)
