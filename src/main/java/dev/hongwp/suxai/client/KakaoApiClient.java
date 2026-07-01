package dev.hongwp.suxai.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class KakaoApiClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiClient.class);
    private static final String ADDRESS_URL =
        "https://dapi.kakao.com/v2/local/search/address.json?query=";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public KakaoApiClient(RestTemplate restTemplate,
                          @Value("${kakao.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey       = apiKey;
    }

    /**
     * 주소 검색 → {sido, sigungu} 반환. 인식 실패 시 빈 맵 반환.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> searchRegion(String query) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                ADDRESS_URL + query, HttpMethod.GET, entity, Map.class);

            if (resp.getBody() == null) return Map.of();

            List<Map<String, Object>> docs =
                (List<Map<String, Object>>) resp.getBody().get("documents");

            if (docs == null || docs.isEmpty()) {
                // 도로명주소 API fallback: keyword 검색
                return searchKeyword(query);
            }

            Map<String, Object> addr = (Map<String, Object>) docs.get(0).get("address");
            if (addr == null) addr = (Map<String, Object>) docs.get(0).get("road_address");
            if (addr == null) return Map.of();

            String sido    = str(addr, "region_1depth_name");
            String sigungu = str(addr, "region_2depth_name");
            log.info("카카오 주소 검색 결과: sido={}, sigungu={}", sido, sigungu);
            return Map.of("sido", sido, "sigungu", sigungu);

        } catch (Exception e) {
            log.error("카카오 주소 API 호출 실패: {}", e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> searchKeyword(String query) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> resp = restTemplate.exchange(
                "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + query,
                HttpMethod.GET, entity, Map.class);

            if (resp.getBody() == null) return Map.of();

            List<Map<String, Object>> docs =
                (List<Map<String, Object>>) resp.getBody().get("documents");
            if (docs == null || docs.isEmpty()) return Map.of();

            String sido    = str(docs.get(0), "address_name").split(" ")[0];
            String sigungu = str(docs.get(0), "address_name").split(" ").length > 1
                             ? str(docs.get(0), "address_name").split(" ")[1] : "";
            return Map.of("sido", sido, "sigungu", sigungu);

        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * 주소 문자열 → [lat, lng] 반환. 실패 시 빈 배열 반환.
     */
    @SuppressWarnings("unchecked")
    public double[] geocode(String address) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            org.springframework.web.util.UriComponents uri =
                org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                    .queryParam("query", address)
                    .build();
            ResponseEntity<Map> resp = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, entity, Map.class);

            if (resp.getBody() == null) return new double[0];

            List<Map<String, Object>> docs =
                (List<Map<String, Object>>) resp.getBody().get("documents");
            if (docs == null || docs.isEmpty()) return new double[0];

            double lat = Double.parseDouble(str(docs.get(0), "y"));
            double lng = Double.parseDouble(str(docs.get(0), "x"));
            log.info("주소 좌표 변환: {} → {},{}", address, lat, lng);
            return new double[]{ lat, lng };

        } catch (Exception e) {
            log.warn("주소 좌표 변환 실패: {} - {}", address, e.getMessage());
            return new double[0];
        }
    }

    /**
     * 키워드로 장소 검색 → [lat, lng] 반환. 주소 geocode 실패 시 fallback용.
     */
    @SuppressWarnings("unchecked")
    public double[] geocodeByKeyword(String query) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            org.springframework.web.util.UriComponents uri =
                org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .build();

            ResponseEntity<Map> resp = restTemplate.exchange(
                uri.toUriString(), HttpMethod.GET, entity, Map.class);

            if (resp.getBody() == null) return new double[0];
            List<Map<String, Object>> docs =
                (List<Map<String, Object>>) resp.getBody().get("documents");
            if (docs == null || docs.isEmpty()) return new double[0];

            double lat = Double.parseDouble(str(docs.get(0), "y"));
            double lng = Double.parseDouble(str(docs.get(0), "x"));
            log.info("키워드 좌표 변환: {} → {},{}", query, lat, lng);
            return new double[]{ lat, lng };

        } catch (Exception e) {
            log.warn("키워드 좌표 변환 실패: {} - {}", query, e.getMessage());
            return new double[0];
        }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString().trim();
    }
}
