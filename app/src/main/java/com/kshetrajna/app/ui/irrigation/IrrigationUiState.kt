package com.kshetrajna.app.ui.irrigation

import com.kshetrajna.app.domain.model.IrrigationData

data class IrrigationUiStateData(
    val irrigationData: IrrigationData = IrrigationData(),
    val isSendingCommand: Boolean = false,
    val commandActionMessage: String? = null,
    val commandErrorMessage: String? = null
)
