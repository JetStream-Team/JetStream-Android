package com.jetstream.android.services.jetstream

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.jetstream.android.proto.MessageWrapper
import okio.ByteString

const val TAG = "ProtoMessageHandler"
fun protoMessageHandler(bytes: ByteString, context: Context) {
    val wrapper = MessageWrapper.ADAPTER.decode(bytes)

    // Identity Message
    if (wrapper.identity != null) {
        JetStreamRepository.setServerInfo(
            ServerInfo(
                wrapper.identity.name,
                wrapper.identity.host,
                wrapper.identity.port
            )
        )
        Log.d(TAG, "Identity message received")
    }

    else if (wrapper.openapp != null) {
        val packageName = wrapper.openapp.app
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Opened app: $packageName")
        } else {
            Log.w(TAG, "Cannot open app, not installed: $packageName")
        }
    }
}