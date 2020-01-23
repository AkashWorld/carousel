package com.carousel.server

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("ServerApplicationMain")

    val options = Options()
    options.addOption("p", "port", true, "port to listen on")
    options.addOption("w", "password", true, "server password")

    val parser = DefaultParser()
    val cmd = parser.parse(options, args)

    try {
        var port: Int? = null
        if (cmd.hasOption("p")) {
            val portArg = cmd.getOptionValue("p")
            try {
                port = Integer.parseInt(portArg)
            } catch (e: Exception) {
                logger.error("Could not parse port")
                exitProcess(1)
            }
        }
        if (cmd.hasOption("w")) {
            val serverPassword = cmd.getOptionValue("w")
            try {
                Server.getInstance().setServerPassword(serverPassword)
            } catch (e: Exception) {
                logger.error("Could not parse server password")
                exitProcess(1)
            }
        }
        if (port != null) {
            Server.getInstance().initialize(port)
        } else {
            Server.getInstance().initialize()
        }
    } catch (e: Exception) {
        HelpFormatter().printHelp("Server", options)
    }
}
