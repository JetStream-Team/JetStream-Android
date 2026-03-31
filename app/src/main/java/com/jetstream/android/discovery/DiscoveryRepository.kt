package com.jetstream.android.discovery

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jetstream.android.service.ServerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DiscoveryRepository {
    private val TAG = "DiscoveryRepository"
    private val _servers = MutableStateFlow<List<ServerInfo>>(emptyList())
    val servers = _servers.asStateFlow()

    fun addServer(server: ServerInfo) {
        _servers.value += server
    }
    fun removeServer(name: String) {
        _servers.value = _servers.value.filter { it.name != name }
    }

    fun clear() {
        _servers.value = emptyList()
    }
}