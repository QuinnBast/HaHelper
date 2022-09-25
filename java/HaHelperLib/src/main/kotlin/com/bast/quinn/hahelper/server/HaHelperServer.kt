package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.Host
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory

class HaHelperServer(
    private val localHostConfig: Host,
    myId: String,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelper::class.java)
    }

    private val leaderServices = LeaderServices(myId)

    private val srv = ServerBuilder.forPort(localHostConfig.port)
        .addService(leaderServices)
        .addService(DataServices(leaderServices))

    fun start() {
        logger.info("Starting server at ${localHostConfig.hostname}:${localHostConfig.port}")
        val server = srv.build().start()
    }

}