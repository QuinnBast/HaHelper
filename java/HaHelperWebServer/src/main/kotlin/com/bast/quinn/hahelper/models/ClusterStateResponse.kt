package com.bast.quinn.hahelper.models

@kotlinx.serialization.Serializable
data class ClusterStateResponse(
    val state: List<ClientStatus>,
)

@kotlinx.serialization.Serializable
data class ClientStatus(
    val clusterId: String,
    val memberId: String,
    val hostname: String,
    val port: Int,
    val electionTerm: Int,
    val currentLeader: String,
    val raftState: String,
    val electionState: String,
)