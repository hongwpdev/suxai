package dev.hongwp.suxai.model.dailyReport;

import java.time.LocalDate;

public class ReportResult {

    private boolean created;
    private LocalDate reportDate;
    private String filePath;
    private String message;
    private DailyReport report;

    public ReportResult() {
    }

    public ReportResult(boolean created, LocalDate reportDate, String filePath,
                        String message, DailyReport report) {
        this.created = created;
        this.reportDate = reportDate;
        this.filePath = filePath;
        this.message = message;
        this.report = report;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DailyReport getReport() {
        return report;
    }

    public void setReport(DailyReport report) {
        this.report = report;
    }
}
