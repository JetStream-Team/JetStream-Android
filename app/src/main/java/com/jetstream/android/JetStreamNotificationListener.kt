package com.jetstream.android

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.proto.Notification as JetStreamNotification

val exclude_packages = arrayOf(
    ""
//    "com.drnoob.datamonitor"
)

class JetStreamNotificationListener : NotificationListenerService() {

    private var jetStreamService: JetStreamService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            jetStreamService = (binder as JetStreamService.LocalBinder).getService()
            isBound = true
            println("JetStreamNotificationListener bound to JetStreamService")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            jetStreamService = null
            isBound = false
            println("JetStreamNotificationListener unbound from JetStreamService")
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Intent(this, JetStreamService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        println("JetStreamNotificationListener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            println("JetStreamNotificationListener disconnected")
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
        println("Notification posted: ${sbn.packageName}")

        // Filter out certain notifications
        if (filterNotification(sbn)) return

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
        val sent = service.sendMessage(bytes)
        println("Notification forwarded [${sbn.packageName}] sent=$sent")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        println("Notification removed: ${sbn.packageName}")

        // Filter out certain notifications
        if (filterNotification(sbn)) return

        val service = jetStreamService ?: return
        if (!service.isConnected) return

        val extras = sbn.notification.extras

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
        println("Notification removal forwarded [${sbn.packageName}] sent=$sent")
    }
}