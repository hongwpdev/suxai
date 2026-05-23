package dev.hongwp.suxai.model;

public class AnalysisResult {

    private String analysis;
    private String analyzedAt;
    private int wqCount;
    private int flCount;

    public AnalysisResult() {}

    public AnalysisResult(String analysis, String analyzedAt, int wqCount, int flCount) {
        this.analysis = analysis;
        this.analyzedAt = analyzedAt;
        this.wqCount = wqCount;
        this.flCount = flCount;
    }

    public String getAnalysis()              { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public String getAnalyzedAt()                { return analyzedAt; }
    public void setAnalyzedAt(String analyzedAt) { this.analyzedAt = analyzedAt; }

    public int getWqCount()              { return wqCount; }
    public void setWqCount(int wqCount)  { this.wqCount = wqCount; }

    public int getFlCount()              { return flCount; }
    public void setFlCount(int flCount)  { this.flCount = flCount; }
}
