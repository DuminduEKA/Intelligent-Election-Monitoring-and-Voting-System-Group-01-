package model;

public class VoterUser extends User {

    private int voterId;
    private String nic;
    private int districtId;
    private boolean hasVoted;

    public VoterUser(int id, String name, int voterId, String nic, int districtId, boolean hasVoted) {
        super(id, name);
        this.voterId = voterId;
        this.nic = nic;
        this.districtId = districtId;
        this.hasVoted = hasVoted;
    }

    public int getVoterId() {
        return voterId;
    }

    public String getNic() {
        return nic;
    }

    public int getDistrictId() {
        return districtId;
    }

    public boolean hasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    @Override
    public String displayDashboard() {
        return "VoterDashboard";
    }
}
