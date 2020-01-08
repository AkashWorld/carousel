package com.carousal.server

import org.fourthline.cling.UpnpServiceImpl
import org.fourthline.cling.support.igd.PortMappingListener
import org.fourthline.cling.support.model.PortMapping
import org.slf4j.LoggerFactory
import java.net.InetAddress

interface UPnPProvider {
    fun requestMapping()
    fun release()
}

class UPnPProviderImpl(private val port: Int) : UPnPProvider {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val desiredMapping =
        PortMapping(port, InetAddress.getLocalHost().hostAddress, PortMapping.Protocol.TCP, "CarousalUPnP")
    private val upnpService = UpnpServiceImpl(PortMappingListener(desiredMapping))

    override fun requestMapping() {
        logger.info("Requesting UPnP port forwarding for port $port")
        upnpService.controlPoint?.search()
    }

    override fun release() {
        upnpService.shutdown()
    }
}
