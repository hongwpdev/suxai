package dev.hongwp.suxai.service.dailyReport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DailyReportSchedulerTest {

    @Test
    void runsBatchForConfiguredTargets() {
        DailyReportService service = mock(DailyReportService.class);
        DailyReportScheduler scheduler = new DailyReportScheduler(service, "318, 311");

        scheduler.runDailyBatch();

        verify(service).generateDailyReport("318", null, null);
        verify(service).generateDailyReport("311", null, null);
        verifyNoMoreInteractions(service);
    }

    @Test
    void parsesConfiguredTargetCodes() {
        DailyReportService service = mock(DailyReportService.class);
        DailyReportScheduler scheduler = new DailyReportScheduler(service, "318, 311 , , 312");

        assertEquals(java.util.List.of("318", "311", "312"), scheduler.getTargetSujCodes());
    }
}
