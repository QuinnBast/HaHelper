package com.bast.quinn.hahelper.server

import com.bast.quinn.hahelper.grpc.meta.GetStatusRequest
import com.bast.quinn.hahelper.grpc.meta.GetStatusResponse
import com.bast.quinn.hahelper.grpc.meta.MetaServiceGrpcKt
import com.bast.quinn.hahelper.state.LeaderStateMutable

class MetaServices(
    private val leaderStateMutable: LeaderStateMutable,
) : MetaServiceGrpcKt.MetaServiceCoroutineImplBase() {

    override suspend fun getStatus(request: GetStatusRequest): GetStatusResponse {
        return GetStatusResponse.newBuilder()
            .setClusterId(leaderStateMutable.getImmutableState().clusterId.toLong())
            .setMemberId(leaderStateMutable.getImmutableState().memberId)
            .setTerm(leaderStateMutable.getImmutableState().electionTerm)
            .setCurrentLeader(leaderStateMutable.leaderId)
            .setRaftState(leaderStateMutable.raftState)
            .setElectionState(leaderStateMutable.electionState)
            .build()
    }

}