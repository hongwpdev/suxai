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
    private static final String URL =
        "http://apis.data.go.kr/B500001/rwis/waterFlux/fcltylist/codelist?fcltyDivCode=1&serviceKey=";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public KwaterFacilityApiClient(RestTemplate restTemplate,
                                   @Value("${kwater.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public List<FacilityInfo> fetchFacilities() {
        try {
            String xml = restTemplate.getForObject(URL + apiKey, String.class);
            return parseXml(xml);
        } catch (Exception e) {
            log.error("정수장 목록 API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<FacilityInfo> parseXml(String xml) {
        List<FacilityInfo> result = new ArrayList<>();
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
                    if ("fcltyMngNm".equals(tag))  name    = val;
                    else if ("sujCode".equals(tag)) sujCode = val;
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
