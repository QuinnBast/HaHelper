package com.bast.quinn.hahelper

import com.bast.quinn.hahelper.client.HaHelperClient
import com.bast.quinn.hahelper.server.HaHelperServer
import org.slf4j.LoggerFactory
import java.util.*

class HaHelper(
    private val config: HaHelperServerConfig
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelper::class.java)
    }

    private val memberId = UUID.randomUUID().toString()

    fun start() {
        logger.info("Attempting to start server...")
        HaHelperServer(config.local, memberId).start()
        HaHelperClient(config.cluster.discoveryMethod).startClient()
    }

}