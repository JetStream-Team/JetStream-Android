package com.jetstream.android

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.jetstream.android.proto.Action
import com.jetstream.android.proto.Clipboard
import com.jetstream.android.proto.Lock
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.proto.Poweroff
import com.jetstream.android.proto.Reboot

class ActionHandler(private val fgService: JetStreamService?, private val context: Context) {
    private val tag = "ActionHandler"
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun lockDesktop() {
        val message = MessageWrapper(
            action = Action(lock = Lock())
        )
        val sent = fgService?.sendMessage(MessageWrapper.ADAPTER.encode(message))
        Log.d(tag, "Lock desktop message sent=$sent")
    }

    fun poweroffDesktop() {
        val message = MessageWrapper(
            action = Action(poweroff = Poweroff())
        )
        val sent = fgService?.sendMessage(MessageWrapper.ADAPTER.encode(message))
        Log.d(tag, "Poweroff desktop message sent=$sent")
    }

    fun rebootDesktop() {
        val message = MessageWrapper(
            action = Action(reboot = Reboot())
        )
        val sent = fgService?.sendMessage(MessageWrapper.ADAPTER.encode(message))
        Log.d(tag, "Reboot desktop message sent=$sent")
    }

    fun sendClipboard() {
        // Check if the clipboard actually has data
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(
                ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val pasteData = item?.text?.toString()

            if (!pasteData.isNullOrEmpty()) {
                val wrapper = MessageWrapper(
                    clipboard = Clipboard(
                        content = pasteData
                    )
                )
                val sent = fgService?.sendMessage(MessageWrapper.ADAPTER.encode(wrapper))
                Log.d(tag, "Send clipboard data, sent=$sent")
            }
        }
    }
}