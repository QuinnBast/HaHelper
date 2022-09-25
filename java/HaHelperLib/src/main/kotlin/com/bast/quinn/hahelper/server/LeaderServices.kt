package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.grpc.leader.*
import com.bast.quinn.hahelper.grpc.leader.LeaderServiceGrpc.LeaderServiceImplBase
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LeaderServices(
    private val myId: String
) : LeaderServiceGrpcKt.LeaderServiceCoroutineImplBase() {

    companion object {
        val scope = CoroutineScope(Dispatchers.IO)
    }

    private val leaderId: String = ""
    private val isElectingSelf = false

    // The amount of time to elapse before attempting to elect itself the leader
    val electionTimer: Long = 0

    fun amILeader() = leaderId == myId

    override suspend fun electLeader(request: ElectLeaderRequest): ElectLeaderResponse {
        if(leaderId != "") {
            // A leader is already elected, don't vote for the new leader
        }
        // Elect the incoming person as the leader
        return ElectLeaderResponse.getDefaultInstance()
    }

    override suspend fun leaderDecided(request: LeaderDecidedRequest): LeaderDecidedResponse {
        return LeaderDecidedResponse.getDefaultInstance()
    }

    override fun heartbeat(requests: Flow<HeartbeatMessage>): Flow<HeartbeatMessage> {
        return flow {
            requests.collect {
                emit(HeartbeatMessage.getDefaultInstance())
            }
        }
    }

    override suspend fun ping(request: PingRequest): PingResponse {
        return PingResponse.getDefaultInstance()
    }

}