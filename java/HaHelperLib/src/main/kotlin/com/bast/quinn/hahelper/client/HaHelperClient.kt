package com.bast.quinn.hahelper.client

import com.bast.quinn.hahelper.DiscoveryMethod
import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.grpc.leader.HeartbeatMessage
import com.bast.quinn.hahelper.grpc.leader.LeaderServiceGrpcKt
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory

class HaHelperClient(
    private val discoveryConfig: DiscoveryMethod,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelper::class.java)
        private const val HEARTBEAT_DELAY_MS = 5000L
        private val scope = CoroutineScope(Dispatchers.IO)
    }

    fun startClient() {
        logger.info("Attempting to discover a cluster...")
        startHeartbeatThreadAsync()
    }

    private fun startHeartbeatThreadAsync() {
        logger.info("Attempting to connect to other clients...")
        createStubs()?.forEach { stub ->
            scope.launch {
                while(true) {
                    startHeartbeatToClient(stub)
                    delay(HEARTBEAT_DELAY_MS)
                }
            }
        }
    }

    private suspend fun startHeartbeatToClient(stub: LeaderServiceGrpcKt.LeaderServiceCoroutineStub) {
        val flow = flow {
            while(true) {
                emit(HeartbeatMessage.getDefaultInstance())
                delay(HEARTBEAT_DELAY_MS)
            }
        }
        kotlin.runCatching {
            stub.heartbeat(flow).collect { heartbeatResponse ->
                logger.info("Connected to ${stub.channel.authority()}")
            }
        }.onFailure {
            logger.error("Unable to connect to ${stub.channel.authority()}")
        }
    }

    private fun createStubs() = discoveryConfig.knownHosts?.map {
        LeaderServiceGrpcKt.LeaderServiceCoroutineStub(
            ManagedChannelBuilder
                .forAddress(it.hostname, it.port)
                .usePlaintext()
                .build()
        )
    }
}