package com.jetstream.android.service

import android.util.Log
import com.jetstream.android.proto.MessageWrapper
import okio.ByteString

const val TAG = "ProtoMessageHandler"
fun protoMessageHandler(bytes: ByteString) {
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
}