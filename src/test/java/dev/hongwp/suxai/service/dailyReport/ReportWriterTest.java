package dev.hongwp.suxai.service.dailyReport;

import dev.hongwp.suxai.model.dailyReport.DailyReport;
import dev.hongwp.suxai.model.dailyReport.ReportListItem;
import dev.hongwp.suxai.model.dailyReport.ReportResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesMarkdownReport() throws Exception {
        ReportWriter writer = new ReportWriter(tempDir.toString());
        DailyReport report = sampleReport();

        ReportResult result = writer.write(report);

        assertTrue(result.isCreated());
        assertNotNull(result.getFilePath());
        assertTrue(Files.exists(Path.of(result.getFilePath())));
        assertTrue(Files.readString(Path.of(result.getFilePath())).contains("AI 분석 결과"));
        assertTrue(Files.readString(Path.of(result.getFilePath())).startsWith("# 2026-07-06 (월)"));
    }

    @Test
    void keepsExistingFileWhenMarkdownIsSame() throws Exception {
        ReportWriter writer = new ReportWriter(tempDir.toString());
        DailyReport report = sampleReport();

        ReportResult first = writer.write(report);
        ReportResult second = writer.write(sampleReport());
        String saved = Files.readString(Path.of(first.getFilePath()));

        assertTrue(first.isCreated());
        assertFalse(second.isCreated());
        assertEquals("동일 내용의 보고서가 이미 존재합니다.", second.getMessage());
        assertEquals(saved, Files.readString(Path.of(second.getFilePath())));
    }

    @Test
    void overwritesExistingFileWhenMarkdownIsDifferent() throws Exception {
        ReportWriter writer = new ReportWriter(tempDir.toString());

        ReportResult first = writer.write(sampleReport());
        DailyReport updatedReport = sampleReport();
        updatedReport.setMarkdown("이상 징후가 발견되었습니다.");
        updatedReport.setAnalyzedAt("2026-07-06 11:00");
        updatedReport.setWaterQualityCount(5);

        ReportResult second = writer.write(updatedReport);
        String overwritten = Files.readString(Path.of(second.getFilePath()));

        assertTrue(first.isCreated());
        assertTrue(second.isCreated());
        assertEquals("일일 보고서가 변경 내용으로 갱신되었습니다.", second.getMessage());
        assertTrue(overwritten.contains("이상 징후가 발견되었습니다."));
        assertTrue(overwritten.contains("2026-07-06 11:00"));
    }

    @Test
    void listsReportsAsTitleAndContent() {
        ReportWriter writer = new ReportWriter(tempDir.toString());
        writer.write(sampleReport());

        ReportListItem item = writer.list().get(0);

        assertEquals(LocalDate.of(2026, 7, 6), item.getReportDate());
        assertEquals("2026-07-06 (월)", item.getTitle());
        assertTrue(item.getContent().contains("- 보고일: 2026-07-06"));
        assertTrue(item.getContent().contains("## AI 분석 결과"));
    }

    private DailyReport sampleReport() {
        DailyReport report = new DailyReport();
        report.setReportDate(LocalDate.of(2026, 7, 6));
        report.setSujCode("318");
        report.setFacilityName("파주정수장");
        report.setTitle("파주정수장 일일 수질 리포트");
        report.setMarkdown("정상 상태입니다.");
        report.setAnalyzedAt("2026-07-06 10:00");
        report.setWaterQualityCount(3);
        report.setFlowCount(2);
        return report;
    }
}
