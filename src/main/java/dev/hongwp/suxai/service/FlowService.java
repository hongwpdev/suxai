package dev.hongwp.suxai.service;

import dev.hongwp.suxai.client.KwaterFlowApiClient;
import dev.hongwp.suxai.model.FlowRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FlowService {

    private static final long CACHE_TTL_SECONDS = 300; // 5분

    private final KwaterFlowApiClient apiClient;

    private volatile List<FlowRecord> cachedData = List.of();
    private volatile Instant cacheExpiredAt = Instant.EPOCH;

    public FlowService(KwaterFlowApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<FlowRecord> getRecords() {
        if (Instant.now().isAfter(cacheExpiredAt)) {
            List<FlowRecord> fresh = apiClient.fetchFlowRecords();
            if (!fresh.isEmpty()) {
                cachedData = fresh;
                cacheExpiredAt = Instant.now().plusSeconds(CACHE_TTL_SECONDS);
            }
        }
        return cachedData;
    }
}
