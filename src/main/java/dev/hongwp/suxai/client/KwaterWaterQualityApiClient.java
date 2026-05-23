package dev.hongwp.suxai.client;

import dev.hongwp.suxai.model.WaterQualityRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class KwaterWaterQualityApiClient {

    private static final Logger log = LoggerFactory.getLogger(KwaterWaterQualityApiClient.class);

    private static final String WQ_URL = "https://apis.data.go.kr/B500001/rwis/waterQuality/list";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String apiKey;

    public KwaterWaterQualityApiClient(RestTemplate restTemplate,
                                       @Value("${kwater.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public List<WaterQualityRecord> fetchRecords(String sujCode) {
        if (apiKey.isBlank()) {
            log.warn("K-water API 키 미설정 — 수질 데이터 없음");
            return Collections.emptyList();
        }

        try {
            LocalDateTime now   = LocalDateTime.now();
            LocalDateTime start = now.minusHours(1);

            String url = WQ_URL
                + "?serviceKey=" + apiKey
                + "&stDt=" + start.format(DATE_FMT)
                + "&stTm=00"
                + "&edDt=" + now.format(DATE_FMT)
                + "&edTm=24"
                + "&sujCode=" + sujCode
                + "&pageNo=1"
                + "&numOfRows=100";

            log.info("수질 API 호출: sujCode={}", sujCode);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            return parseRecords(response);

        } catch (Exception e) {
            log.error("K-water 수질 API 호출 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Map<String, Object> response) {
        if (response == null) return Collections.emptyList();
        try {
            Object responseVal = response.get("response");
            if (!(responseVal instanceof Map)) return Collections.emptyList();

            Map<?, ?> responseMap = (Map<?, ?>) responseVal;

            Object header = responseMap.get("header");
            if (header instanceof Map<?, ?> h) {
                if (!"00".equals(String.valueOf(h.get("resultCode")))) return Collections.emptyList();
            }

            Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
            if (body == null) return Collections.emptyList();

            Object itemsObj = body.get("items");
            if (itemsObj == null || (itemsObj instanceof String s && s.isBlank())) return Collections.emptyList();

            Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
            Object itemObj = itemsMap.get("item");

            if (itemObj instanceof List<?> list) return (List<Map<String, Object>>) list;
            if (itemObj instanceof Map)           return List.of((Map<String, Object>) itemObj);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("정수장 목록 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<WaterQualityRecord> parseRecords(Map<String, Object> response) {
        List<Map<String, Object>> items = extractItems(response);
        if (items.isEmpty()) {
            log.info("수질 API 조회 결과 없음");
            return Collections.emptyList();
        }
        log.info("수질 API 아이템 {}건 수신", items.size());
        return items.stream().map(this::toRecord).filter(Objects::nonNull).toList();
    }

    private WaterQualityRecord toRecord(Map<String, Object> item) {
        try {
            return new WaterQualityRecord(
                str(item, "fcltyMngNo"),
                str(item, "fcltyMngNm"),
                str(item, "fcltyAddr"),
                str(item, "liIndDivName"),
                formatDate(str(item, "occrrncDt")),
                str(item, "phVal"),  str(item, "phUnit"),
                str(item, "tbVal"),  str(item, "tbUnit"),
                str(item, "clVal"),  str(item, "clUnit")
            );
        } catch (Exception e) {
            log.warn("수질 항목 변환 실패: {}", item);
            return null;
        }
    }

    private String formatDate(String dt) {
        if (dt == null || dt.length() < 10) return dt;
        return dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-"
             + dt.substring(6, 8) + " " + dt.substring(8, 10) + "시";
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString().trim();
    }
}
