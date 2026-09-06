package com.kshetrajna.app.ui.manualph

import com.kshetrajna.app.domain.model.ManualPH

/**
 * Data holder for Manual pH screen state.
 */
data class ManualPhUiStateData(
    val nodeName: String = "Field Zone 1 Node",
    val entries: List<ManualPH> = emptyList(),
    val latestEntry: ManualPH? = null,
    val isSaving: Boolean = false,
    val inputPhText: String = "",
    val inputNotesText: String = "",
    val validationError: String? = null,
    val saveSuccessMessage: String? = null,
)
