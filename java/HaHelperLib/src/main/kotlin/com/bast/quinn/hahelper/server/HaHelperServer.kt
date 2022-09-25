package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.model.LeaderStateMutable
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory

class HaHelperServer(
    private val port: Int,
    private val leaderState: LeaderStateMutable,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelper::class.java)
    }

    fun start() {
        logger.info("Starting server on $port")
        ServerBuilder.forPort(port)
            .addService(LeaderServices(leaderState))
            .addService(DataServices())
            .build()
            .start()
    }

}