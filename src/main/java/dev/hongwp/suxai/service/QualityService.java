package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.QualityApiClient;
import dev.hongwp.suxai.model.QualityRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QualityService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final QualityApiClient apiClient;

    private final ConcurrentHashMap<String, List<QualityRecord>> dataCache   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant>                  expiryCache = new ConcurrentHashMap<>();

    public QualityService(QualityApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<QualityRecord> getRecords(String sujCode, String startDate, String endDate) {
        String cacheKey = sujCode + "|" + (startDate != null ? startDate : "") + "|" + (endDate != null ? endDate : "");
        Instant expiry = expiryCache.getOrDefault(cacheKey, Instant.EPOCH);
        if (Instant.now().isAfter(expiry)) {
            List<QualityRecord> fresh = apiClient.fetchRecords(sujCode, startDate, endDate);
            if (!fresh.isEmpty()) {
                dataCache.put(cacheKey, fresh);
                expiryCache.put(cacheKey, Instant.now().plusSeconds(CACHE_TTL_SECONDS));
            }
        }
        return dataCache.getOrDefault(cacheKey, List.of());
    }
}
