package com.kshetrajna.app.data.local

import com.kshetrajna.app.domain.model.Node
import kotlinx.coroutines.flow.Flow

/**
 * Local persistence data source interface. Primary source for offline-first architecture.
 */
interface LocalDataSource {
    fun getNodes(): Flow<List<Node>>
}
