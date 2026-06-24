package model;

public class Candidate {
    private int candidateId;
    private String candidateName;
    private int partyId;
    private int districtId;

    // Convenience display fields (joined from other tables)
    private String partyName;
    private String partyShortName;
    private String districtName;

    private byte[] photoData;  // NEW – candidate photo bytes stored in DB

    public Candidate() {}

    public Candidate(int candidateId, String candidateName, int partyId, int districtId) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.partyId = partyId;
        this.districtId = districtId;
    }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public int getPartyId() { return partyId; }
    public void setPartyId(int partyId) { this.partyId = partyId; }

    public int getDistrictId() { return districtId; }
    public void setDistrictId(int districtId) { this.districtId = districtId; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getPartyShortName() { return partyShortName; }
    public void setPartyShortName(String partyShortName) { this.partyShortName = partyShortName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public byte[] getPhotoData() { return photoData; }
    public void setPhotoData(byte[] photoData) { this.photoData = photoData; }

    @Override
    public String toString() {
        return candidateName + (partyName != null ? " (" + partyName + ")" : "");
    }
}