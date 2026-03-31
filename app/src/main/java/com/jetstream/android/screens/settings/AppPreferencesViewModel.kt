package com.jetstream.android.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jetstream.android.screens.settings.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppPreferencesViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPreferences(app)

    val autoConnect = prefs.autoConnect.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val syncClipboard = prefs.syncClipboard.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val syncNotifications = prefs.syncNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val respectDnd = prefs.respectDnd.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setAutoConnect(v: Boolean) = viewModelScope.launch { prefs.setAutoConnect(v) }
    fun setSyncClipboard(v: Boolean) = viewModelScope.launch { prefs.setSyncClipboard(v) }
    fun setSyncNotifications(v: Boolean) = viewModelScope.launch { prefs.setSyncNotifications(v) }
    fun setRespectDnd(v: Boolean) = viewModelScope.launch { prefs.setRespectDnd(v) }
}