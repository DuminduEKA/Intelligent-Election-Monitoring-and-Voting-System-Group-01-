package model;

import java.sql.Date;

public class Election {
    private int electionId;
    private String electionTitle;
    private Date electionDate;
    private String status; // Active / Closed

    public Election() {}

    public Election(int electionId, String electionTitle, Date electionDate, String status) {
        this.electionId = electionId;
        this.electionTitle = electionTitle;
        this.electionDate = electionDate;
        this.status = status;
    }

    public int getElectionId() { return electionId; }
    public void setElectionId(int electionId) { this.electionId = electionId; }

    public String getElectionTitle() { return electionTitle; }
    public void setElectionTitle(String electionTitle) { this.electionTitle = electionTitle; }

    public Date getElectionDate() { return electionDate; }
    public void setElectionDate(Date electionDate) { this.electionDate = electionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return electionTitle + " [" + status + "]";
    }
}
