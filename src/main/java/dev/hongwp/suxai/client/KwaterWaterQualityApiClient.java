package dev.hongwp.suxai.client;

import dev.hongwp.suxai.model.WaterQualityRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class KwaterWaterQualityApiClient {

    private static final Logger log = LoggerFactory.getLogger(KwaterWaterQualityApiClient.class);

    private static final String WQ_URL = "https://apis.data.go.kr/B500001/rwis/waterQuality/list";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String sujCode;

    public KwaterWaterQualityApiClient(RestTemplate restTemplate,
                                       @Value("${kwater.api.key}") String apiKey,
                                       @Value("${kwater.suj.code}") String sujCode) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.sujCode = sujCode;
    }

    public List<WaterQualityRecord> fetchRecords() {
        if (apiKey.equals("YOUR_API_KEY_HERE") || apiKey.isBlank()) {
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

            log.info("수질 API 호출: {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            return parseResponse(response);

        } catch (Exception e) {
            log.error("K-water 수질 API 호출 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<WaterQualityRecord> parseResponse(Map<String, Object> response) {
        if (response == null) return Collections.emptyList();

        try {
            Object responseVal = response.get("response");
            if (!(responseVal instanceof Map)) {
                log.error("수질 API 에러 응답: {}", responseVal);
                return Collections.emptyList();
            }

            Map<?, ?> responseMap = (Map<?, ?>) responseVal;

            Object header = responseMap.get("header");
            if (header instanceof Map<?, ?> h) {
                String resultCode = String.valueOf(h.get("resultCode"));
                if (!"00".equals(resultCode)) {
                    log.error("수질 API 오류코드={} 메시지={}", resultCode, h.get("resultMsg"));
                    return Collections.emptyList();
                }
            }

            Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
            if (body == null) return Collections.emptyList();

            Object itemsObj = body.get("items");
            if (itemsObj == null || (itemsObj instanceof String s && s.isBlank())) {
                log.info("수질 API 조회 결과 없음");
                return Collections.emptyList();
            }

            // items = { item: [...] } 구조
            Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
            Object itemObj = itemsMap.get("item");

            List<Map<String, Object>> items;
            if (itemObj instanceof List<?> list) {
                items = (List<Map<String, Object>>) list;
            } else if (itemObj instanceof Map) {
                items = List.of((Map<String, Object>) itemObj);
            } else {
                log.warn("수질 API item 구조 불명: {}", itemObj);
                return Collections.emptyList();
            }

            log.info("수질 API 아이템 {}건 수신", items.size());

            return items.stream()
                .map(this::toRecord)
                .filter(r -> r != null)
                .toList();

        } catch (Exception e) {
            log.error("수질 API 응답 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private WaterQualityRecord toRecord(Map<String, Object> item) {
        try {
            String id           = str(item, "fcltyMngNo");
            String facilityName = str(item, "fcltyMngNm");
            String address      = str(item, "fcltyAddr");
            String divName      = str(item, "liIndDivName");
            String measuredAt   = formatDate(str(item, "occrrncDt"));
            String phVal        = str(item, "phVal");
            String phUnit       = str(item, "phUnit");
            String tbVal        = str(item, "tbVal");
            String tbUnit       = str(item, "tbUnit");
            String clVal        = str(item, "clVal");
            String clUnit       = str(item, "clUnit");

            return new WaterQualityRecord(id, facilityName, address, divName, measuredAt,
                                          phVal, phUnit, tbVal, tbUnit, clVal, clUnit);
        } catch (Exception e) {
            log.warn("수질 항목 변환 실패: {}", item);
            return null;
        }
    }

    // "2026052313" → "2026-05-23 13시"
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
