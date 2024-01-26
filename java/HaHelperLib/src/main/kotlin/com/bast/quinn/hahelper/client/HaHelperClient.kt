package com.bast.quinn.hahelper.client

import com.bast.quinn.hahelper.HaHelper
import com.bast.quinn.hahelper.HaHelperServerConfig
import com.bast.quinn.hahelper.Host
import com.bast.quinn.hahelper.grpc.leader.HeartbeatRequest
import com.bast.quinn.hahelper.grpc.leader.LeaderServiceGrpcKt
import com.bast.quinn.hahelper.grpc.leader.VoteRequest
import com.bast.quinn.hahelper.model.ImmutableLeaderState
import com.bast.quinn.hahelper.model.LeaderStateMutable
import com.bast.quinn.hahelper.model.RaftState
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.util.*

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

    private val leaderElectionStubs = serverConfig.cluster.knownHosts
        .filter { !isMyself(it) }
        .map {
            logger.info("Creating a client to {}:{}", it.hostname, it.port)
            LeaderServiceGrpcKt.LeaderServiceCoroutineStub(
                ManagedChannelBuilder
                    .forAddress(it.hostname, it.port)
                    .usePlaintext()
                    .build()
        )
    }

    private fun isMyself(it: Host) =
        serverConfig.serverPort == it.port && (it.hostname.lowercase(Locale.getDefault()) == "localhost" || it.hostname == InetAddress.getLocalHost().hostAddress || it.hostname == InetAddress.getLocalHost().hostName)

    fun startClient() = runBlocking {
        logger.info("Attempting to discover a cluster...")
        logger.info("Required quorum: {}", serverConfig.cluster.getQuorum())
        startElectionTermThreadAsync()
        startHeartbeatThreadAsync()
    }

    private fun startHeartbeatThreadAsync() = scope.launch {
        while(true) {
            delay(HEARTBEAT_DELAY_MS)
            if (mutableLeaderState.raftState == RaftState.LEADER) {
                sendHeartbeats(mutableLeaderState.getImmutableState())
            }
        }
    }

    private fun startElectionTermThreadAsync() = scope.launch {
        while(true) {
            delay(HEARTBEAT_DELAY_MS + random.nextInt(300).toLong())
            if(mutableLeaderState.shouldStartElection()) {
                performElection()
            }
        }
    }

    private suspend fun performElection() {
        if(mutableLeaderState.getImmutableState().hasLeader()) {
            logger.info("Lost quorum. Starting new election.")
            mutableLeaderState.setLeader("", 0)
        }

        mutableLeaderState.newElectionTerm()
        val leaderState = mutableLeaderState.getImmutableState()

        val votes = 1 + (requestVotes(leaderState)?.count { it != null && it.isVoting } ?: 0)
        val quorum = serverConfig.cluster.getQuorum()

        if(votes >= quorum) {
            logger.info("Got $votes / $quorum votes in term ${leaderState.electionTerm}. I am the leader (${leaderState.memberId}).")
            mutableLeaderState.setLeader(leaderState.memberId, leaderState.electionTerm)
            sendHeartbeats(leaderState)
        } else {
            if(leaderState.electionTerm.toInt() % 200 == 0) {
                logger.info("Waiting for majority. Cluster quorum not met. Term {}", leaderState.electionTerm)
            }
        }
    }

    private suspend fun requestVotes(immutableLeaderState: ImmutableLeaderState) = leaderElectionStubs.map {
        kotlin.runCatching {
            it.requestVote(
                VoteRequest
                    .newBuilder()
                    .setElectionTerm(immutableLeaderState.electionTerm)
                    .setLeaderId(immutableLeaderState.memberId)
                    .build()
            )
        }.getOrNull()
    }

    private suspend fun sendHeartbeats(immutableLeaderState: ImmutableLeaderState) = leaderElectionStubs.map {
        kotlin.runCatching {
            mutableLeaderState.setHeartbeat()
            it.heartbeat(
                HeartbeatRequest.newBuilder()
                    .setLeaderId(immutableLeaderState.memberId)
                    .setClusterId(1)
                    .setTerm(immutableLeaderState.electionTerm)
                    .build()
            )
        }.getOrNull()
    }
}