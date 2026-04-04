package com.jetstream.android.discovery

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.jetstream.android.services.jetstream.ServerInfo

class DiscoveryListener(
    private val nsdManager: NsdManager,
    private val onFound: (ServerInfo) -> Unit,
    private val onLost: (String) -> Unit,
) : NsdManager.DiscoveryListener {

    private val tag = "DiscoveryListener"

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        DiscoveryRepository.setDiscovering(false)
        Log.e(tag, "Discovery start failed: error $errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        DiscoveryRepository.setDiscovering(false)
        Log.e(tag, "Discovery stop failed: error $errorCode")
    }

    override fun onDiscoveryStarted(serviceType: String) {
        DiscoveryRepository.setDiscovering(true)
        Log.d(tag, "Discovery started for $serviceType")
    }

    override fun onDiscoveryStopped(serviceType: String) {
        DiscoveryRepository.setDiscovering(false)
        Log.d(tag, "Discovery stopped for $serviceType")
    }

    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        Log.d(tag, "Service found: ${serviceInfo.serviceName}")
        nsdManager.resolveService(serviceInfo, buildResolveListener())
    }

    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        Log.d(tag, "Service lost: ${serviceInfo.serviceName}")
        onLost(serviceInfo.serviceName)
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