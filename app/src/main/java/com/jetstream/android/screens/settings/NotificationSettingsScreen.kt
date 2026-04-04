package com.jetstream.android.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
        SettingsPageHeader("Notification Settings", onBack)

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