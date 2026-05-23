package dev.hongwp.suxai.model;

public class FlowRecord {

    private String id;           // fcltyMngNo
    private String facilityName; // fcltyNm
    private String description;  // dataItemDesc
    private String value;        // dataVal
    private String unit;         // itemUnit
    private String measuredAt;   // occrrncDt 포맷팅
    private String divType;      // M=순간 / D=적산

    public FlowRecord() {}

    public FlowRecord(String id, String facilityName, String description,
                      String value, String unit, String measuredAt, String divType) {
        this.id = id;
        this.facilityName = facilityName;
        this.description = description;
        this.value = value;
        this.unit = unit;
        this.measuredAt = measuredAt;
        this.divType = divType;
    }

    public String getId()           { return id; }
    public void setId(String id)    { this.id = id; }

    public String getFacilityName()                  { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getValue()             { return value; }
    public void setValue(String value)   { this.value = value; }

    public String getUnit()            { return unit; }
    public void setUnit(String unit)   { this.unit = unit; }

    public String getMeasuredAt()                { return measuredAt; }
    public void setMeasuredAt(String measuredAt) { this.measuredAt = measuredAt; }

    public String getDivType()               { return divType; }
    public void setDivType(String divType)   { this.divType = divType; }
}
