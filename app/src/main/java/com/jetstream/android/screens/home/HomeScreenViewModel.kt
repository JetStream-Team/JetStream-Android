package com.jetstream.android.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetstream.android.service.JetStreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeScreenState(
    val deviceName: String = "Mathew's G45",
    val serverIP: String = "",
    val connected: Boolean = false,
)

class HomeScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            JetStreamRepository.connected.collect { connected ->
                _uiState.update { it.copy(connected = connected) }
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
}