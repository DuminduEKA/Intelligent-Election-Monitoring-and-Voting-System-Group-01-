package model;

public class District {
    private int districtId;
    private String districtName;
    private int totalRegisteredVoters;

    public District() {}

    public District(int districtId, String districtName, int totalRegisteredVoters) {
        this.districtId = districtId;
        this.districtName = districtName;
        this.totalRegisteredVoters = totalRegisteredVoters;
    }

    public int getDistrictId() { return districtId; }
    public void setDistrictId(int districtId) { this.districtId = districtId; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public int getTotalRegisteredVoters() { return totalRegisteredVoters; }
    public void setTotalRegisteredVoters(int totalRegisteredVoters) { this.totalRegisteredVoters = totalRegisteredVoters; }

    @Override
    public String toString() {
        return districtName; // so it displays nicely in JComboBox
    }
}
