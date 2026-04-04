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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Clipboard Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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