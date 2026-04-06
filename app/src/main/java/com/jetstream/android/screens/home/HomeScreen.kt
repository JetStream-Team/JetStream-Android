package com.jetstream.android.screens.home

import android.app.Dialog
import android.graphics.drawable.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ContentPasteGo
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jetstream.android.Routes
import com.jetstream.android.ui.theme.JetStreamTheme

data class ActionTile(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

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
        onDiscoverStop = { viewModel.stopDiscovery() },
        showPowerMenu = { viewModel.showPowerMenu() },
        hidePowerMenu = { viewModel.hidePowerMenu() },
        sendLockMessage = { viewModel.sendLockMessage() },
        sendPowerOffMessage = { viewModel.sendPowerOffMessage() },
        sendRebootMessage = { viewModel.sendRebootMessage() },
        sendClipboard = { viewModel.sendClipboard() },
        gotoPresentationRemote = { navController.navigate(Routes.HOME_PRESENTATION)}
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
    showPowerMenu: () -> Unit,
    hidePowerMenu: () -> Unit,
    sendLockMessage: () -> Unit,
    sendPowerOffMessage: () -> Unit,
    sendRebootMessage: () -> Unit,
    sendClipboard: () -> Unit,
    gotoPresentationRemote: () -> Unit
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

        if (uiState.connected) {
            ActionTileGrid(
                uiState,
                showPowerMenu,
                hidePowerMenu,
                sendLockMessage,
                sendPowerOffMessage,
                sendRebootMessage,
                sendClipboard,
                gotoPresentationRemote
            )
        } else {
            Text(
                "No Actions Available - Connect to a server",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                style = MaterialTheme.typography.bodyLarge,
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

@Composable
fun ActionTileGrid(
    uiState: HomeScreenState,
    showPowerMenu: () -> Unit,
    hidePowerMenu: () -> Unit,
    sendLockMessage: () -> Unit,
    sendPowerOffMessage: () -> Unit,
    sendRebootMessage: () -> Unit,
    sendClipboard: () -> Unit,
    gotoPresentationRemote: () -> Unit
) {
    val actionTiles = listOf(
        ActionTile("PowerMenu", Icons.Rounded.PowerSettingsNew, showPowerMenu),
        ActionTile("Send Clipboard", Icons.Rounded.ContentPasteGo, sendClipboard),
        ActionTile("Presentation Remote", Icons.Rounded.SettingsRemote, gotoPresentationRemote)
    )
    val actionTilesChunked = actionTiles.chunked(2)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actionTilesChunked.forEach { actionTilesRow ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actionTilesRow.forEach { actionTile ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        onClick = actionTile.onClick,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(18.dp)
                                .fillMaxSize()
                        ) {
                            Icon(
                                imageVector = actionTile.icon,
                                contentDescription = actionTile.label,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .size(28.dp),
                            )
                            Text(
                                actionTile.label,
                                modifier = Modifier.align(Alignment.BottomStart)
                            )
                        }
                    }

                    if (actionTilesRow.count() < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (uiState.powerMenuVisible) {
        PowerMenu(
            hidePowerMenu,
            sendLockMessage,
            sendPowerOffMessage,
            sendRebootMessage
        )
    }
}

@Composable
fun PowerMenu(
    hidePowerMenu: () -> Unit,
    sendLockMessage: () -> Unit,
    sendPowerOffMessage: () -> Unit,
    sendRebootMessage: () -> Unit
) {
    val buttonHeight = 50.dp
    val lowRounding = 4.dp
    val highRounding = 18.dp

    AlertDialog(
        onDismissRequest = hidePowerMenu,
        title = { Text("Power Menu") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = { sendLockMessage(); hidePowerMenu() },
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                    shape = RoundedCornerShape(highRounding, highRounding, lowRounding, lowRounding)
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Lock")
                }
                FilledTonalButton(
                    onClick = { sendPowerOffMessage(); hidePowerMenu() },
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                    shape = RoundedCornerShape(lowRounding)
                ) {
                    Icon(Icons.Rounded.PowerSettingsNew, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Power Off")
                }
                FilledTonalButton(
                    onClick = { sendRebootMessage(); hidePowerMenu() },
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                    shape = RoundedCornerShape(lowRounding, lowRounding, highRounding, highRounding)
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Reboot")
                }
            }
        },
        confirmButton = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreenContent(
                HomeScreenState(connected = true),
                gotoSettings = { },
                showConnectionSheet = { },
                hideConnectionSheet = { },
                onServerIPChange = { },
                onConnect = { },
                onDisconnect = { },
                onDiscoverStart = { },
                onDiscoverStop = { },
                showPowerMenu = { },
                hidePowerMenu = { },
                sendLockMessage = { },
                sendPowerOffMessage = { },
                sendRebootMessage = { },
                sendClipboard = { },
                gotoPresentationRemote = { }
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

@Preview(showBackground = true)
@Composable
fun PowerMenuPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PowerMenu(
                hidePowerMenu = { },
                sendLockMessage = { },
                sendPowerOffMessage = { },
                sendRebootMessage = { }
            )
        }
    }
}