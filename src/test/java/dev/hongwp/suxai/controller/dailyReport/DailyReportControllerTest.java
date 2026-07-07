package dev.hongwp.suxai.controller.dailyReport;

import dev.hongwp.suxai.model.dailyReport.ReportListItem;
import dev.hongwp.suxai.model.dailyReport.ReportResult;
import dev.hongwp.suxai.service.dailyReport.DailyReportService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyReportControllerTest {

    @Test
    void runsDailyReportManually() throws Exception {
        DailyReportService service = mock(DailyReportService.class);
        when(service.generateDailyReport("318", "2026-07-06", "2026-07-06"))
            .thenReturn(new ReportResult(true, LocalDate.of(2026, 7, 6), "reports/dailyReport/2026/07/2026-07-06.md", "ok", null));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DailyReportController(service)).build();

        mockMvc.perform(post("/api/daily-report/run")
                .param("sujCode", "318")
                .param("startDate", "2026-07-06")
                .param("endDate", "2026-07-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created").value(true))
            .andExpect(jsonPath("$.filePath").value("reports/dailyReport/2026/07/2026-07-06.md"));

        verify(service).generateDailyReport("318", "2026-07-06", "2026-07-06");
    }

    @Test
    void readsDailyReportByDate() throws Exception {
        DailyReportService service = mock(DailyReportService.class);
        when(service.getReport(LocalDate.of(2026, 7, 6)))
            .thenReturn(new ReportResult(false, LocalDate.of(2026, 7, 6), "reports/dailyReport/2026/07/2026-07-06.md", "ok", null));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DailyReportController(service)).build();

        mockMvc.perform(get("/api/daily-report")
                .param("date", "2026-07-06"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reportDate[0]").value(2026))
            .andExpect(jsonPath("$.reportDate[1]").value(7))
            .andExpect(jsonPath("$.reportDate[2]").value(6));

        verify(service).getReport(LocalDate.of(2026, 7, 6));
    }

    @Test
    void listsDailyReports() throws Exception {
        DailyReportService service = mock(DailyReportService.class);
        when(service.listReports()).thenReturn(List.of(
            new ReportListItem(LocalDate.of(2026, 7, 6), "2026-07-06 (월)", "본문", "reports/dailyReport/2026/07/2026-07-06.md")
        ));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DailyReportController(service)).build();

        mockMvc.perform(get("/api/daily-report/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("2026-07-06 (월)"))
            .andExpect(jsonPath("$[0].content").value("본문"));

        verify(service).listReports();
    }
}
