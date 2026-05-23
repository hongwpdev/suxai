package dev.hongwp.suxai.model;

public class FlowStation {

    private String id;
    private String name;
    private String loc;
    private double flow;
    private double level;
    private double score;
    private String status;

    public FlowStation() {}

    public FlowStation(String id, String name, String loc, double flow, double level) {
        this.id = id;
        this.name = name;
        this.loc = loc;
        this.flow = flow;
        this.level = level;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLoc() { return loc; }
    public void setLoc(String loc) { this.loc = loc; }

    public double getFlow() { return flow; }
    public void setFlow(double flow) { this.flow = flow; }

    public double getLevel() { return level; }
    public void setLevel(double level) { this.level = level; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
