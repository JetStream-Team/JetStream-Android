package com.jetstream.android.screens.home

import android.app.Application
import android.content.Context
import android.content.ClipDescription
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jetstream.android.discovery.DiscoveryRepository
import com.jetstream.android.discovery.JetStreamDiscovery
import com.jetstream.android.proto.Action
import com.jetstream.android.proto.Clipboard
import com.jetstream.android.proto.Lock
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.proto.Poweroff
import com.jetstream.android.proto.Reboot
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
    val isDiscovering: Boolean = false,
    val discoveredServers: List<ServerInfo> = emptyList(),
    val connectionSheetVisible: Boolean = false,
    val powerMenuVisible: Boolean = false
)

class HomeScreenViewModel(app: Application) : AndroidViewModel(app) {
    private val TAG = "HomeScreenViewModel"
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    private val jetStreamDiscovery = JetStreamDiscovery(app)

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
            DiscoveryRepository.isDiscovering.collect { isDiscovering ->
                _uiState.update { it.copy(isDiscovering = isDiscovering) }
            }
        }
        viewModelScope.launch {
            DiscoveryRepository.discoveredServers.collect { servers ->
                _uiState.update { it.copy(discoveredServers = servers) }
            }
        }
    }

    fun setServerIP(newIP: String) {
        _uiState.update { it.copy(serverIP = newIP) }
    }

    fun connect() = JetStreamRepository.wsConnect(_uiState.value.serverIP)

    fun disconnect() = JetStreamRepository.wsDisconnect()

    fun startDiscovery() = jetStreamDiscovery.startDiscovery()

    fun stopDiscovery() = jetStreamDiscovery.stopDiscovery()

    fun showConnectionSheet() {
        _uiState.update { it.copy(connectionSheetVisible = true) }
    }

    fun hideConnectionSheet() {
        _uiState.update { it.copy(connectionSheetVisible = false) }
    }

    fun showPowerMenu() {
        _uiState.update { it.copy(powerMenuVisible = true) }
    }

    fun hidePowerMenu() {
        _uiState.update { it.copy(powerMenuVisible = false) }
    }

    fun sendLockMessage() {
        val wrapper = MessageWrapper(
            action = Action(lock = Lock())
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Lock message sent")
    }

    fun sendPowerOffMessage() {
        val wrapper = MessageWrapper(
            action = Action(poweroff = Poweroff())
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Poweroff message sent")
    }

    fun sendRebootMessage() {
        val wrapper = MessageWrapper(
            action = Action(reboot = Reboot())
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Reboot message sent")
    }

    fun sendClipboard() {
        val application = getApplication<Application>()
        val clipboard = application.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

        if (!clipboard.hasPrimaryClip()) return
        if (clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) != true) return

        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: return

        val wrapper = MessageWrapper(
            clipboard = Clipboard(content = text)
        )
        JetStreamRepository.wsSend(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(TAG, "Clipboard content sent")
    }
}