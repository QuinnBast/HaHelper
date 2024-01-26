package com.bast.quinn.hahelper.state

import com.bast.quinn.hahelper.grpc.leader.ElectionState
import com.bast.quinn.hahelper.grpc.leader.RaftState
import java.util.*

class LeaderStateMutable(
    private val clusterId: Int,
    private val memberId: String,
    private var electionTerm: Long = 0L,
    var leaderId: String = "",
    var raftState: RaftState = RaftState.FOLLOWER,
    var lastHeartbeat: Long = System.currentTimeMillis(),
    var electionState: ElectionState = ElectionState.IN_ELECTION,
) {

    companion object {
        private val ELECTION_TIMER_DELAY = 200 + Random(System.currentTimeMillis()).nextInt(300)
    }

    fun getImmutableState() = ImmutableLeaderState(clusterId, memberId, electionTerm, leaderId, raftState, lastHeartbeat)

    fun newElectionTerm() {
        electionTerm += 1
    }

    fun setLeader(leader: String, term: Long) {
        electionTerm = term
        leaderId = leader
        lastHeartbeat = System.currentTimeMillis()
        electionState = if(leader == "") {
            ElectionState.IN_ELECTION
        } else {
            ElectionState.IN_CLUSTER
        }

        raftState = if(leader == memberId) {
            RaftState.LEADER
        } else {
            RaftState.FOLLOWER
        }
    }

    fun setTerm(term: Long) {
        // This should also reset the election term timer.
        electionTerm = term
    }

    fun setHeartbeat() {
        lastHeartbeat = System.currentTimeMillis()
    }

    fun shouldStartElection() = raftState == RaftState.FOLLOWER && (lastHeartbeat + ELECTION_TIMER_DELAY < System.currentTimeMillis())
}

data class ImmutableLeaderState(
    val clusterId: Int,
    val memberId: String,
    val electionTerm: Long = 0L,
    val leaderId: String = "",
    val raftState: RaftState = RaftState.FOLLOWER,
    val lastHeartbeat: Long = System.currentTimeMillis()
) {
    fun hasVotedInTerm(term: Long) = term <= electionTerm
    fun hasLeader() = leaderId != ""
}