package com.kshetrajna.app.ui.fertility

import com.kshetrajna.app.domain.model.FertilityData

data class FertilityUiStateData(
    val fertilityData: FertilityData = FertilityData(),
    val isCalculating: Boolean = false,
    val calculationError: String? = null
)
