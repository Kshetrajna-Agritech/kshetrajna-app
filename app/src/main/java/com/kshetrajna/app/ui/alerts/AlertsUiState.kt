package com.kshetrajna.app.ui.alerts

import com.kshetrajna.app.domain.model.AlertsAndSafetyData

data class AlertsUiStateData(
    val data: AlertsAndSafetyData = AlertsAndSafetyData(),
    val actionMessage: String? = null,
    val errorMessage: String? = null
)
