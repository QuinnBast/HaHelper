package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.HaHelperServerConfig
import com.bast.quinn.hahelper.state.LeaderStateMutable
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory

class HaHelperServer(
    private val serverConfig: HaHelperServerConfig,
    private val leaderState: LeaderStateMutable,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelper::class.java)
    }

    fun start() {
        logger.info("Starting server on ${serverConfig.serverPort}")
        ServerBuilder.forPort(serverConfig.serverPort)
            .addService(LeaderServices(leaderState))
            .addService(DataServices())
            .addService(MetaServices(leaderState))
            .build()
            .start()
    }

}