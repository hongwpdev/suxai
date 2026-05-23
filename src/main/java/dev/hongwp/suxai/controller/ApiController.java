package dev.hongwp.suxai.controller;

import dev.hongwp.suxai.model.*;
import dev.hongwp.suxai.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final WaterQualityService wqService;
    private final FlowService         flService;
    private final AnalysisService     analysisService;
    private final FacilityService     facilityService;
    private final String              defaultSujCode;

    private static final List<NewsItem> NEWS = List.of(
        new NewsItem("crit", "충북 화학공장 폐수 무단방류 의혹 — 인근 하천 전기전도도 정상치 3배 초과"),
        new NewsItem("warn", "낙동강 중류 집중호우로 탁도 기준치(50 NTU) 초과 관측"),
        new NewsItem("warn", "한강 수계 봄철 녹조 발생 징후 — DO 수치 감소 동반"),
        new NewsItem("ok",   "충주댐 방류량 정상화 — 한강 유량 안정 단계 진입"),
        new NewsItem("ok",   "다음 주 전국 강수량 평년 수준 예보 — 하천 수위 안정 전망"),
        new NewsItem("warn", "임진강 연천 구간 수위 소폭 상승 — 기상청 강수 영향 분석 중"),
        new NewsItem("crit", "영산강 나주 구간 DO 4.8mg/L 기록 — 어류 폐사 우려")
    );

    public ApiController(WaterQualityService wqService,
                         FlowService flService,
                         AnalysisService analysisService,
                         FacilityService facilityService,
                         @Value("${kwater.suj.code}") String defaultSujCode) {
        this.wqService       = wqService;
        this.flService       = flService;
        this.analysisService = analysisService;
        this.facilityService = facilityService;
        this.defaultSujCode  = defaultSujCode;
    }

    @GetMapping("/facilities")
    public List<FacilityInfo> facilities() {
        return facilityService.getFacilities();
    }

    @GetMapping("/waterQuality")
    public List<WaterQualityRecord> waterQuality(
            @RequestParam(required = false) String sujCode) {
        return wqService.getRecords(sujCode != null ? sujCode : defaultSujCode);
    }

    @GetMapping("/waterFlow")
    public List<FlowRecord> flow(
            @RequestParam(required = false) String sujCode) {
        return flService.getRecords(sujCode != null ? sujCode : defaultSujCode);
    }

    @GetMapping("/analysis")
    public AnalysisResult analysis(
            @RequestParam(required = false) String sujCode) {
        return analysisService.analyze(sujCode != null ? sujCode : defaultSujCode);
    }

    @GetMapping("/news")
    public List<NewsItem> news() {
        return NEWS;
    }
}
