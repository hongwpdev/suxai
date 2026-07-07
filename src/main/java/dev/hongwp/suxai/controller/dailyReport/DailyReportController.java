package dev.hongwp.suxai.controller.dailyReport;

import dev.hongwp.suxai.model.dailyReport.ReportListItem;
import dev.hongwp.suxai.model.dailyReport.ReportResult;
import dev.hongwp.suxai.service.dailyReport.DailyReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-report")
public class DailyReportController {

    private final DailyReportService dailyReportService;

    public DailyReportController(DailyReportService dailyReportService) {
        this.dailyReportService = dailyReportService;
    }

    @PostMapping("/run")
    public ResponseEntity<ReportResult> run(
        @RequestParam(required = false) String sujCode,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(dailyReportService.generateDailyReport(sujCode, startDate, endDate));
    }

    @GetMapping
    public ResponseEntity<ReportResult> getReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(dailyReportService.getReport(date));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ReportListItem>> listReports() {
        return ResponseEntity.ok(dailyReportService.listReports());
    }
}
