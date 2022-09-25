package com.bast.quinn.hahelper.model

import java.util.*

class LeaderStateMutable(
    val memberId: String,
    private var electionTerm: Long = 0L,
    var leaderId: String = "",
    var raftState: RaftState = RaftState.FOLLOWER,
    var lastHeartbeat: Long = System.currentTimeMillis()
) {

    companion object {
        private val ELECTION_TIMER_DELAY = 200 + Random(System.currentTimeMillis()).nextInt(300)
    }

    fun getState() = LeaderState(memberId, electionTerm, leaderId, raftState, lastHeartbeat)

    fun newElectionTerm() {
        electionTerm += 1
    }

    fun setLeader(leader: String, term: Long) {
        if(leader == memberId) {
            raftState = RaftState.LEADER
        } else {
            raftState = RaftState.FOLLOWER
        }
        lastHeartbeat = System.currentTimeMillis()
        electionTerm = term
        leaderId = leader
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

enum class RaftState {
    FOLLOWER, LEADER
}

data class LeaderState(
    val memberId: String,
    val electionTerm: Long = 0L,
    val leaderId: String = "",
    val raftState: RaftState = RaftState.FOLLOWER,
    val lastHeartbeat: Long = System.currentTimeMillis()
) {
    fun hasVotedInTerm(term: Long) = term <= electionTerm
    fun hasLeader() = leaderId != ""
}