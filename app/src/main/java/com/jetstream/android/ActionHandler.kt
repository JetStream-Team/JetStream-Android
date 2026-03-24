package com.jetstream.android

import android.util.Log
import com.jetstream.android.proto.Action
import com.jetstream.android.proto.Lock
import com.jetstream.android.proto.MessageWrapper
import com.jetstream.android.proto.Poweroff
import com.jetstream.android.proto.Reboot

class ActionHandler(private val fgService: JetStreamService?) {
    private val tag = "ActionHandler"

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
}