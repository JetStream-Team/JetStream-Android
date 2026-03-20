package com.jetstream.android

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.jetstream.android.proto.Clipboard
import com.jetstream.android.proto.MessageWrapper

class JetStreamAccessibilityService : AccessibilityService() {

    private var jetStreamService: JetStreamService? = null
    private var isBound = false
    private var clipboardManager: ClipboardManager? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            jetStreamService = (binder as JetStreamService.LocalBinder).getService()
            isBound = true
            Log.d("JetStreamA11y", "Bound to JetStreamService")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            jetStreamService = null
            isBound = false
            Log.d("JetStreamA11y", "Unbound from JetStreamService")
        }
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val service = jetStreamService ?: return@OnPrimaryClipChangedListener
        if (!service.isConnected) return@OnPrimaryClipChangedListener

        val clip = clipboardManager?.primaryClip ?: return@OnPrimaryClipChangedListener
        if (clip.itemCount == 0) return@OnPrimaryClipChangedListener

        val text = clip.getItemAt(0).coerceToText(this).toString()
        if (text.isEmpty()) return@OnPrimaryClipChangedListener

        Log.d("JetStreamA11y", "Clipboard changed, sending to desktop")

        val wrapper = MessageWrapper(
            clipboard = Clipboard(content = text)
        )
        val sent = service.sendMessage(MessageWrapper.ADAPTER.encode(wrapper))
        Log.d("JetStreamA11y", "Clipboard sent=$sent")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("JetStreamA11y", "Accessibility service connected")

        bindService(
            Intent(this, JetStreamService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(clipboardListener)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        Log.d("JetStreamA11y", "Accessibility service destroyed")
    }
}