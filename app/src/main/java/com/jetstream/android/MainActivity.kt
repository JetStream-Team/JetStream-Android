package com.jetstream.android

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jetstream.android.ui.theme.JetStreamTheme
import kotlin.getValue
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private var jetStreamService: JetStreamService? by mutableStateOf(null)
    private var isBound = false

    // Private connection object to store the connection to the service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            jetStreamService = (service as JetStreamService.LocalBinder).getService()
            isBound = true
            Log.d(tag, "Bound to JetStreamService")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            jetStreamService = null
            isBound = false
            Log.d(tag, "Unbound from JetStreamService")
        }
    }

    // Bind to the service on start
    override fun onStart() {
        super.onStart()
        bindService(Intent(this, JetStreamService::class.java), connection, BIND_AUTO_CREATE)
    }

    // Unbind from the service on stop
    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels()

        // Start the service at startup
        ContextCompat.startForegroundService(this, Intent(this, JetStreamService::class.java))

        enableEdgeToEdge()
        setContent {
            JetStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        jetStreamService,
                        viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    fgService: JetStreamService? = null,
    viewModel: MainViewModel = MainViewModel()
) {

    val state = viewModel.uiState.collectAsState().value

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(state.scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Welcome to JetStream",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Enter server address or discover local server.",
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField (
            value = state.serverIP,
            onValueChange = { viewModel.setServerIP(it) },
            label = { Text("Server Address") },
            placeholder = { Text("eg: 192.168.1.10") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    fgService?.wsConnect(state.serverIP)
                },
                modifier = Modifier.weight(1f),
                enabled = fgService?.isConnected == false && state.serverIP.isNotEmpty()
            ) {
                Text("Connect")
            }

            Button(
                onClick = {
                    fgService?.wsDisconnect()
                },
                modifier = Modifier.weight(1f),
                enabled = fgService?.isConnected == true
            ) {
                Text("Disconnect")
            }
        }

        Button(
            onClick = {
                fgService?.startDiscovery()
            }
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
        val servers = fgService?.discoveredServers ?: emptyList()
        if (servers.isEmpty()) {
            Text("No servers found")
        } else {
            servers.forEach { server ->
                Card(
                    onClick = {
                        viewModel.setServerIP(server.host)
                        fgService?.stopDiscovery()
                        fgService?.wsConnect(server.host)
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

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Surface(
            color = if (fgService?.isConnected == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (fgService?.isConnected == true) "Status: Connected" else "Status: Disconnected",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (fgService?.isConnected == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
            )
        }

        if (fgService?.serverInfo != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = """
                            Name: ${fgService.serverInfo?.name}
                            Host: ${fgService.serverInfo?.host}
                            Port: ${fgService.serverInfo?.port}
                        """.trimIndent(),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    JetStreamTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MainScreen()
        }
    }
}
