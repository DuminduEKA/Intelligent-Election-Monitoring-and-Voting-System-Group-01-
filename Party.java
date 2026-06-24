package model;

public class Party {
    private int partyId;
    private String partyName;
    private String shortName;       // NEW – short / abbreviated party name (e.g. "UNP")
    private String partyLogoPath;   // kept for backward-compat (unused when BLOB is set)
    private String leaderName;
    private byte[] logoData;        // NEW – actual image bytes stored in DB

    public Party() {}

    public Party(int partyId, String partyName, String partyLogoPath, String leaderName) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.partyLogoPath = partyLogoPath;
        this.leaderName = leaderName;
    }

    public Party(int partyId, String partyName, String shortName, String partyLogoPath, String leaderName) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.shortName = shortName;
        this.partyLogoPath = partyLogoPath;
        this.leaderName = leaderName;
    }

    public int getPartyId() { return partyId; }
    public void setPartyId(int partyId) { this.partyId = partyId; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getPartyLogoPath() { return partyLogoPath; }
    public void setPartyLogoPath(String partyLogoPath) { this.partyLogoPath = partyLogoPath; }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public byte[] getLogoData() { return logoData; }
    public void setLogoData(byte[] logoData) { this.logoData = logoData; }

    @Override
    public String toString() {
        return (shortName != null && !shortName.trim().isEmpty())
                ? partyName + " (" + shortName + ")"
                : partyName;
    }
}
