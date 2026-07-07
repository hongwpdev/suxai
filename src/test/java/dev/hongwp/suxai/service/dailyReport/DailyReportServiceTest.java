package dev.hongwp.suxai.service.dailyReport;

import dev.hongwp.suxai.model.FacilityInfo;
import dev.hongwp.suxai.model.dailyReport.AIResponse;
import dev.hongwp.suxai.model.dailyReport.ReportResult;
import dev.hongwp.suxai.service.FacilityService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DailyReportServiceTest {

    @Test
    void generatesReportUsingExistingAiFlow() {
        FacilityService facilityService = mock(FacilityService.class);
        AIClient aiClient = mock(AIClient.class);
        ReportWriter reportWriter = mock(ReportWriter.class);

        when(facilityService.getFacilities()).thenReturn(List.of(
            new FacilityInfo("318", "파주정수장", "")
        ));
        when(aiClient.analyze(any())).thenReturn(new AIResponse("분석 결과", "2026-07-06 10:00", 4, 2));
        when(reportWriter.write(any())).thenAnswer(invocation -> {
            var report = invocation.getArgument(0, dev.hongwp.suxai.model.dailyReport.DailyReport.class);
            return new ReportResult(true, report.getReportDate(), "reports/dailyReport/2026/07/2026-07-06.md", "ok", report);
        });

        DailyReportService service = new DailyReportService(facilityService, aiClient, reportWriter, "318");

        ReportResult result = service.generateDailyReport(null, null, null);

        assertTrue(result.isCreated());
        assertNotNull(result.getReport());
        assertEquals(LocalDate.now(), result.getReportDate());
        assertEquals("파주정수장", result.getReport().getFacilityName());
        verify(aiClient, times(1)).analyze(any());
        verify(reportWriter, times(1)).write(any());
    }
}
