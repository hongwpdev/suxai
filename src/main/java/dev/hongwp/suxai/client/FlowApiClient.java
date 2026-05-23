package dev.hongwp.suxai.client;

import dev.hongwp.suxai.model.FlowRecord;
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
import java.util.Objects;

@Component
public class FlowApiClient {

    private static final Logger log = LoggerFactory.getLogger(FlowApiClient.class);

    private static final String FLOW_URL = "https://apis.data.go.kr/B500001/rwis/waterFlux/waterFlux";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;
    private final String apiKey;

    public FlowApiClient(RestTemplate restTemplate,
                         @Value("${kwater.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public List<FlowRecord> fetchFlowRecords(String sujCode) {
        if (apiKey.isBlank()) {
            log.warn("K-water API 키 미설정 — 유량 데이터 없음");
            return Collections.emptyList();
        }

        try {
            LocalDateTime now   = LocalDateTime.now();
            LocalDateTime start = now.minusHours(1);

            String url = FLOW_URL
                + "?serviceKey=" + apiKey
                + "&stDt=" + start.format(DATE_FMT)
                + "&stTm=00"
                + "&edDt=" + now.format(DATE_FMT)
                + "&edTm=24"
                + "&sujCode=" + sujCode
                + "&pageNo=1"
                + "&numOfRows=100";

            log.info("유량 API 호출: sujCode={}", sujCode);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            return parseResponse(response);

        } catch (Exception e) {
            log.error("K-water 유량 API 호출 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<FlowRecord> parseResponse(Map<String, Object> response) {
        if (response == null) return Collections.emptyList();

        try {
            Object responseVal = response.get("response");
            if (!(responseVal instanceof Map)) {
                log.error("유량 API 에러 응답: {}", responseVal);
                return Collections.emptyList();
            }

            Map<?, ?> responseMap = (Map<?, ?>) responseVal;

            Object header = responseMap.get("header");
            if (header instanceof Map<?, ?> h) {
                if (!"00".equals(String.valueOf(h.get("resultCode")))) {
                    log.error("유량 API 오류코드={} 메시지={}", h.get("resultCode"), h.get("resultMsg"));
                    return Collections.emptyList();
                }
            }

            Map<String, Object> body = (Map<String, Object>) responseMap.get("body");
            if (body == null) return Collections.emptyList();

            Object itemsObj = body.get("items");
            if (itemsObj == null || (itemsObj instanceof String s && s.isBlank())) {
                log.info("유량 API 조회 결과 없음");
                return Collections.emptyList();
            }

            Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
            Object itemObj = itemsMap.get("item");

            List<Map<String, Object>> items;
            if (itemObj instanceof List<?> list) {
                items = (List<Map<String, Object>>) list;
            } else if (itemObj instanceof Map) {
                items = List.of((Map<String, Object>) itemObj);
            } else {
                log.warn("유량 API item 구조 불명: {}", itemObj);
                return Collections.emptyList();
            }

            log.info("유량 API 아이템 {}건 수신", items.size());
            return items.stream().map(this::toFlowRecord).filter(Objects::nonNull).toList();

        } catch (Exception e) {
            log.error("유량 API 응답 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private FlowRecord toFlowRecord(Map<String, Object> item) {
        try {
            String divTypeCode = str(item, "dataItemDiv");
            String divType     = "M".equals(divTypeCode) ? "순간" : "D".equals(divTypeCode) ? "적산" : divTypeCode;
            return new FlowRecord(
                str(item, "fcltyMngNo"), str(item, "fcltyNm"),
                str(item, "dataItemDesc"), str(item, "dataVal"),
                str(item, "itemUnit"), formatDate(str(item, "occrrncDt")), divType
            );
        } catch (Exception e) {
            log.warn("유량 항목 변환 실패: {}", item);
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
