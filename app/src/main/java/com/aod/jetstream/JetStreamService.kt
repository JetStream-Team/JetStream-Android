package com.aod.jetstream

import android.app.*
import android.content.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import com.aod.jetstream.Jetstream.MessageWrapper
import com.aod.jetstream.Jetstream.Clipboard


class JetStreamService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var session: DefaultClientWebSocketSession? = null

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
        engine {
            config {
                sslSocketFactory(SSLSocketFactoryBuilder.createTrustAll(), SSLSocketFactoryBuilder.TrustAllManager)
                hostnameVerifier { _, _ -> true }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        // TODO: Replace with your PC's IP address!
        connectToPc("192.168.154.41")
        setupClipboardListener()
        return START_STICKY
    }

    private fun setupClipboardListener() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                if (text.isNotEmpty()) {
                    sendClipboardToPc(text)
                }
            }
        }
    }

    private fun connectToPc(host: String) {
        serviceScope.launch {
            try {
                // Using port 8080 as per typical Rust websocket defaults
                client.webSocket(method = HttpMethod.Get, host = host, port = 8000, path = "/") {
                    session = this
                    for (frame in incoming) { /* Listening for PC replies */ }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendClipboardToPc(text: String) {
        serviceScope.launch {
            val clip = Clipboard.newBuilder().setContent(text).build()
            val wrapper = MessageWrapper.newBuilder().setClipboard(clip).build()
            session?.send(wrapper.toByteArray())
        }
    }

    private fun createNotification(): Notification {
        val channelId = "jetstream_sync"
        val channel = NotificationChannel(channelId, "JetStream Active", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("JetStream Connected")
            .setContentText("Monitoring clipboard...")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}