package com.jetstream.android

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.proto.Notification as JetStreamNotification

val exclude_packages = arrayOf(
    "",
    "com.drnoob.datamonitor"
)

class JetStreamNotificationListener : NotificationListenerService() {

    private var jetStreamService: JetStreamService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            jetStreamService = (binder as JetStreamService.LocalBinder).getService()
            isBound = true
            Log.d("JetStreamNotificationListener", "Bound to JetStreamService")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            jetStreamService = null
            isBound = false
            Log.d("JetStreamNotificationListener", "Unbound from JetStreamService")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Intent(this, JetStreamService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        Log.d("JetStreamNotificationListener", "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            Log.d("JetStreamNotificationListener", "Listener disconnected")
        }
    }

    fun filterNotification(sbn: StatusBarNotification): Boolean {

        // Filter out if the service is not running or connected
        val service = jetStreamService ?: return true
        if (!service.isConnected) return true

        // Skip notifications posted by JetStream itself to avoid loops
        if (sbn.packageName == packageName) return true

        // Skip spammy applications
        if (sbn.packageName in exclude_packages) return true

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

        Log.d("JetStreamNotificationListener", "Notification posted: ${sbn.packageName}")

        val service = jetStreamService ?: return
        if (!service.isConnected) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val body  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        val wrapper = MessageWrapper(
            notification = JetStreamNotification(
                create = true,
                id = sbn.id,
                title = title,
                body = body
            )
        )
        val bytes = MessageWrapper.ADAPTER.encode(wrapper)
        service.sendMessage(bytes)
        Log.d("JetStreamNotificationListener", "Notification forwarded: ${sbn.packageName}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("JetStreamNotificationListener", "Notification removed: ${sbn.packageName}")

        // Filter out certain notifications
        if (filterNotification(sbn)) return

        val service = jetStreamService ?: return
        if (!service.isConnected) return

        val wrapper = MessageWrapper(
            notification = JetStreamNotification(
                create = false,
                id = sbn.id,
                title = "",
                body = ""
            )
        )
        val bytes = MessageWrapper.ADAPTER.encode(wrapper)
        val sent = service.sendMessage(bytes)
        Log.d("JetStreamNotificationListener", "Notification removal forwarded: ${sbn.packageName}")
    }
}