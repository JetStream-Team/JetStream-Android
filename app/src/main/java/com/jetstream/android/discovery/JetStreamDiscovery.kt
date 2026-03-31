package com.jetstream.android.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.util.Log

private const val NSD_SERVICE_TYPE = "_jetstream._tcp"

class JetStreamDiscovery(
    context: Context,
    val repository: DiscoveryRepository = DiscoveryRepository()
) {
    private val tag = "JetStreamDiscovery"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var activeListener: DiscoveryListener? = null

    fun start() {
        if (activeListener != null) {
            Log.w(tag, "Discovery already running")
            return
        }

        val listener = DiscoveryListener(
            nsdManager = nsdManager,
            onFound = repository::add,
            onLost = repository::remove,
        )

        nsdManager.discoverServices(NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        activeListener = listener
    }

    fun stop() {
        val listener = activeListener ?: return
        try {
            nsdManager.stopServiceDiscovery(listener)
        } catch (e: IllegalArgumentException) {
            Log.w(tag, "Discovery listener was not registered: ${e.message}")
        }
        activeListener = null
    }
}