package dev.hongwp.suxai.model;

public class WaterQualityRecord {

    private String id;           // fcltyMngNo
    private String facilityName; // fcltyMngNm
    private String address;      // fcltyAddr
    private String divName;      // liIndDivName
    private String measuredAt;   // occrrncDt 포맷팅
    private String phVal;        // phVal
    private String phUnit;       // phUnit
    private String tbVal;        // tbVal (탁도)
    private String tbUnit;       // tbUnit
    private String clVal;        // clVal (잔류염소)
    private String clUnit;       // clUnit

    public WaterQualityRecord() {}

    public WaterQualityRecord(String id, String facilityName, String address, String divName,
                              String measuredAt,
                              String phVal, String phUnit,
                              String tbVal, String tbUnit,
                              String clVal, String clUnit) {
        this.id = id;
        this.facilityName = facilityName;
        this.address = address;
        this.divName = divName;
        this.measuredAt = measuredAt;
        this.phVal = phVal;
        this.phUnit = phUnit;
        this.tbVal = tbVal;
        this.tbUnit = tbUnit;
        this.clVal = clVal;
        this.clUnit = clUnit;
    }

    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }

    public String getFacilityName()                  { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getAddress()               { return address; }
    public void setAddress(String address)   { this.address = address; }

    public String getDivName()               { return divName; }
    public void setDivName(String divName)   { this.divName = divName; }

    public String getMeasuredAt()                { return measuredAt; }
    public void setMeasuredAt(String measuredAt) { this.measuredAt = measuredAt; }

    public String getPhVal()             { return phVal; }
    public void setPhVal(String phVal)   { this.phVal = phVal; }

    public String getPhUnit()              { return phUnit; }
    public void setPhUnit(String phUnit)   { this.phUnit = phUnit; }

    public String getTbVal()             { return tbVal; }
    public void setTbVal(String tbVal)   { this.tbVal = tbVal; }

    public String getTbUnit()              { return tbUnit; }
    public void setTbUnit(String tbUnit)   { this.tbUnit = tbUnit; }

    public String getClVal()             { return clVal; }
    public void setClVal(String clVal)   { this.clVal = clVal; }

    public String getClUnit()              { return clUnit; }
    public void setClUnit(String clUnit)   { this.clUnit = clUnit; }
}
