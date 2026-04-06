package com.jetstream.android.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jetstream.android.ui.theme.JetStreamTheme

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val viewModel: AppPreferencesViewModel = viewModel()
    val syncNotifications by viewModel.syncNotifications.collectAsStateWithLifecycle()
    val respectDnd by viewModel.respectDnd.collectAsStateWithLifecycle()
    NotificationSettingsContent(
        syncNotifications = syncNotifications,
        respectDnd = respectDnd,
        onSyncNotificationsChange = { viewModel.setSyncNotifications(it) },
        onRespectDndChange = { viewModel.setRespectDnd(it) },
        onBack = { navController.popBackStack() }
    )
}

@Composable
fun NotificationSettingsContent(
    syncNotifications: Boolean,
    respectDnd: Boolean,
    onSyncNotificationsChange: (Boolean) -> Unit,
    onRespectDndChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column() {
        StandardPageHeader("Notification Settings", onBack)

        Column(Modifier.fillMaxSize()) {
            SwitchItem("Sync Notifications", syncNotifications, onSyncNotificationsChange)
            SwitchItem("Respect Do Not Disturb", respectDnd, onRespectDndChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NotificationSettingsContent(
                syncNotifications = true,
                respectDnd = false,
                onSyncNotificationsChange = {},
                onRespectDndChange = {},
                onBack = {}
            )
        }
    }
}