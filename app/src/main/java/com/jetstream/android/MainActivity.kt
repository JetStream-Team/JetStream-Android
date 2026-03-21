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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jetstream.android.ui.theme.JetStreamTheme

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

        // Start the service at startup
        ContextCompat.startForegroundService(this, Intent(this, JetStreamService::class.java))

        enableEdgeToEdge()
        setContent {
            JetStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), jetStreamService)
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, fgService: JetStreamService? = null) {
    var serverIP by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Welcome to JetStream",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Enter server address.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField (
            value = serverIP,
            onValueChange = { serverIP = it },
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
                    fgService?.wsConnect(serverIP)
                },
                modifier = Modifier.weight(1f),
                enabled = fgService?.isConnected == false && serverIP.isNotEmpty()
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

        OutlinedButton(
            onClick = {
                serverIP = "192.168.1.10"
                fgService?.wsConnect(serverIP)
            },
        ) {
            Text("Connect to 192.168.1.10")
        }

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
