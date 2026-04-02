package com.jetstream.android.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.jetstream.android.Routes
import com.jetstream.android.ui.theme.JetStreamTheme

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeScreenViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState,
        gotoSettings = { navController.navigate(Routes.SETTINGS) },
        onServerIPChange = { viewModel.setServerIP(it) },
        onConnect = { viewModel.connect() },
        onDisconnect = { viewModel.disconnect() },
        onDiscoverStart = { viewModel.startDiscovery() },
        onDiscoverStop = { viewModel.stopDiscovery() }
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeScreenState,
    gotoSettings: () -> Unit,
    onServerIPChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDiscoverStart: () -> Unit,
    onDiscoverStop: () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                uiState.deviceName,
                style = MaterialTheme.typography.headlineLarge
            )

            IconButton(
                onClick = gotoSettings
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                )
            }
        }

        Text(
            text = "Enter server address or discover local server.",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField (
            value = uiState.serverIP,
            onValueChange = onServerIPChange,
            label = { Text("Server Address") },
            placeholder = { Text("eg: 192.168.1.10") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onConnect,
                modifier = Modifier.weight(1f),
                enabled = !uiState.connected && uiState.serverIP.isNotEmpty()
            ) {
                Text("Connect")
            }

            Button(
                onClick = onDisconnect,
                modifier = Modifier.weight(1f),
                enabled = uiState.connected
            ) {
                Text("Disconnect")
            }
        }

        Button(
            onClick = onDiscoverStart
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.padding(2.dp))
            Text("Discover Servers")
        }

        // Discovered server list
        if (uiState.discoveredServers.isEmpty()) {
            Text("No servers found")
        } else {
            uiState.discoveredServers.forEach { server ->
                Card(
                    onClick = {
                        onServerIPChange(server.host)
                        onDiscoverStop()
                        onConnect()
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(server.name, style = MaterialTheme.typography.titleMedium)
                        Text(server.host, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        HorizontalDivider()

        Surface(
            color = if (uiState.connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (uiState.connected) "Status: Connected" else "Status: Disconnected",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.connected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreenContent(
                HomeScreenState(),
                gotoSettings = { },
                onServerIPChange = { },
                onConnect = { },
                onDisconnect = { },
                onDiscoverStart = { },
                onDiscoverStop = { }
            )
        }
    }
}