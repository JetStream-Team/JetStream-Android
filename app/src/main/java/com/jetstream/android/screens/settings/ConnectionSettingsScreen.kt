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
fun ConnectionSettingsScreen(navController: NavController) {
    val viewModel: AppPreferencesViewModel = viewModel()
    val autoConnect by viewModel.autoConnect.collectAsStateWithLifecycle()
    ConnectionSettingsContent(
        autoConnect = autoConnect,
        onAutoConnectChange = { viewModel.setAutoConnect(it) },
        onBack = { navController.popBackStack() }
    )
}

@Composable
fun ConnectionSettingsContent(
    autoConnect: Boolean,
    onAutoConnectChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column() {
        StandardPageHeader("Connection Settings", onBack)

        Column(Modifier.fillMaxSize()) {
            SwitchItem("Auto Connect", autoConnect, onAutoConnectChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ConnectionSettingsContent(
                autoConnect = true,
                onAutoConnectChange = {},
                onBack = {}
            )
        }
    }
}