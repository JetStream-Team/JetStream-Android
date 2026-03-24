package com.jetstream.android

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat.getSystemService
import com.jetstream.android.proto.Clipboard
import com.jetstream.android.proto.MessageWrapper


class JetStreamClipboard : AccessibilityService(){
    private val tag = "JetStreamAccessibilityService"

    private var jetStreamService: JetStreamService? = null
    private var isBound = false

    private var lastClipboardContent: String? = null

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?: return@OnPrimaryClipChangedListener

        if (text == lastClipboardContent) return@OnPrimaryClipChangedListener
        lastClipboardContent = text

        Log.d(tag, "Clipboard changed: ${text.take(60)}")
        sendClipboard(text)
    }

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

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }

        Intent(this, JetStreamService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(clipboardListener)

        Log.d(tag, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.d(tag, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.removePrimaryClipChangedListener(clipboardListener)

        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }

        Log.d(tag, "Accessibility service destroyed")
    }

    private fun sendClipboard(text: String) {
        val service = jetStreamService ?: run {
            Log.w(tag, "JetStreamService not bound, dropping clipboard event")
            return
        }
        if (!service.isConnected) return

        val wrapper = MessageWrapper(
            clipboard = Clipboard(content = text)
        )
        val sent = service.sendMessage(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d(tag, "Clipboard message sent=$sent")
    }

    fun setClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("JetStream", text)
        clipboard.setPrimaryClip(clip)
        lastClipboardContent = text
        Log.d(tag, "Local clipboard updated from remote")
    }

}