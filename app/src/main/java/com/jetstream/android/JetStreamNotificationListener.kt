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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import com.jetstream.android.proto.Notification as JetStreamNotification

class JetStreamNotificationListener : NotificationListenerService() {
    private val tag = "JetStreamNotificationListener"

    companion object {
        private val EXCLUDE_PACKAGES = setOf(
            "",
            "com.drnoob.datamonitor"
        )
    }

    private var jetStreamService: JetStreamService? = null
    private var isBound = false

    // Coroutine scope tied to the listener's lifecycle
    private val listenerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        }
        listenerScope.cancel()
        Log.d(tag, "Listener disconnected")
    }

    private fun filterNotification(sbn: StatusBarNotification): Boolean {
        val service = jetStreamService ?: return true
        if (!service.isConnected) return true
        if (sbn.packageName == packageName) return true
        if (sbn.packageName in EXCLUDE_PACKAGES) return true

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val body  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        if (title.isEmpty() && body.isEmpty()) return true

        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (filterNotification(sbn)) return

        listenerScope.launch {
            // Check the setting before forwarding
            val settingsRepo = SettingsRepository(applicationContext)
            val allowed = settingsRepo.settingsFlow.first().allowNotifications
            if (!allowed) {
                Log.d(tag, "Notification sending disabled — skipping")
                return@launch
            }

            val service = jetStreamService ?: return@launch
            Log.d(tag, "Notification posted: ${sbn.packageName}")

            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val body  = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val iconBytes = sbn.notification.smallIcon
                ?.loadDrawable(this@JetStreamNotificationListener)
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
            service.sendMessage(MessageWrapper.ADAPTER.encode(wrapper))
            Log.d(tag, "Notification forwarded: ${sbn.packageName}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (filterNotification(sbn)) return

        listenerScope.launch {
            val settingsRepo = SettingsRepository(applicationContext)
            val allowed = settingsRepo.settingsFlow.first().allowNotifications
            if (!allowed) {
                Log.d(tag, "Notification sending disabled — skipping removal")
                return@launch
            }

            val service = jetStreamService ?: return@launch
            Log.d(tag, "Notification removed: ${sbn.packageName}")

            val wrapper = MessageWrapper(
                notification = JetStreamNotification(
                    create = false,
                    id = sbn.id,
                    title = "",
                    body = ""
                )
            )
            service.sendMessage(MessageWrapper.ADAPTER.encode(wrapper))
            Log.d(tag, "Notification removal forwarded: ${sbn.packageName}")
        }
    }
}