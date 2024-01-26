package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.grpc.leader.*
import com.bast.quinn.hahelper.state.LeaderStateMutable
import org.slf4j.LoggerFactory

class LeaderServices(
    private val leaderStateMutable: LeaderStateMutable,
) : LeaderServiceGrpcKt.LeaderServiceCoroutineImplBase() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun requestVote(request: VoteRequest): VoteResponse {
        val leaderState = leaderStateMutable.getImmutableState()

        return if(leaderState.hasVotedInTerm(request.electionTerm)) {
            VoteResponse.newBuilder().setIsVoting(false).build()
        } else {
            logger.info("Voting for ${request.leaderId} in term ${request.electionTerm}")
            leaderStateMutable.setTerm(request.electionTerm)
            VoteResponse.newBuilder().setIsVoting(true).build()
        }
    }

    override suspend fun heartbeat(request: HeartbeatRequest): HeartbeatResponse {
        val leaderState = leaderStateMutable.getImmutableState()
        if(!leaderState.hasLeader() || leaderState.electionTerm < request.term) {
            // Update the leader
            leaderStateMutable.setLeader(request.leaderId, request.term)
            logger.info("Got heartbeat from a leader in term ${request.term}, updating leader to ${request.leaderId}")
        }
        leaderStateMutable.setHeartbeat()
        return HeartbeatResponse.getDefaultInstance()
    }

    override suspend fun ping(request: PingRequest): PingResponse {
        return PingResponse.getDefaultInstance()
    }
}