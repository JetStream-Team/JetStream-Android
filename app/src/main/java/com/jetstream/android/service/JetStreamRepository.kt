package com.jetstream.android.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConnectionDetails(
    val serverIp: String = "",
    val port: Int = 8000,
    val deviceName: String = ""
)

object JetStreamRepository {

    private const val TAG = "JetStreamRepository"

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _connectionDetails = MutableStateFlow(ConnectionDetails())
    val connectionDetails = _connectionDetails.asStateFlow()

    private var service: JetStreamService? = null

    fun onServiceConnected(jetStreamService: JetStreamService) {
        service = jetStreamService
        Log.d(TAG, "Service reference acquired")
    }

    fun onServiceDisconnected() {
        _connected.value = false
        _connectionDetails.value = ConnectionDetails()
        service = null
        Log.d(TAG, "Service reference cleared")
    }

    fun wsConnect(serverIp: String) {
        Log.d(TAG, "wsConnect called with $serverIp")
        // TODO: real WebSocket connection here
        _connected.value = true
        _connectionDetails.value = ConnectionDetails(serverIp = serverIp, port = 8000)
        service?.updateNotification("Connected to $serverIp")
    }

    fun wsDisconnect() {
        Log.d(TAG, "wsDisconnect called")
        // TODO: real WebSocket disconnect here
        _connected.value = false
        _connectionDetails.value = ConnectionDetails()
        service?.updateNotification("Disconnected")
    }

    fun updateConnectionDetails(details: ConnectionDetails) {
        _connectionDetails.value = details
    }
}