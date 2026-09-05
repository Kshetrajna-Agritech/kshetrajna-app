package com.kshetrajna.app.domain.model

/**
 * Synchronization tracking record for offline-first persistence and backend sync.
 */
data class SyncRecord(
    val id: String,
    val entityType: String,
    val entityId: String,
    val syncStatus: SyncStatus,
    val createdAtEpochMillis: Long,
    val syncedAtEpochMillis: Long? = null,
    val retryCount: Int = 0,
    val lastErrorMessage: String? = null
) {
    init {
        require(retryCount >= 0) {
            "SyncRecord retry count ($retryCount) cannot be negative"
        }
    }
}
