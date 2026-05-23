package dev.hongwp.suxai.client;

import dev.hongwp.suxai.model.FacilityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class KwaterFacilityApiClient {

    private static final Logger log = LoggerFactory.getLogger(KwaterFacilityApiClient.class);
    private static final String BASE_URL =
        "https://apis.data.go.kr/B500001/rwis/waterQuality/fcltylist/codelist" +
        "?fcltyDivCode=2&numOfRows=100&pageNo=1&serviceKey=";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public KwaterFacilityApiClient(RestTemplate restTemplate,
                                   @Value("${kwater.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public List<FacilityInfo> fetchFacilities() {
        try {
            String raw = restTemplate.getForObject(BASE_URL + apiKey, String.class);
            log.info("정수장 목록 API 응답 (앞 200자): {}",
                raw == null ? "null" : raw.substring(0, Math.min(200, raw.length())));
            return parseXml(raw);
        } catch (Exception e) {
            log.error("정수장 목록 API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<FacilityInfo> parseXml(String xml) {
        List<FacilityInfo> result = new ArrayList<>();
        if (xml == null || xml.isBlank()) {
            log.error("정수장 목록 API 응답 없음");
            return result;
        }

        // BOM(U+FEFF) 및 앞쪽 공백 제거
        xml = xml.replace("﻿", "").stripLeading();

        if (!xml.startsWith("<")) {
            log.error("정수장 목록 API가 XML이 아닌 응답 반환: {}",
                xml.substring(0, Math.min(300, xml.length())));
            return result;
        }

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                NodeList children = items.item(i).getChildNodes();
                String name = "", sujCode = "";
                for (int j = 0; j < children.getLength(); j++) {
                    String tag = children.item(j).getNodeName();
                    String val = children.item(j).getTextContent().trim();
                    if ("fcltyMngNm".equals(tag))   name    = val;
                    else if ("sujCode".equals(tag))  sujCode = val;
                }
                if (!sujCode.isBlank() && !name.isBlank()) {
                    result.add(new FacilityInfo(sujCode, name, ""));
                }
            }
            log.info("정수장 목록 {}개 파싱 완료", result.size());
        } catch (Exception e) {
            log.error("정수장 목록 XML 파싱 실패: {}", e.getMessage());
        }
        return result;
    }
}
