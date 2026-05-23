package dev.hongwp.suxai.model;

public class WaterQualityStation {

    private String id;
    private String name;
    private String loc;
    private double ph;
    private double dissolvedOxygen;
    private double turbidity;
    private double conductivity;
    private double score;
    private String status;

    public WaterQualityStation() {}

    public WaterQualityStation(String id, String name, String loc,
                               double ph, double dissolvedOxygen,
                               double turbidity, double conductivity) {
        this.id = id;
        this.name = name;
        this.loc = loc;
        this.ph = ph;
        this.dissolvedOxygen = dissolvedOxygen;
        this.turbidity = turbidity;
        this.conductivity = conductivity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLoc() { return loc; }
    public void setLoc(String loc) { this.loc = loc; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getDissolvedOxygen() { return dissolvedOxygen; }
    public void setDissolvedOxygen(double dissolvedOxygen) { this.dissolvedOxygen = dissolvedOxygen; }

    public double getTurbidity() { return turbidity; }
    public void setTurbidity(double turbidity) { this.turbidity = turbidity; }

    public double getConductivity() { return conductivity; }
    public void setConductivity(double conductivity) { this.conductivity = conductivity; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
