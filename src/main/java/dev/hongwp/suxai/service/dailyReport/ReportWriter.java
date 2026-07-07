package dev.hongwp.suxai.service.dailyReport;

import dev.hongwp.suxai.model.dailyReport.DailyReport;
import dev.hongwp.suxai.model.dailyReport.ReportListItem;
import dev.hongwp.suxai.model.dailyReport.ReportResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import java.time.format.DateTimeFormatter;

@Service
public class ReportWriter {

    private static final DateTimeFormatter YEAR_FMT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String baseDir;

    public ReportWriter(@Value("${daily-report.base-dir:reports/dailyReport}") String baseDir) {
        this.baseDir = baseDir;
    }

    public ReportResult write(DailyReport report) {
        LocalDate reportDate = report.getReportDate();
        Path reportPath = buildReportPath(reportDate);
        String markdown = buildMarkdown(report);
        boolean existedBefore = Files.exists(reportPath);

        try {
            Files.createDirectories(reportPath.getParent());

            if (existedBefore) {
                String existingMarkdown = Files.readString(reportPath, StandardCharsets.UTF_8);
                if (existingMarkdown.equals(markdown)) {
                    report.setMarkdown(existingMarkdown);
                    return new ReportResult(
                        false,
                        reportDate,
                        reportPath.toString(),
                        "동일 내용의 보고서가 이미 존재합니다.",
                        report
                    );
                }
            }

            Files.writeString(reportPath, markdown, StandardCharsets.UTF_8);
            report.setMarkdown(markdown);

            return new ReportResult(
                true,
                reportDate,
                reportPath.toString(),
                existedBefore ? "일일 보고서가 변경 내용으로 갱신되었습니다." : "일일 보고서가 생성되었습니다.",
                report
            );
        } catch (IOException e) {
            throw new IllegalStateException("보고서 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public ReportResult read(LocalDate reportDate) {
        Path reportPath = buildReportPath(reportDate);
        if (!Files.exists(reportPath)) {
            return new ReportResult(false, reportDate, reportPath.toString(), "보고서가 존재하지 않습니다.", null);
        }

        try {
            DailyReport report = new DailyReport();
            report.setReportDate(reportDate);
            report.setMarkdown(Files.readString(reportPath, StandardCharsets.UTF_8));

            return new ReportResult(
                false,
                reportDate,
                reportPath.toString(),
                "보고서를 조회했습니다.",
                report
            );
        } catch (IOException e) {
            throw new IllegalStateException("보고서 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public List<ReportListItem> list() {
        Path rootPath = Path.of(baseDir);
        if (!Files.exists(rootPath)) {
            return List.of();
        }

        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            return pathStream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".md"))
                .sorted(Comparator.reverseOrder())
                .map(this::toReportListItem)
                .toList();
        } catch (IOException e) {
            throw new IllegalStateException("보고서 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private Path buildReportPath(LocalDate reportDate) {
        return Path.of(
            baseDir,
            reportDate.format(YEAR_FMT),
            reportDate.format(MONTH_FMT),
            reportDate.format(FILE_FMT) + ".md"
        );
    }

    private String buildMarkdown(DailyReport report) {
        String title = formatTitle(report.getReportDate());
        return """
            # %s
            
            - 보고일: %s
            - 정수장: %s
            - 수계코드: %s
            - AI 분석 기준 시각: %s
            - 수질 데이터 건수: %d
            - 유량 데이터 건수: %d
            
            ## AI 분석 결과
            
            %s
            """.formatted(
            title,
            report.getReportDate(),
            safe(report.getFacilityName()),
            safe(report.getSujCode()),
            safe(report.getAnalyzedAt()),
            report.getWaterQualityCount(),
            report.getFlowCount(),
            safe(report.getMarkdown())
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private ReportListItem toReportListItem(Path reportPath) {
        try {
            String markdown = Files.readString(reportPath, StandardCharsets.UTF_8);
            LocalDate reportDate = extractReportDate(reportPath);
            String title = extractTitle(markdown, reportDate);
            String content = extractContent(markdown);
            return new ReportListItem(reportDate, title, content, reportPath.toString());
        } catch (IOException e) {
            throw new IllegalStateException("보고서 파일 읽기에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private LocalDate extractReportDate(Path reportPath) {
        String fileName = reportPath.getFileName().toString().replace(".md", "");
        return LocalDate.parse(fileName, FILE_FMT);
    }

    private String extractTitle(String markdown, LocalDate reportDate) {
        String[] lines = markdown.split("\\R", 2);
        if (lines.length > 0 && lines[0].startsWith("# ")) {
            return lines[0].substring(2).trim();
        }
        return formatTitle(reportDate);
    }

    private String extractContent(String markdown) {
        String[] parts = markdown.split("\\R\\R", 2);
        if (parts.length < 2) {
            return markdown;
        }
        return parts[1].trim();
    }

    private String formatTitle(LocalDate reportDate) {
        return "%s (%s)".formatted(
            reportDate.format(FILE_FMT),
            reportDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        );
    }
}
