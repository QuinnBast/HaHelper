package com.bast.quinn.hahelper.client

import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.HaHelperServerConfig
import com.bast.quinn.hahelper.grpc.leader.HeartbeatRequest
import com.bast.quinn.hahelper.grpc.leader.LeaderServiceGrpcKt
import com.bast.quinn.hahelper.grpc.leader.VoteRequest
import com.bast.quinn.hahelper.model.LeaderState
import com.bast.quinn.hahelper.model.LeaderStateMutable
import com.bast.quinn.hahelper.model.RaftState
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.floor

class HaHelperClient(
    private val serverConfig: HaHelperServerConfig,
    private val mutableLeaderState: LeaderStateMutable,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HaHelper::class.java)
        private const val HEARTBEAT_DELAY_MS = 50L
        private val scope = CoroutineScope(Dispatchers.IO)
        private val random = Random(System.currentTimeMillis())
    }

    private val leaderElectionStubs = serverConfig.cluster.discoveryMethod.knownHosts
        ?.filter { serverConfig.local != it }
        ?.map {
        LeaderServiceGrpcKt.LeaderServiceCoroutineStub(
            ManagedChannelBuilder
                .forAddress(it.hostname, it.port)
                .usePlaintext()
                .build()
        )
    }

    fun startClient() = runBlocking {
        logger.info("Attempting to discover a cluster...")
        startElectionTermThreadAsync()
        startHeartbeatThreadAsync()
    }

    private suspend fun requestVotes(leaderState: LeaderState) = leaderElectionStubs?.map {
        kotlin.runCatching {
            it.requestVote(
                VoteRequest
                    .newBuilder()
                    .setElectionTerm(leaderState.electionTerm)
                    .setLeaderId(leaderState.memberId)
                    .build()
            )
        }.getOrNull()
    }

    private suspend fun sendHeartBeats(leaderState: LeaderState) = leaderElectionStubs?.map {
        kotlin.runCatching {
            mutableLeaderState.setHeartbeat()
            it.heartbeat(
                HeartbeatRequest.newBuilder()
                    .setLeaderId(leaderState.memberId)
                    .setClusterId(1)
                    .setTerm(leaderState.electionTerm)
                    .build()
            )
        }.getOrNull()
    }

    private fun startHeartbeatThreadAsync() = scope.launch {
        logger.info("Starting heartbeat thread...")
        while(true) {
            delay(HEARTBEAT_DELAY_MS)
            if (mutableLeaderState.raftState == RaftState.LEADER) {
                sendHeartBeats(mutableLeaderState.getState())
            }
        }
    }

    private fun startElectionTermThreadAsync() = scope.launch {
        logger.info("Starting thread to manage elections...")
        while(true) {
            delay(HEARTBEAT_DELAY_MS + random.nextInt(300).toLong())
            if(mutableLeaderState.shouldStartElection()) {
                mutableLeaderState.newElectionTerm()
                val leaderState = mutableLeaderState.getState()
                val responses = requestVotes(leaderState)
                val votes = 1 + (responses?.count { it != null && it.isVoting } ?: 0)
                val quorum = (floor(serverConfig.cluster.clusterSize / 2.0) + 1).toInt()
                if(votes >= quorum) {
                    mutableLeaderState.setLeader(leaderState.memberId, leaderState.electionTerm)
                    logger.info("Got $votes / $quorum votes in term ${leaderState.electionTerm}. I am the leader (${leaderState.memberId}).")
                    sendHeartBeats(leaderState)
                }
            }
        }
    }
}