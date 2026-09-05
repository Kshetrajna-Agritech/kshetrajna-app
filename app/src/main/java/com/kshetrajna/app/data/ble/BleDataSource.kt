package com.kshetrajna.app.data.ble

import com.kshetrajna.app.core.result.Resource

/**
 * BLE transport data source contract interface.
 * Exact UUIDs and binary framing will be bound when approved firmware contracts are provided.
 */
interface BleDataSource {
    suspend fun connectDevice(address: String): Resource<Unit>
}
