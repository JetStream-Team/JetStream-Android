package com.jetstream.android.discovery

import android.util.Log
import com.jetstream.android.services.jetstream.ServerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DiscoveryRepository {
    private val TAG = "DiscoveryRepository"
    private val _discoveredServers = MutableStateFlow<List<ServerInfo>>(emptyList())
    val discoveredServers = _discoveredServers.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering = _isDiscovering.asStateFlow()

    fun addServer(server: ServerInfo) {
        _discoveredServers.value += server
        Log.d(TAG, "New server added to discovered servers")
    }

    fun removeServer(name: String) {
        _discoveredServers.value = _discoveredServers.value.filter { it.name != name }
        Log.d(TAG, "Server removed from discovered servers")
    }

    fun clearServers() {
        _discoveredServers.value = emptyList()
        Log.d(TAG, "Discovered servers list cleared")
    }

    fun setDiscovering(isDiscovering: Boolean) {
        _isDiscovering.value = isDiscovering
    }
}