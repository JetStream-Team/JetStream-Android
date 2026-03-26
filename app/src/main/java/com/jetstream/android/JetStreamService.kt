package com.jetstream.android

import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

const val PORT = 8000

interface WSCallback {
    fun onConnected()
    fun onDisconnected()
    fun onClipboardReceived(content: String)
}

class JetStreamService : Service(), WSCallback {
    private val tag = "JetStreamService"

    inner class LocalBinder : Binder() { fun getService() = this@JetStreamService }
    override fun onBind(intent: Intent) = LocalBinder()

    private var isRunning = false

    private val wsClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    var isConnected by mutableStateOf(false)

    // Coroutine scope for async settings checks
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onConnected() {
        webSocket?.let { isConnected = true }
        getSystemService(NotificationManager::class.java)
            .notify(1, buildNotification("Connected"))
    }

    override fun onDisconnected() {
        webSocket = null
        isConnected = false
        getSystemService(NotificationManager::class.java)
            .notify(1, buildNotification("Disconnected"))
    }

    override fun onClipboardReceived(content: String) {
        serviceScope.launch {
            val settingsRepo = SettingsRepository(applicationContext)
            val allowed = settingsRepo.settingsFlow.first().allowClipboard
            if (!allowed) {
                Log.d(tag, "Clipboard sync disabled — dropping clipboard message")
                return@launch
            }

            // Must run on the main thread to access ClipboardManager
            launch(Dispatchers.Main) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("JetStream", content))
                Log.d(tag, "Clipboard synced from desktop")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "JetStreamService created")

        val serviceChannel = NotificationChannel(
            tag,
            "Connectivity Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_STICKY
        isRunning = true
        Log.d(tag, "JetStreamService started")

        val notification = buildNotification("Disconnected")
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Service Stopping")
        webSocket = null
        isRunning = false
        serviceScope.cancel()
        wsClient.dispatcher.executorService.shutdown()
        wsClient.connectionPool.evictAll()
        Log.d(tag, "JetStreamService destroyed")
    }

    fun buildNotification(description: String): Notification {
        return NotificationCompat.Builder(this, tag)
            .setContentTitle("JetStream")
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .build()
    }

    fun wsConnect(serverIP: String) {
        if (webSocket != null) {
            Log.w(tag, "WebSocket already connected")
            return
        }
        val request = Request.Builder().url("ws://$serverIP:$PORT/").build()
        webSocket = wsClient.newWebSocket(request, WSListener(this, applicationContext))
    }

    fun wsDisconnect() {
        if (webSocket == null) {
            Log.w(tag, "No WebSocket connection to close")
            return
        }
        webSocket?.close(1000, "Client disconnected")
    }

    fun sendMessage(data: ByteArray): Boolean {
        val ws = webSocket ?: return false
        return ws.send(ByteString.of(*data))
    }
}