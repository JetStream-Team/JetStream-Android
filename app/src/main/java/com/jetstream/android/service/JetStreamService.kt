package com.jetstream.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jetstream.android.MainActivity
import com.jetstream.android.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString

class JetStreamService : Service() {
    private val TAG = "JetStreamService"
    private val CHANNEL_ID = "jetstream_service"

    private var isRunning = false

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        val notificationManager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "JetStream Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        // Pass itself to JetStreamRepository
        JetStreamRepository.onServiceCreated(this)

        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Do nothing if already running
        if (isRunning) {
            Log.w(TAG, "Already running, ignoring onStartCommand()")
            return START_STICKY
        }

        // If not already running, start the foreground service
        isRunning = true
        startForeground(1, buildNotification("Disconnected"), FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Disconnect from websocket
        wsDisconnect()

        // Destroy client
        client.dispatcher.executorService.shutdown()

        // Clear JetStreamRepository
        JetStreamRepository.onServiceDestroyed()

        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun wsConnect(serverIp: String, port: Int = 8000) {
        // Do nothing if already connected
        if (webSocket != null) {
            Log.w(TAG, "Already connected, ignoring connect()")
            return
        }

        // Build websocket request
        val url = "ws://$serverIp:$port"
        val request = Request.Builder()
            .url(url)
            .build()

        // Try to connect
        Log.d(TAG, "Connecting to $url")
        webSocket = client.newWebSocket(
            request,
            WSListener(serverIp, port, ::updateNotification, ::clearWebSocket)
        )
    }

    fun wsDisconnect() {
        // Do nothing if not connected
        val ws = webSocket ?: run {
            Log.w(TAG, "Not connected, ignoring disconnect()")
            return
        }

        // Try to disconnect
        Log.d(TAG, "Disconnecting")
        ws.close(1000, "User disconnected")
        webSocket = null
    }

    fun clearWebSocket() {
        webSocket = null
    }

    fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(1, buildNotification(text))
    }

    fun send(bytes: ByteArray) {
        val ws = webSocket ?: run {
            Log.w(TAG, "Not connected, ignoring send()")
            return
        }

        Log.d(TAG, "Sending bytes: ${bytes.size}")
        ws.send(bytes.toByteString(0, bytes.size))
    }


    private fun buildNotification(statusText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JetStream")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}