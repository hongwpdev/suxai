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
public class GroqApiClient {

    private static final Logger log = LoggerFactory.getLogger(GroqApiClient.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public GroqApiClient(RestTemplate restTemplate,
                           @Value("${groq.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return !apiKey.isBlank() && !apiKey.equals("YOUR_GROQ_API_KEY_HERE");
    }

    @SuppressWarnings("unchecked")
    public String analyze(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "model", MODEL,
            "max_tokens", 2048,
            "messages", List.of(
                Map.of("role", "system", "content",
                    "당신은 수처리 전문가입니다. " +
                    "반드시 한국어(한글)로만 답변하세요. " +
                    "한자(殺菌, 濃度 등 한문)를 절대 사용하지 마세요. " +
                    "한자 대신 반드시 한글을 사용하세요: 살균, 농도, 능력, 부족 등. " +
                    "영어 단어도 가능하면 한국어로 바꾸세요."),
                Map.of("role", "user", "content", prompt)
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate
                .exchange(GROQ_URL, HttpMethod.POST, request, Map.class)
                .getBody();

            if (response == null) return "분석 결과를 가져오지 못했습니다.";

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) return "분석 결과가 비어 있습니다.";

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Groq API 호출 실패: {}", e.getMessage());
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
