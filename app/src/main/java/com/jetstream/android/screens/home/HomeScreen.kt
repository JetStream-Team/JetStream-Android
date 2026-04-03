package com.jetstream.android.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jetstream.android.Routes
import com.jetstream.android.ui.theme.JetStreamTheme

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeScreenViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState,
        gotoSettings = { navController.navigate(Routes.SETTINGS) },
        showConnectionSheet = { viewModel.showConnectionSheet() },
        hideConnectionSheet = { viewModel.hideConnectionSheet() },
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
    showConnectionSheet: () -> Unit,
    hideConnectionSheet: () -> Unit,
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
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(
                onClick = gotoSettings
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                )
            }
        }

        StatusTile(
            uiState,
            showConnectionSheet
        )

        if (uiState.connectionSheetVisible) {
            ConnectionSheet(
                uiState,
                hideConnectionSheet,
                onServerIPChange,
                onConnect,
                onDisconnect,
                onDiscoverStart,
                onDiscoverStop
            )
        }

    }
}

@Composable
fun StatusTile(
    uiState: HomeScreenState,
    showConnectionSheet: () -> Unit
) {
    val statusColor = if (uiState.connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        border = BorderStroke(width = 8.dp, color = statusColor),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        onClick = showConnectionSheet
    ) {
        Box(modifier = Modifier.padding(22.dp)) {
            if (uiState.connected) {
                Text(
                    """Connected to ${uiState.serverInfo.host}
                            |${uiState.serverInfo.name}
                        """.trimMargin(),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    """Disconnected
                           |Click to Connect
                        """.trimMargin(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSheet(
    uiState: HomeScreenState,
    hideConnectionSheet: () -> Unit,
    onServerIPChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDiscoverStart: () -> Unit,
    onDiscoverStop: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = hideConnectionSheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),

        ) {

            // Server discovering
            Text(
                text = "Discover your server",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Spacer(Modifier.padding(8.dp))
                if (uiState.isDiscovering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Discovered server list
            if (uiState.discoveredServers.isNotEmpty()) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "or",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }


            // Manual IP entering field
            Text(
                text = "Enter server address",
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

    //        HorizontalDivider()
    //
    //        Surface(
    //            color = if (uiState.connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    //            shape = RoundedCornerShape(8.dp)
    //        ) {
    //            Text(
    //                text = if (uiState.connected) "Status: Connected" else "Status: Disconnected",
    //                modifier = Modifier.padding(8.dp),
    //                style = MaterialTheme.typography.bodyMedium,
    //                color = if (uiState.connected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
    //            )
    //        }
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
                showConnectionSheet = { },
                hideConnectionSheet = { },
                onServerIPChange = { },
                onConnect = { },
                onDisconnect = { },
                onDiscoverStart = { },
                onDiscoverStop = { }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionSheetPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ConnectionSheet(
                HomeScreenState(),
                hideConnectionSheet = { },
                onServerIPChange = { },
                onConnect = { },
                onDisconnect = { },
                onDiscoverStart = { },
                onDiscoverStop = { }
            )
        }
    }
}