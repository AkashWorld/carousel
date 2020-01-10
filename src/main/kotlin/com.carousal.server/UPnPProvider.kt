package com.carousal.server

import org.fourthline.cling.DefaultUpnpServiceConfiguration
import org.fourthline.cling.UpnpService
import org.fourthline.cling.UpnpServiceImpl
import org.fourthline.cling.model.Namespace
import org.fourthline.cling.support.igd.PortMappingListener
import org.fourthline.cling.support.model.PortMapping
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl
import org.fourthline.cling.transport.impl.jetty.StreamClientImpl
import org.fourthline.cling.transport.spi.NetworkAddressFactory
import org.fourthline.cling.transport.spi.StreamClient
import org.fourthline.cling.transport.spi.StreamServer
import org.slf4j.LoggerFactory
import java.net.InetAddress


interface UPnPProvider {
    fun requestMapping()
    fun release()
}

class UPnPProviderImpl(private val port: Int) : UPnPProvider {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var upnpService: UpnpService? = null
    override fun requestMapping() {
        logger.info("Requesting UPnP port forwarding for port $port")
        val desiredMapping =
            PortMapping(port, InetAddress.getLocalHost().hostAddress, PortMapping.Protocol.TCP, "CarousalUPnP")
        upnpService = UpnpServiceImpl(MyUpnpServiceConfiguration(), PortMappingListener(desiredMapping))
        upnpService?.controlPoint?.search()
    }

    override fun release() {
        upnpService?.shutdown()
    }
}


class MyUpnpServiceConfiguration : DefaultUpnpServiceConfiguration() {
    override fun createNamespace(): Namespace {
        return Namespace("/upnp") // This will be the servlet context path
    }

    override fun createStreamClient(): StreamClient<*> {
        return StreamClientImpl(
            StreamClientConfigurationImpl(
                super.getSyncProtocolExecutorService()
            )
        )
    }

    override fun createStreamServer(networkAddressFactory: NetworkAddressFactory): StreamServer<*> {
        return AsyncServletStreamServerImpl(
            AsyncServletStreamServerConfigurationImpl(
                JettyServletContainer.INSTANCE,
                networkAddressFactory.streamListenPort
            )
        )
    }
}