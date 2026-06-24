package model;

import java.sql.Timestamp;

public class Ballot {
    private int ballotId;
    private int voterId;
    private int candidateId;
    private int electionId;
    private Timestamp timestamp;

    public Ballot() {}

    public Ballot(int voterId, int candidateId, int electionId) {
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.electionId = electionId;
    }

    public int getBallotId() { return ballotId; }
    public void setBallotId(int ballotId) { this.ballotId = ballotId; }

    public int getVoterId() { return voterId; }
    public void setVoterId(int voterId) { this.voterId = voterId; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
