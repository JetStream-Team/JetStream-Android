package com.jetstream.android.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppPreferences(private val context: Context) {

    companion object {
        // Connection
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        // Clipboard
        val SYNC_CLIPBOARD = booleanPreferencesKey("sync_clipboard")
        // Notifications
        val SYNC_NOTIFICATIONS = booleanPreferencesKey("sync_notifications")
        val RESPECT_DND = booleanPreferencesKey("respect_dnd")
    }

    // Connection
    val autoConnect: Flow<Boolean> = context.dataStore.data.map { it[AUTO_CONNECT] ?: true }
    suspend fun setAutoConnect(v: Boolean) = context.dataStore.edit { it[AUTO_CONNECT] = v }

    // Clipboard
    val syncClipboard: Flow<Boolean> = context.dataStore.data.map { it[SYNC_CLIPBOARD] ?: true }
    suspend fun setSyncClipboard(v: Boolean) = context.dataStore.edit { it[SYNC_CLIPBOARD] = v }

    // Notifications
    val syncNotifications: Flow<Boolean> = context.dataStore.data.map { it[SYNC_NOTIFICATIONS] ?: true }
    suspend fun setSyncNotifications(v: Boolean) = context.dataStore.edit { it[SYNC_NOTIFICATIONS] = v }

    val respectDnd: Flow<Boolean> = context.dataStore.data.map { it[RESPECT_DND] ?: false }
    suspend fun setRespectDnd(v: Boolean) = context.dataStore.edit { it[RESPECT_DND] = v }
}