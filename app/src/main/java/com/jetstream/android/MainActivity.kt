package com.jetstream.android

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jetstream.android.ui.theme.JetStreamTheme

class MainActivity : ComponentActivity() {
    private var runningService by mutableStateOf<JetStreamService?>(null)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            runningService = (service as JetStreamService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            runningService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start service as soon as the app is opened
        val intent = Intent(this, JetStreamService::class.java)
        startForegroundService(intent)

        bindService(Intent(this, JetStreamService::class.java), connection, Context.BIND_AUTO_CREATE)

        enableEdgeToEdge()
        setContent {
            JetStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), runningService)
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, runningService: JetStreamService? = null) {
    var text by remember { mutableStateOf("") }

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
            value = text,
            onValueChange = { text = it },
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
                    runningService?.wsConnect()
                },
                modifier = Modifier.weight(1f),
                enabled = text.isNotEmpty()
            ) {
                Text("Connect")
            }

            Button(
                onClick = {
                    runningService?.wsDisconnect()
                },
                modifier = Modifier.weight(1f),
                enabled = text.isNotEmpty()
            ) {
                Text("Disconnect")
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
