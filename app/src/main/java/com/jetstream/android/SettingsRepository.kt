package com.jetstream.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Top-level extension — creates a single DataStore instance per process
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jetstream_settings")

data class AppSettings(
    val allowNotifications: Boolean = true,
    val allowClipboard: Boolean = true,
)

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_ALLOW_NOTIFICATIONS = booleanPreferencesKey("allow_notifications")
        private val KEY_ALLOW_CLIPBOARD     = booleanPreferencesKey("allow_clipboard")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            allowNotifications = prefs[KEY_ALLOW_NOTIFICATIONS] ?: true,
            allowClipboard     = prefs[KEY_ALLOW_CLIPBOARD]     ?: true,
        )
    }

    suspend fun setAllowNotifications(value: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_NOTIFICATIONS] = value }
    }

    suspend fun setAllowClipboard(value: Boolean) {
        context.dataStore.edit { it[KEY_ALLOW_CLIPBOARD] = value }
    }
}