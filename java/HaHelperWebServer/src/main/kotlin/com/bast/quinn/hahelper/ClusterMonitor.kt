package com.bast.quinn.hahelper

import com.bast.quinn.hahelper.grpc.leader.ElectionState
import com.bast.quinn.hahelper.grpc.leader.RaftState
import com.bast.quinn.hahelper.grpc.meta.GetStatusResponse
import com.bast.quinn.hahelper.models.ClientStatus

class ClusterMonitor(
    config: HaHelperServerConfig
) {

    val clients: List<ClusterClient> = config.cluster.knownHosts.map {
        ClusterClient(it)
    }

    suspend fun getClusterStatus(): List<ClientStatus> {
        return clients.map {
            val client = it
            kotlin.runCatching {
                val response = it.poll()
                ClientStatus(
                    clusterId = response.clusterId.toString(),
                    memberId = response.memberId,
                    hostname = client.host.hostname,
                    port = client.host.port,
                    electionTerm = response.term.toInt(),
                    currentLeader = response.currentLeader,
                    raftState = response.raftState.toString(),
                    electionState = response.electionState.toString(),
                )
            }.recover {
                ClientStatus(
                    clusterId = "",
                    memberId = "",
                    hostname = client.host.hostname,
                    port = client.host.port,
                    electionTerm = 0,
                    currentLeader = "",
                    raftState = RaftState.RAFT_STATE_UNKNOWN.toString(),
                    electionState = ElectionState.ELECTION_STATE_UNKNOWN.toString(),
                )
            }.getOrThrow()
        }
    }

}