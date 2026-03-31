package com.jetstream.android.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetstream.android.discovery.DiscoveryRepository
import com.jetstream.android.discovery.JetStreamDiscovery
import com.jetstream.android.service.JetStreamRepository
import com.jetstream.android.service.ServerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeScreenState(
    val deviceName: String = "Mathew's G45",
    val serverIP: String = "",
    val connected: Boolean = false,
    val serverInfo: ServerInfo = ServerInfo(),
    val discoveredServers: List<ServerInfo> = emptyList()
)

class HomeScreenViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    private val discovery = JetStreamDiscovery(app)

    init {
        viewModelScope.launch {
            JetStreamRepository.connected.collect { connected ->
                _uiState.update { it.copy(connected = connected) }
            }
        }
        viewModelScope.launch {
            JetStreamRepository.serverInfo.collect { serverInfo ->
                _uiState.update { it.copy(serverInfo = serverInfo) }
            }
        }
        viewModelScope.launch {
            DiscoveryRepository.servers.collect { servers ->
                _uiState.update { it.copy(discoveredServers = servers) }
            }
        }
    }

    fun setServerIP(newIP: String) {
        _uiState.update { it.copy(serverIP = newIP) }
    }

    fun connect() {
        JetStreamRepository.wsConnect(_uiState.value.serverIP)
    }

    fun disconnect() {
        JetStreamRepository.wsDisconnect()
    }

    fun startDiscovery() = discovery.start()

    fun stopDiscovery() = discovery.stop()
}