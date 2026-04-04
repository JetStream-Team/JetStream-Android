package com.jetstream.android.services.jetstream

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ServerInfo(
    val name: String = "",
    val host: String = "",
    val port: Int = 8000,
)

object JetStreamRepository {
    private const val TAG = "JetStreamRepository"

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _serverInfo = MutableStateFlow(ServerInfo())
    val serverInfo = _serverInfo.asStateFlow()

    private var service: JetStreamService? = null

    fun onServiceCreated(jetStreamService: JetStreamService) {
        service = jetStreamService
        Log.d(TAG, "Service reference acquired")
    }

    fun onServiceDestroyed() {
        _connected.value = false
        _serverInfo.value = ServerInfo()
        service = null
        Log.d(TAG, "Service reference cleared")
    }

    fun onWebSocketConnected(serverIp: String, port: Int) {
        _connected.value = true
    }

    fun onWebSocketDisconnected() {
        _connected.value = false
        _serverInfo.value = ServerInfo()
    }

    fun setServerInfo(serverInfo: ServerInfo) {
        _serverInfo.value = serverInfo
        Log.d(TAG, "Server info updated")
    }

    fun wsConnect(serverIp: String, port: Int = 8000) {
        val service = service ?: run {
            Log.w(TAG, "wsConnect called but service is null")
            return
        }

        service.wsConnect(serverIp, port)
    }

    fun wsDisconnect() {
        val service = service ?: run {
            Log.w(TAG, "wsDisconnect called but service is null")
            return
        }

        service.wsDisconnect()
    }

    fun wsSend(bytes: ByteArray) {
        val service = service ?: run {
            Log.w(TAG, "send called but service is null")
            return
        }

        service.send(bytes)
    }
}