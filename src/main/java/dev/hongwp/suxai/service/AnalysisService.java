package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.GroqApiClient;
import dev.hongwp.suxai.model.AnalysisResult;
import dev.hongwp.suxai.model.FlowRecord;
import dev.hongwp.suxai.model.WaterQualityRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private static final long CACHE_TTL_SECONDS = 300;
    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WaterQualityService wqService;
    private final FlowService flService;
    private final GroqApiClient groqClient;

    private final ConcurrentHashMap<String, AnalysisResult> cache    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant>        expiry   = new ConcurrentHashMap<>();

    public AnalysisService(WaterQualityService wqService,
                           FlowService flService,
                           GroqApiClient groqClient) {
        this.wqService = wqService;
        this.flService = flService;
        this.groqClient = groqClient;
    }

    public AnalysisResult analyze(String sujCode) {
        if (!groqClient.isConfigured()) {
            return new AnalysisResult(
                "Groq API 키가 설정되지 않았습니다.\n" +
                "application.properties의 groq.api.key를 설정해 주세요.",
                now(), 0, 0
            );
        }

        Instant exp = expiry.get(sujCode);
        if (exp != null && Instant.now().isBefore(exp)) {
            return cache.get(sujCode);
        }

        List<WaterQualityRecord> wqList = wqService.getRecords(sujCode);
        List<FlowRecord> flList = flService.getRecords(sujCode);

        String prompt = buildPrompt(wqList, flList);
        String result = groqClient.analyze(prompt);

        AnalysisResult analysisResult = new AnalysisResult(result, now(), wqList.size(), flList.size());
        cache.put(sujCode, analysisResult);
        expiry.put(sujCode, Instant.now().plusSeconds(CACHE_TTL_SECONDS));
        return analysisResult;
    }

    private String buildPrompt(List<WaterQualityRecord> wqList, List<FlowRecord> flList) {
        // fcltyMngNo 기준으로 인덱싱
        Map<String, WaterQualityRecord> wqMap = wqList.stream()
            .collect(Collectors.toMap(
                WaterQualityRecord::getId,
                r -> r,
                (a, b) -> a  // 중복 시 첫 번째 유지
            ));

        Map<String, List<FlowRecord>> flMap = flList.stream()
            .collect(Collectors.groupingBy(FlowRecord::getId));

        Set<String> joinedIds  = new LinkedHashSet<>(wqMap.keySet());
        joinedIds.retainAll(flMap.keySet());

        Set<String> wqOnlyIds = new LinkedHashSet<>(wqMap.keySet());
        wqOnlyIds.removeAll(flMap.keySet());

        Set<String> flOnlyIds = new LinkedHashSet<>(flMap.keySet());
        flOnlyIds.removeAll(wqMap.keySet());

        StringBuilder sb = new StringBuilder();
        sb.append("당신은 수처리 전문가입니다. 아래는 한국수자원공사 정수장의 실시간 수질·유량 데이터입니다.\n\n");

        // ── 1. 수질·유량 모두 있는 시설 (상관관계 분석 가능) ──────────────
        if (!joinedIds.isEmpty()) {
            sb.append("## 수질·유량 통합 데이터 (상관관계 분석 대상)\n");
            joinedIds.stream().limit(10).forEach(id -> {
                WaterQualityRecord wq = wqMap.get(id);
                List<FlowRecord> flows = flMap.get(id);

                sb.append(String.format("### %s (시설번호: %s)%n", wq.getFacilityName(), id));
                sb.append(String.format("  [수질] pH=%s | 탁도=%s NTU | 잔류염소=%s mg/L | %s%n",
                    wq.getPhVal(), wq.getTbVal(), wq.getClVal(), wq.getMeasuredAt()));
                flows.forEach(f ->
                    sb.append(String.format("  [유량] %s %s %s (%s) | %s%n",
                        f.getDescription(), f.getValue(), f.getUnit(), f.getDivType(), f.getMeasuredAt()))
                );
                sb.append("\n");
            });
        }

        // ── 2. 수질 데이터만 있는 시설 ────────────────────────────────────
        if (!wqOnlyIds.isEmpty()) {
            sb.append("## 수질 데이터만 있는 시설\n");
            sb.append("시설명 | pH | 탁도(NTU) | 잔류염소(mg/L) | 측정시간\n");
            wqOnlyIds.stream().limit(10).forEach(id -> {
                WaterQualityRecord r = wqMap.get(id);
                sb.append(String.format("%s | %s | %s | %s | %s%n",
                    r.getFacilityName(), r.getPhVal(), r.getTbVal(), r.getClVal(), r.getMeasuredAt()));
            });
            sb.append("\n");
        }

        // ── 3. 유량 데이터만 있는 시설 ────────────────────────────────────
        if (!flOnlyIds.isEmpty()) {
            sb.append("## 유량 데이터만 있는 시설\n");
            sb.append("시설명 | 항목 | 측정값 | 단위 | 유형 | 측정시간\n");
            flOnlyIds.stream().limit(10).forEach(id ->
                flMap.get(id).forEach(r ->
                    sb.append(String.format("%s | %s | %s | %s | %s | %s%n",
                        r.getFacilityName(), r.getDescription(),
                        r.getValue(), r.getUnit(), r.getDivType(), r.getMeasuredAt()))
                )
            );
            sb.append("\n");
        }

        // ── 4. 데이터 없음 ────────────────────────────────────────────────
        if (wqList.isEmpty() && flList.isEmpty()) {
            sb.append("현재 수집된 데이터가 없습니다.\n\n");
        }

        sb.append("""
위 데이터를 분석하여 아래 3가지를 한국어로 간결하게 답변해주세요.

**1. 전체 현황**
현재 수질·유량 상태를 2~3줄로 요약하세요.

**2. 이상징후**
주의 또는 위험이 필요한 시설과 이유를 나열하세요. 없으면 "이상징후 없음"으로 답변하세요.
수질 기준: pH 6.5~8.5 정상 / 탁도 0.5 NTU 이하 정상 / 잔류염소 0.1~2.0 mg/L 정상
수질·유량 통합 시설의 경우: 유량 변화가 수질(특히 탁도·잔류염소)에 미치는 영향도 분석하세요.

**3. 권고사항**
조치가 필요한 경우 우선순위를 매겨 제시하세요. 없으면 "특이사항 없음"으로 답변하세요.
""");

        return sb.toString();
    }

    private String now() {
        return LocalDateTime.now().format(DT_FMT);
    }
}
