package com.jetstream.android.services.jetstream

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WSListener(
    private val serverIp: String,
    private val port: Int,
    private val updateNotification: (String) -> Unit,
    private val clearWebSocket: () -> Unit
) : WebSocketListener() {

    private val TAG = "WSListener"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WebSocket opened")
        JetStreamRepository.onWebSocketConnected(serverIp, port)
        JetStreamRepository.setServerInfo(ServerInfo("", serverIp))
        updateNotification("Connected to $serverIp")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Message received: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "Binary message received: ${bytes.size} bytes")
        protoMessageHandler(bytes)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket closing: $code $reason")
        webSocket.close(1000, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket closed: $code $reason")
        clearWebSocket()
        JetStreamRepository.onWebSocketDisconnected()
        updateNotification("Disconnected")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "WebSocket failure: ${t.message}")
        clearWebSocket()
        JetStreamRepository.onWebSocketDisconnected()
        updateNotification("Disconnected")
    }
}