package dev.hongwp.suxai.service.dailyReport;

import dev.hongwp.suxai.model.FacilityInfo;
import dev.hongwp.suxai.model.dailyReport.AIRequest;
import dev.hongwp.suxai.model.dailyReport.AIResponse;
import dev.hongwp.suxai.model.dailyReport.DailyReport;
import dev.hongwp.suxai.model.dailyReport.ReportListItem;
import dev.hongwp.suxai.model.dailyReport.ReportResult;
import dev.hongwp.suxai.service.FacilityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyReportService {

    private final FacilityService facilityService;
    private final AIClient aiClient;
    private final ReportWriter reportWriter;
    private final String defaultSujCode;

    public DailyReportService(FacilityService facilityService,
                              AIClient aiClient,
                              ReportWriter reportWriter,
                              @Value("${kwater.suj.code}") String defaultSujCode) {
        this.facilityService = facilityService;
        this.aiClient = aiClient;
        this.reportWriter = reportWriter;
        this.defaultSujCode = defaultSujCode;
    }

    public ReportResult generateDailyReport(String sujCode, String startDate, String endDate) {
        LocalDate reportDate = resolveReportDate(endDate, startDate);
        String effectiveSujCode = isBlank(sujCode) ? defaultSujCode : sujCode;
        String effectiveStartDate = isBlank(startDate) ? reportDate.toString() : startDate;
        String effectiveEndDate = isBlank(endDate) ? reportDate.toString() : endDate;
        String facilityName = resolveFacilityName(effectiveSujCode);

        AIRequest request = new AIRequest(
            effectiveSujCode,
            facilityName,
            reportDate,
            effectiveStartDate,
            effectiveEndDate
        );

        AIResponse aiResponse = aiClient.analyze(request);

        DailyReport report = new DailyReport();
        report.setReportDate(reportDate);
        report.setSujCode(effectiveSujCode);
        report.setFacilityName(facilityName);
        report.setTitle("%s 일일 수질 리포트".formatted(facilityName));
        report.setMarkdown(aiResponse.getAnalysis());
        report.setAnalyzedAt(aiResponse.getAnalyzedAt());
        report.setWaterQualityCount(aiResponse.getWaterQualityCount());
        report.setFlowCount(aiResponse.getFlowCount());

        return reportWriter.write(report);
    }

    public ReportResult getReport(LocalDate reportDate) {
        return reportWriter.read(reportDate);
    }

    public List<ReportListItem> listReports() {
        return reportWriter.list();
    }

    private LocalDate resolveReportDate(String endDate, String startDate) {
        if (!isBlank(endDate)) {
            return LocalDate.parse(endDate);
        }
        if (!isBlank(startDate)) {
            return LocalDate.parse(startDate);
        }
        return LocalDate.now();
    }

    private String resolveFacilityName(String sujCode) {
        List<FacilityInfo> facilities = facilityService.getFacilities();
        return facilities.stream()
            .filter(facility -> sujCode.equals(facility.getSujCode()))
            .map(FacilityInfo::getFacilityName)
            .findFirst()
            .orElse("정수장 " + sujCode);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
