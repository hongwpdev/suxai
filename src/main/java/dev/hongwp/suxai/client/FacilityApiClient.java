package dev.hongwp.suxai.client;

import dev.hongwp.suxai.model.FacilityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class FacilityApiClient {

    private static final Logger log = LoggerFactory.getLogger(FacilityApiClient.class);
    private static final String BASE_URL =
        "https://apis.data.go.kr/B500001/rwis/waterQuality/fcltylist/codelist" +
        "?fcltyDivCode=2&numOfRows=100&pageNo=1&serviceKey=";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public FacilityApiClient(RestTemplate restTemplate,
                             @Value("${kwater.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public List<FacilityInfo> fetchFacilities() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(BASE_URL + apiKey, Map.class);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("정수장 목록 API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<FacilityInfo> parseResponse(Map<String, Object> response) {
        if (response == null) return Collections.emptyList();
        try {
            Map<String, Object> responseMap = (Map<String, Object>) response.get("response");
            if (responseMap == null) return Collections.emptyList();

            Map<String, Object> header = (Map<String, Object>) responseMap.get("header");
            if (header != null && !"00".equals(String.valueOf(header.get("resultCode")))) {
                log.warn("정수장 목록 API 오류 응답: {}", header.get("resultMsg"));
                return Collections.emptyList();
            }

            Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
            if (body == null) return Collections.emptyList();

            Map<String, Object> items = (Map<String, Object>) body.get("items");
            if (items == null) return Collections.emptyList();

            Object itemObj = items.get("item");
            List<Map<String, Object>> itemList;
            if (itemObj instanceof List<?> list) {
                itemList = (List<Map<String, Object>>) list;
            } else if (itemObj instanceof Map) {
                itemList = List.of((Map<String, Object>) itemObj);
            } else {
                return Collections.emptyList();
            }

            List<FacilityInfo> result = itemList.stream()
                .map(item -> {
                    String name    = str(item, "fcltyMngNm");
                    String sujCode = str(item, "sujCode");
                    return new FacilityInfo(sujCode, name, "");
                })
                .filter(f -> !f.getSujCode().isBlank() && !f.getFacilityName().isBlank())
                .toList();

            log.info("정수장 목록 {}개 파싱 완료", result.size());
            return result;

        } catch (Exception e) {
            log.error("정수장 목록 JSON 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString().trim();
    }
}
