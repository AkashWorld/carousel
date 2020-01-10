package com.carousal.server

import org.bitlet.weupnp.GatewayDevice
import org.bitlet.weupnp.GatewayDiscover
import org.slf4j.LoggerFactory


interface UPnPProvider {
    fun requestMapping(): Boolean
    fun release()
    fun tryGetExternalIp(): String?
}

class UPnPProviderImpl(private val port: Int) : UPnPProvider {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var device: GatewayDevice? = null
    private var externalIp: String? = null

    override fun requestMapping(): Boolean {
        try {
            val discover = GatewayDiscover()
            discover.discover()
            device = discover.validGateway
            if (device != null) {
                logger.info("Found gateway device ${device?.friendlyName}")
            } else {
                logger.error("No valid gateway device found.")
                return false
            }
            val localAddress = device?.localAddress
            externalIp = device?.externalIPAddress
            logger.info("Sending port mapping request");
            if (device?.addPortMapping(
                    port, port,
                    localAddress?.hostAddress, "TCP", "Carousal"
                ) != true
            ) {
                logger.error("Port mapping attempt failed");
                return false
            }
            return true
        } catch (e: Exception) {
            logger.error(e.message, e.cause)
            device = null
            externalIp = null
            return false
        }
    }

    override fun release() {
        device?.deletePortMapping(port, "TCP")
        externalIp = null
    }

    override fun tryGetExternalIp(): String? {
        return externalIp
    }

}

