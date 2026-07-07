package dev.hongwp.suxai.model.dailyReport;

import java.time.LocalDate;

public class DailyReport {

    private LocalDate reportDate;
    private String sujCode;
    private String facilityName;
    private String title;
    private String markdown;
    private String analyzedAt;
    private int waterQualityCount;
    private int flowCount;

    public DailyReport() {
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getSujCode() {
        return sujCode;
    }

    public void setSujCode(String sujCode) {
        this.sujCode = sujCode;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
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
