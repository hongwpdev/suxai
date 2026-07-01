package dev.hongwp.suxai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hongwp.suxai.client.KakaoApiClient;
import dev.hongwp.suxai.model.FacilityInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final KakaoApiClient  kakaoClient;
    private final FacilityService facilityService;
    private final List<Map<String, Object>> mappingRules;

    // 시작 시 geocode 결과를 캐시
    private final List<CachedFacility> facilityCache = new ArrayList<>();

    public record FacilityMatch(FacilityInfo facility, String address, double lat, double lng, double distanceKm) {}

    private record CachedFacility(FacilityInfo facility, String address, double lat, double lng) {}

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

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void initCache() {
        List<FacilityInfo> facilities = facilityService.getFacilities();
        for (Map<String, Object> rule : mappingRules) {
            String keyword = (String) rule.get("keyword");
            String address = (String) rule.getOrDefault("address", "");

            FacilityInfo matched = facilities.stream()
                .filter(f -> f.getFacilityName().contains(keyword))
                .findFirst()
                .orElse(null);
            if (matched == null) {
                log.warn("정수장 매핑 실패 - keyword: {}", keyword);
                continue;
            }

            double[] coords = kakaoClient.geocode(address);
            if (coords.length == 2) {
                facilityCache.add(new CachedFacility(matched, address, coords[0], coords[1]));
                log.info("정수장 캐시: {} → {},{}", matched.getFacilityName(), coords[0], coords[1]);
            } else {
                log.warn("좌표 조회 실패 - {}: {}", keyword, address);
            }
        }
        log.info("정수장 위치 캐시 완료: {}개", facilityCache.size());
    }

    /**
     * 사용자 입력 위치에서 가까운 정수장을 거리순으로 반환
     */
    public List<FacilityMatch> findNearestFacilities(String query, int limit) {
        double[] userCoords = kakaoClient.geocode(query);
        if (userCoords.length != 2) {
            // geocode 실패 시 키워드 검색으로 fallback
            userCoords = kakaoClient.geocodeByKeyword(query);
        }
        if (userCoords.length != 2) {
            log.warn("위치 변환 실패: {}", query);
            return List.of();
        }

        double userLat = userCoords[0];
        double userLng = userCoords[1];

        return facilityCache.stream()
            .map(c -> new FacilityMatch(
                c.facility(), c.address(), c.lat(), c.lng(),
                haversineKm(userLat, userLng, c.lat(), c.lng())))
            .sorted(Comparator.comparingDouble(FacilityMatch::distanceKm))
            .limit(limit)
            .toList();
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
