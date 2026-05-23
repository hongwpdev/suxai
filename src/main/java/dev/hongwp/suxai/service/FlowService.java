package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.KwaterFlowApiClient;
import dev.hongwp.suxai.model.FlowRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlowService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final KwaterFlowApiClient apiClient;

    private final ConcurrentHashMap<String, List<FlowRecord>> dataCache   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant>          expiryCache = new ConcurrentHashMap<>();

    public FlowService(KwaterFlowApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<FlowRecord> getRecords(String sujCode) {
        Instant expiry = expiryCache.getOrDefault(sujCode, Instant.EPOCH);
        if (Instant.now().isAfter(expiry)) {
            List<FlowRecord> fresh = apiClient.fetchFlowRecords(sujCode);
            if (!fresh.isEmpty()) {
                dataCache.put(sujCode, fresh);
                expiryCache.put(sujCode, Instant.now().plusSeconds(CACHE_TTL_SECONDS));
            }
        }
        return dataCache.getOrDefault(sujCode, List.of());
    }
}
