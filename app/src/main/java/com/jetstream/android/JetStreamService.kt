package com.jetstream.android

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

class JetStreamService: Service() {
    // Binder setup
    inner class LocalBinder : Binder() { fun getService() = this@JetStreamService }
    override fun onBind(intent: Intent) = LocalBinder()

    private val CHANNEL_ID = "JetStreamService"
    private var isRunning = false

    private val wsClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    fun suicide() { webSocket = null }

    var isConnected by mutableStateOf(false)
    fun setStatus(newStatus: Boolean) {
        isConnected = newStatus
        if (newStatus) {
            getSystemService(NotificationManager::class.java)
                .notify(1, buildNotification("Connected"))
        } else {
            getSystemService(NotificationManager::class.java)
                .notify(1, buildNotification("Disconnected"))
        }
    }

    override fun onCreate() {
        super.onCreate()

        println("JetStream Service created")

        // Create notification channel
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Connectivity Service",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (isRunning) {
            return START_STICKY
        }

        println("JetStream Service started")

        // Create notification
        val notification = buildNotification("Disconnected")

        // Start foreground service with the notification
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        return START_STICKY
    }

    fun buildNotification(description: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JetStream")
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .build()
    }

    fun wsConnect(serverIP: String) {
        if (webSocket != null) {
            println("WebSocket already connected")
            return
        }

        val request = Request.Builder().url("ws://$serverIP:8000/").build()

        webSocket = wsClient.newWebSocket(request, WSListener(::setStatus, ::suicide))
    }

    fun wsDisconnect() {
        if (webSocket == null) {
            println("No WebSocket connection to close")
            return
        }

        webSocket?.close(0, "Client disconnected")
    }

    fun sendMessage(data: String): Boolean {
        val ws = webSocket ?: return false
        return ws.send(data)
    }

    fun sendMessage(data: ByteString): Boolean {
        val ws = webSocket ?: return false
        return ws.send(data)
    }

    fun sendMessage(data: ByteArray): Boolean {
        return sendMessage(ByteString.of(*data))
    }
}