package model;

public class Voter {
    private int voterId;
    private String voterName;
    private String nic;
    private int age;
    private int districtId;
    private boolean hasVoted;
    private String username;
    private String password;
    private String approvalStatus;

    public Voter() {}

    public Voter(int voterId, String voterName, String nic, int age, int districtId,
                 boolean hasVoted, String username, String password) {
        this.voterId = voterId;
        this.voterName = voterName;
        this.nic = nic;
        this.age = age;
        this.districtId = districtId;
        this.hasVoted = hasVoted;
        this.username = username;
        this.password = password;
    }

    public int getVoterId() { return voterId; }
    public void setVoterId(int voterId) { this.voterId = voterId; }

    public String getVoterName() { return voterName; }
    public void setVoterName(String voterName) { this.voterName = voterName; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getDistrictId() { return districtId; }
    public void setDistrictId(int districtId) { this.districtId = districtId; }

    public boolean isHasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getApprovalStatus() {
        return approvalStatus;}

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;}
}
