package dev.hongwp.suxai.model.dailyReport;

public class AIResponse {

    private String analysis;
    private String analyzedAt;
    private int waterQualityCount;
    private int flowCount;

    public AIResponse() {
    }

    public AIResponse(String analysis, String analyzedAt, int waterQualityCount, int flowCount) {
        this.analysis = analysis;
        this.analyzedAt = analyzedAt;
        this.waterQualityCount = waterQualityCount;
        this.flowCount = flowCount;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(String analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public int getWaterQualityCount() {
        return waterQualityCount;
    }

    public void setWaterQualityCount(int waterQualityCount) {
        this.waterQualityCount = waterQualityCount;
    }

    public int getFlowCount() {
        return flowCount;
    }

    public void setFlowCount(int flowCount) {
        this.flowCount = flowCount;
    }
}
