package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.FacilityApiClient;
import dev.hongwp.suxai.model.FacilityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FacilityService {

    private static final Logger log = LoggerFactory.getLogger(FacilityService.class);
    private static final long CACHE_TTL_SECONDS = 86_400; // 24시간

    private final FacilityApiClient facilityClient;

    private volatile List<FacilityInfo> cached = List.of();
    private volatile Instant cacheExpiredAt = Instant.EPOCH;

    public FacilityService(FacilityApiClient facilityClient) {
        this.facilityClient = facilityClient;
    }

    public List<FacilityInfo> getFacilities() {
        if (Instant.now().isAfter(cacheExpiredAt)) {
            try {
                List<FacilityInfo> fresh = facilityClient.fetchFacilities();
                if (!fresh.isEmpty()) {
                    cached = fresh;
                    cacheExpiredAt = Instant.now().plusSeconds(CACHE_TTL_SECONDS);
                    log.info("전국 정수장 {}개 캐시 완료", fresh.size());
                }
            } catch (Exception e) {
                log.warn("정수장 목록 갱신 실패: {}", e.getMessage());
            }
        }
        return cached;
    }
}
