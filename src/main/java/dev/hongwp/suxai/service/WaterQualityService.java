package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.KwaterWaterQualityApiClient;
import dev.hongwp.suxai.model.WaterQualityRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WaterQualityService {

    private static final long CACHE_TTL_SECONDS = 300; // 5분

    private final KwaterWaterQualityApiClient apiClient;

    private volatile List<WaterQualityRecord> cachedData = List.of();
    private volatile Instant cacheExpiredAt = Instant.EPOCH;

    public WaterQualityService(KwaterWaterQualityApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<WaterQualityRecord> getRecords() {
        if (Instant.now().isAfter(cacheExpiredAt)) {
            List<WaterQualityRecord> fresh = apiClient.fetchRecords();
            if (!fresh.isEmpty()) {
                cachedData = fresh;
                cacheExpiredAt = Instant.now().plusSeconds(CACHE_TTL_SECONDS);
            }
        }
        return cachedData;
    }
}
