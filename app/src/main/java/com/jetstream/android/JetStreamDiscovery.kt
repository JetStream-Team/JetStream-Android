package com.jetstream.android

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

private const val NSD_SERVICE_TYPE = "_jetstream._tcp"

class JetStreamDiscovery(
    context: Context,
    private val discoveredServers: SnapshotStateList<ServerInfo>
) {
    private val tag = "JetStreamDiscovery"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private fun onFound(server: ServerInfo) {
        if (discoveredServers.none { it.name == server.name }) {
            discoveredServers.add(server)
        }
    }

    private fun onLost(name: String) {
        // removeIf is the correct way to filter out items by a property
        discoveredServers.removeIf { it.name == name }
    }
    fun start() {
        if (discoveryListener != null) {
            Log.w(tag, "Discovery already running")
            return
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Discovery start failed: error $errorCode")
                discoveryListener = null
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(tag, "Discovery stop failed: error $errorCode")
            }

            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(tag, "Discovery started for $serviceType")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(tag, "Discovery stopped for $serviceType")
                discoveryListener = null
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(tag, "Service found: ${serviceInfo.serviceName}")
                nsdManager.resolveService(serviceInfo, buildResolveListener())
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(tag, "Service lost: ${serviceInfo.serviceName}")
                onLost(serviceInfo.serviceName)
            }
        }

        nsdManager.discoverServices(NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stop() {
        val listener = discoveryListener ?: return
        try {
            nsdManager.stopServiceDiscovery(listener)
        } catch (e: IllegalArgumentException) {
            Log.w(tag, "Discovery listener was not registered: ${e.message}")
        }
        discoveryListener = null
    }

    private fun buildResolveListener() = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(tag, "Resolve failed for ${serviceInfo.serviceName}: error $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val host = serviceInfo.host?.hostAddress ?: run {
                Log.w(tag, "Resolved service has no host address")
                return
            }
            val server = ServerInfo(
                name = serviceInfo.serviceName,
                host = host,
                port = serviceInfo.port
            )
            Log.d(tag, "Service resolved: $server")
            onFound(server)
        }
    }
}