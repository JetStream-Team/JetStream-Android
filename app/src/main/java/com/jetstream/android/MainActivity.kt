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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jetstream.android.ui.theme.JetStreamTheme

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private var jetStreamService: JetStreamService? by mutableStateOf(null)
    private var isBound = false

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

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, JetStreamService::class.java), connection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextCompat.startForegroundService(this, Intent(this, JetStreamService::class.java))
        enableEdgeToEdge()
        setContent {
            JetStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        fgService = jetStreamService
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier, fgService: JetStreamService? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable("main") {
            MainScreen(
                fgService = fgService,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    fgService: JetStreamService? = null,
    onNavigateToSettings: () -> Unit = {},
) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome to JetStream",
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        }

        Text(
            text = "Enter server address.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
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
                onClick = { fgService?.wsConnect(serverIP) },
                modifier = Modifier.weight(1f),
                enabled = fgService?.isConnected == false && serverIP.isNotEmpty()
            ) {
                Text("Connect")
            }

            Button(
                onClick = { fgService?.wsDisconnect() },
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
            color = if (fgService?.isConnected == true) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (fgService?.isConnected == true) "Status: Connected"
                else "Status: Disconnected",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (fgService?.isConnected == true) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onError
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