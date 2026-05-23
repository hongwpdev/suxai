package dev.hongwp.suxai.model;

public class FacilityInfo {

    private String sujCode;
    private String facilityName;
    private String address;

    public FacilityInfo() {}

    public FacilityInfo(String sujCode, String facilityName, String address) {
        this.sujCode = sujCode;
        this.facilityName = facilityName;
        this.address = address;
    }

    public String getSujCode()               { return sujCode; }
    public void setSujCode(String sujCode)   { this.sujCode = sujCode; }

    public String getFacilityName()                  { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getAddress()               { return address; }
    public void setAddress(String address)   { this.address = address; }
}
