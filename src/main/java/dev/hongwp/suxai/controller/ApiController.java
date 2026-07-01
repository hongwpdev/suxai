package dev.hongwp.suxai.controller;

import dev.hongwp.suxai.model.*;
import dev.hongwp.suxai.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final QualityService wqService;
    private final FlowService         flService;
    private final AnalysisService     analysisService;
    private final FacilityService     facilityService;
    private final AddressService      addressService;
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

    public ApiController(QualityService wqService,
                         FlowService flService,
                         AnalysisService analysisService,
                         FacilityService facilityService,
                         AddressService addressService,
                         @Value("${kwater.suj.code}") String defaultSujCode) {
        this.wqService       = wqService;
        this.flService       = flService;
        this.analysisService = analysisService;
        this.facilityService = facilityService;
        this.addressService  = addressService;
        this.defaultSujCode  = defaultSujCode;
    }

    @GetMapping("/facilities")
    public List<FacilityInfo> facilities() {
        return facilityService.getFacilities();
    }

    @GetMapping("/waterQuality")
    public List<QualityRecord> waterQuality(
            @RequestParam(required = false) String sujCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return wqService.getRecords(sujCode != null ? sujCode : defaultSujCode, startDate, endDate);
    }

    @GetMapping("/waterFlow")
    public List<FlowRecord> flow(
            @RequestParam(required = false) String sujCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return flService.getRecords(sujCode != null ? sujCode : defaultSujCode, startDate, endDate);
    }

    @GetMapping("/analysis")
    public AnalysisResult analysis(
            @RequestParam(required = false) String sujCode,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return analysisService.analyze(sujCode != null ? sujCode : defaultSujCode, startDate, endDate);
    }

    @GetMapping("/news")
    public List<NewsItem> news() {
        return NEWS;
    }

    @GetMapping("/findFacility")
    public ResponseEntity<?> findFacility(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int limit) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "주소를 입력해주세요."));
        }
        List<AddressService.FacilityMatch> results = addressService.findNearestFacilities(query, limit);
        if (results.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "위치를 찾을 수 없습니다. 더 구체적인 주소를 입력해주세요."));
        }
        List<Map<String, Object>> list = results.stream()
            .map(m -> {
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("sujCode",      m.facility().getSujCode());
                item.put("facilityName", m.facility().getFacilityName());
                item.put("address",      m.address());
                item.put("lat",          m.lat());
                item.put("lng",          m.lng());
                item.put("distanceKm",   Math.round(m.distanceKm() * 10.0) / 10.0);
                return item;
            })
            .toList();
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("query",   query);
        resp.put("results", list);
        return ResponseEntity.ok(resp);
    }
}
