package dev.hongwp.suxai.service.dailyReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyReportScheduler.class);

    private final DailyReportService dailyReportService;
    private final List<String> targetSujCodes;

    public DailyReportScheduler(DailyReportService dailyReportService,
                                @Value("${daily-report.target-suj-codes:${kwater.suj.code}}") String targetSujCodes) {
        this.dailyReportService = dailyReportService;
        this.targetSujCodes = parseTargetSujCodes(targetSujCodes);
    }

    @Scheduled(cron = "${daily-report.cron}", zone = "${daily-report.zone:Asia/Seoul}")
    public void runDailyBatch() {
        for (String sujCode : targetSujCodes) {
            try {
                dailyReportService.generateDailyReport(sujCode, null, null);
            } catch (Exception e) {
                log.error("일일 보고 배치 실행 실패 - sujCode={}: {}", sujCode, e.getMessage(), e);
            }
        }
    }

    List<String> getTargetSujCodes() {
        return targetSujCodes;
    }

    private List<String> parseTargetSujCodes(String rawTargetSujCodes) {
        return java.util.Arrays.stream(rawTargetSujCodes.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();
    }
}
