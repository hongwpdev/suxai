package dev.hongwp.suxai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hongwp.suxai.client.KakaoApiClient;
import dev.hongwp.suxai.model.FacilityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final KakaoApiClient   kakaoClient;
    private final FacilityService  facilityService;
    private final List<Map<String, Object>> mappingRules;

    @SuppressWarnings("unchecked")
    public AddressService(KakaoApiClient kakaoClient,
                          FacilityService facilityService) throws Exception {
        this.kakaoClient     = kakaoClient;
        this.facilityService = facilityService;

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> root = om.readValue(
            new ClassPathResource("facility-mapping.json").getInputStream(), Map.class);
        this.mappingRules = (List<Map<String, Object>>) root.get("mapping");
    }

    /**
     * 주소 입력 → 매칭된 정수장 반환. 없으면 Optional.empty().
     */
    public Optional<FacilityInfo> findFacilityByAddress(String query) {
        // 1. 카카오 API로 행정구역 추출
        Map<String, String> region = kakaoClient.searchRegion(query);
        String sido    = region.getOrDefault("sido",    "");
        String sigungu = region.getOrDefault("sigungu", "");

        log.info("주소 검색 - query={}, sido={}, sigungu={}", query, sido, sigungu);

        // 2. 매핑 규칙에서 키워드 찾기 (시군구 우선, 시도 차선)
        String keyword = findKeyword(sigungu);
        if (keyword.isEmpty()) keyword = findKeyword(sido);
        if (keyword.isEmpty()) keyword = findKeywordByInput(query); // 입력값 직접 검색

        log.info("매핑 키워드: {}", keyword);

        if (keyword.isEmpty()) return Optional.empty();

        // 3. 실시간 정수장 목록에서 키워드 매칭
        List<FacilityInfo> facilities = facilityService.getFacilities();
        final String kw = keyword;
        return facilities.stream()
            .filter(f -> f.getFacilityName().contains(kw))
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    private String findKeyword(String regionName) {
        if (regionName.isBlank()) return "";
        for (Map<String, Object> rule : mappingRules) {
            List<String> regions = (List<String>) rule.get("regions");
            boolean match = regions.stream().anyMatch(r ->
                regionName.contains(r) || r.contains(regionName.replace("시", "").replace("구", "").replace("군", ""))
            );
            if (match) return (String) rule.get("keyword");
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String findKeywordByInput(String input) {
        for (Map<String, Object> rule : mappingRules) {
            List<String> regions = (List<String>) rule.get("regions");
            boolean match = regions.stream().anyMatch(r ->
                input.contains(r) || r.contains(input.replaceAll("[시구군도]$", ""))
            );
            if (match) return (String) rule.get("keyword");
        }
        return "";
    }
}
