package dev.hongwp.suxai.model.dailyReport;

import java.time.LocalDate;

public class AIRequest {

    private String sujCode;
    private String facilityName;
    private LocalDate reportDate;
    private String startDate;
    private String endDate;

    public AIRequest() {
    }

    public AIRequest(String sujCode, String facilityName, LocalDate reportDate,
                     String startDate, String endDate) {
        this.sujCode = sujCode;
        this.facilityName = facilityName;
        this.reportDate = reportDate;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
