package com.jetstream.android.discovery

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class ServerInfo(
    val name: String,
    val host: String,
    val port: Int
)

class DiscoveryRepository {
    private val tag = "DiscoveryRepository"
    val servers: SnapshotStateList<ServerInfo> = mutableStateListOf()

    fun add(server: ServerInfo) {
        if (servers.none { it.name == server.name }) {
            servers.add(server)
        }
    }

    fun remove(name: String) {
        servers.removeAll { it.name == name }
    }

    fun clear() {
        servers.clear()
    }
}