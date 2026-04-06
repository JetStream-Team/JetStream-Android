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
fun ClipboardSettingsScreen(navController: NavController) {
    val viewModel: AppPreferencesViewModel = viewModel()
    val syncClipboard by viewModel.syncClipboard.collectAsStateWithLifecycle()

    ClipboardSettingsContent(
        syncClipboard = syncClipboard,
        onSyncClipboardChange = { viewModel.setSyncClipboard(it) },
        onBack = { navController.popBackStack() }
    )
}

@Composable
fun ClipboardSettingsContent(
    syncClipboard: Boolean,
    onSyncClipboardChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column() {
        StandardPageHeader("Clipboard Settings", onBack)

        Column(Modifier.fillMaxSize()) {
            SwitchItem("Sync Clipboard", syncClipboard, onSyncClipboardChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClipboardScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ClipboardSettingsContent(
                syncClipboard = true,
                onSyncClipboardChange = {},
                onBack = {}
            )
        }
    }
}