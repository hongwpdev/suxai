package dev.hongwp.suxai.model.dailyReport;

import java.time.LocalDate;

public class ReportListItem {

    private LocalDate reportDate;
    private String title;
    private String content;
    private String filePath;

    public ReportListItem() {
    }

    public ReportListItem(LocalDate reportDate, String title, String content, String filePath) {
        this.reportDate = reportDate;
        this.title = title;
        this.content = content;
        this.filePath = filePath;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
