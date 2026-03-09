package com.jetstream.android

import android.app.*
import android.content.ClipDescription
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat

class JetStreamService: Service() {

    // Binder setup
    inner class LocalBinder : Binder() { fun getService() = this@JetStreamService }
    override fun onBind(intent: Intent) = LocalBinder()

    private val CHANNEL_ID = "JetStreamService"

    val connected = mutableStateOf(false)

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Connectivity Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    fun wsConnect() {
        println("Connected")
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, buildNotification("Connected"))
        connected.value = true
    }

    fun wsDisconnect() {
        println("Disconnected")
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, buildNotification("Connected"))
        connected.value = false
    }
}