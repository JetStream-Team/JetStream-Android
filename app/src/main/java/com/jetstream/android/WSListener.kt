package com.jetstream.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.IOException
import com.jetstream.android.proto.Identity as JetStreamIdentity

class WSListener(
    private val callback: WSCallback,
    private val context: Context
): WebSocketListener() {
    private val tag = "WSListener"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.d(tag, "WebSocket Opened")
        callback.onConnected()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.d(tag, "WebSocket Closing")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d(tag, "WebSocket Closed")
        callback.onDisconnected()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        try {
            val identification = JetStreamIdentity.ADAPTER.decode(bytes)
            Log.d(tag, "Identified server: ${identification.name} at ${identification.host}:${identification.port}")
            val server = ServerInfo(
                name = identification.name,
                host = identification.host,
                port = identification.port
            )
            callback.setIdentity(server)
        } catch (e: IOException) {
            Log.e(tag, "Failed to decode identification message: ${e.message}")
        }
        Log.d(tag, "WebSocket Message Received as Bytes")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d(tag, "WebSocket Message Received as Text: $text")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e(tag, "WebSocket Failure: ${t.message}")
        callback.onDisconnected()
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }
}