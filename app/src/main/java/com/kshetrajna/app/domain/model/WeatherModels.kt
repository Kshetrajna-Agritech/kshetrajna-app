package com.kshetrajna.app.domain.model

/**
 * External/forecast atmospheric and weather data.
 */
data class WeatherData(
    val id: String,
    val farmId: String? = null,
    val retrievedAtEpochMillis: Long,
    val rainfallMm: Float? = null,
    val temperatureCelsius: Float? = null,
    val humidityPercent: Float? = null,
    val conditionText: String? = null,
    val sourceName: String = "External Forecast",
    val isCached: Boolean = true,
    val category: MeasurementCategory = MeasurementCategory.EXTERNAL_FORECAST
)
