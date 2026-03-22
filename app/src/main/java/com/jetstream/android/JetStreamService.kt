package com.jetstream.android

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

const val PORT = 8000

interface WSCallback {
    fun onConnected()
    fun onDisconnected()
}

class JetStreamService: Service(), WSCallback {
    private val tag = "JetStreamService"

    inner class LocalBinder : Binder() { fun getService() = this@JetStreamService }
    override fun onBind(intent: Intent) = LocalBinder()

    private var isRunning = false // Whether the service is running

    private val wsClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    var isConnected by mutableStateOf(false) // Whether the websocket is connected

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

    override fun onCreate() {
        super.onCreate()

        Log.d(tag, "JetStreamService created")

        // Create notification channel
        val serviceChannel = NotificationChannel(
            tag,
            "Connectivity Service",
            NotificationManager.IMPORTANCE_LOW
        )

        // Register notification channel with system
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Don't restart service if it's already running
        if (isRunning) { return START_STICKY }

        isRunning = true

        Log.d(tag,  "JetStreamService started")

        // Create notification
        val notification = buildNotification("Disconnected")

        // Start foreground service with the notification
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop WebSocket connection
        webSocket?.close(1000, "Service Stopping")
        webSocket = null

        isRunning = false

        // Shutdown WebSocket client
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

        // Create WebSocket connection
        val request = Request.Builder().url("ws://$serverIP:$PORT/").build()
        webSocket = wsClient.newWebSocket(request, WSListener(this, applicationContext))
    }

    fun wsDisconnect() {
        if (webSocket == null) {
            Log.w(tag, "No WebSocket connection to close")
            return
        }

        // Close WebSocket connection
        webSocket?.close(1000, "Client disconnected")
    }

    fun sendMessage(data: ByteArray): Boolean {
        val ws = webSocket ?: return false
        return ws.send(ByteString.of(*data))
    }
}