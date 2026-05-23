package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.KwaterWaterQualityApiClient;
import dev.hongwp.suxai.model.WaterQualityRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WaterQualityService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final KwaterWaterQualityApiClient apiClient;

    private final ConcurrentHashMap<String, List<WaterQualityRecord>> dataCache  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant>                  expiryCache = new ConcurrentHashMap<>();

    public WaterQualityService(KwaterWaterQualityApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<WaterQualityRecord> getRecords(String sujCode) {
        Instant expiry = expiryCache.getOrDefault(sujCode, Instant.EPOCH);
        if (Instant.now().isAfter(expiry)) {
            List<WaterQualityRecord> fresh = apiClient.fetchRecords(sujCode);
            if (!fresh.isEmpty()) {
                dataCache.put(sujCode, fresh);
                expiryCache.put(sujCode, Instant.now().plusSeconds(CACHE_TTL_SECONDS));
            }
        }
        return dataCache.getOrDefault(sujCode, List.of());
    }
}
