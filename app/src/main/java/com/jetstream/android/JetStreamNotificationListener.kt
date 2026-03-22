package com.jetstream.android

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.jetstream.android.proto.MessageWrapper
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import com.jetstream.android.proto.Notification as JetStreamNotification

class JetStreamNotificationListener : NotificationListenerService() {
    private val tag = "JetStreamNotificationListener"

    // Companion objects are static i.e. shared across instances
    companion object {
        private val EXCLUDE_PACKAGES = setOf(
            "",
            "com.drnoob.datamonitor"
        )
    }

    private var jetStreamService: JetStreamService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            jetStreamService = (binder as JetStreamService.LocalBinder).getService()
            isBound = true
            Log.d(tag, "Bound to JetStreamService")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            jetStreamService = null
            isBound = false
            Log.d(tag, "Unbound from JetStreamService")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Intent(this, JetStreamService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        Log.d(tag, "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            Log.d(tag, "Listener disconnected")
        }
    }

    private fun filterNotification(sbn: StatusBarNotification): Boolean {

        // Filter out if the service is not running or connected
        val service = jetStreamService ?: return true
        if (!service.isConnected) return true

        // Skip notifications posted by JetStream itself to avoid loops
        if (sbn.packageName == packageName) return true

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

        Log.d(tag, "Notification posted: ${sbn.packageName}")

        val service = jetStreamService ?: return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val body  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val iconBytes = sbn.notification.smallIcon
            ?.loadDrawable(this)
            ?.toBitmap(width = 48, height = 48)
            ?.let { bitmap ->
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray().toByteString()
                }
            } ?: ByteString.EMPTY

        val wrapper = MessageWrapper(
            notification = JetStreamNotification(
                create = true,
                id = sbn.id,
                title = title,
                body = body,
                icon = iconBytes
            )
        )
        val bytes = MessageWrapper.ADAPTER.encode(wrapper)
        service.sendMessage(bytes)
        Log.d(tag, "Notification forwarded: ${sbn.packageName}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(tag, "Notification removed: ${sbn.packageName}")

        // Filter out certain notifications
        if (filterNotification(sbn)) return

        val service = jetStreamService ?: return

        val wrapper = MessageWrapper(
            notification = JetStreamNotification(
                create = false,
                id = sbn.id,
                title = "",
                body = ""
            )
        )
        val bytes = MessageWrapper.ADAPTER.encode(wrapper)
        service.sendMessage(bytes)
        Log.d(tag, "Notification removal forwarded: ${sbn.packageName}")
    }
}