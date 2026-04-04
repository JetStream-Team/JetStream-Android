package com.jetstream.android.services.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.screens.settings.AppPreferences
import com.jetstream.android.services.jetstream.JetStreamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream

class NotificationListener : NotificationListenerService() {
    private val TAG = "NotificationListener"

    private var syncNotifications = true
    private var respectDnd = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Companion objects are static i.e. shared across instances
    companion object {
        private val EXCLUDE_PACKAGES = setOf(
            "",
            "com.drnoob.datamonitor"
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        val prefs = AppPreferences(applicationContext)
        serviceScope.launch {
            prefs.syncNotifications.collect { syncNotifications = it }
        }
        serviceScope.launch {
            prefs.respectDnd.collect { respectDnd = it }
        }
        Log.d(TAG, "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    private fun filterNotification(sbn: StatusBarNotification): Boolean {

        // Skip if notification sync is disabled
        if (!syncNotifications) return true

        // Filter out if the service is not running or connected
        if (!JetStreamRepository.connected.value) return true

        // Skip notifications posted by JetStream itself to avoid loops
        if (sbn.packageName == packageName) return true

        // Skip if DND is active and the user wants to respect it
        if (respectDnd) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) return true
        }

        // Skip spammy applications
        if (sbn.packageName in EXCLUDE_PACKAGES) return true

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val body  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Skip notifications without any content or title
        if (title.isEmpty() && body.isEmpty()) return true

        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Filter out certain notifications
        if (filterNotification(sbn)) return

        Log.d(TAG, "Notification posted: ${sbn.packageName}")

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val body  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val iconBytes = sbn.notification.smallIcon
            ?.loadDrawable(this)
            ?.toBitmap(width = 96, height = 96)
            ?.let { bitmap ->
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray().toByteString()
                }
            } ?: ByteString.EMPTY

        val wrapper = MessageWrapper(
            notification = com.jetstream.android.proto.Notification(
                create = true,
                id = sbn.id,
                title = title,
                body = body,
                app = sbn.packageName,
                icon = iconBytes
            )
        )
        val bytes = MessageWrapper.ADAPTER.encode(wrapper)
        JetStreamRepository.wsSend(bytes)
        Log.d(TAG, "Notification forwarded: ${sbn.packageName}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification removed: ${sbn.packageName}")

        // Filter out certain notifications
        if (filterNotification(sbn)) return

        val wrapper = MessageWrapper(
            notification = com.jetstream.android.proto.Notification(
                create = false,
                id = sbn.id,
                title = "",
                body = ""
            )
        )
        val bytes = MessageWrapper.ADAPTER.encode(wrapper)
        JetStreamRepository.wsSend(bytes)
        Log.d(TAG, "Notification removal forwarded: ${sbn.packageName}")
    }
}