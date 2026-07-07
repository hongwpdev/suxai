package dev.hongwp.suxai.ui;

import dev.hongwp.suxai.SuxaiApplication;
import dev.hongwp.suxai.client.KakaoApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@SpringBootTest(
    classes = SuxaiApplication.class,
    properties = {
        "kakao.api.key=test-kakao-key",
        "groq.api.key=test-groq-key",
        "kwater.api.key=test-kwater-key",
        "kwater.suj.code=318"
    }
)
@AutoConfigureMockMvc
class IndexPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void forwardsRootToIndexPage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void servesIndexHtmlWithDailyReportMenuAndHooks() throws Exception {
        String body = new String(
            mockMvc.perform(get("/index.html"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray(),
            StandardCharsets.UTF_8
        );

        assertTrue(body.contains("AI 분석"));
        assertTrue(body.contains("일일 보고(AI)"));
        assertTrue(body.contains("tab-report"));
        assertTrue(body.contains("runDailyReport()"));
        assertTrue(body.contains("fetchDailyReport()"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KakaoApiClient kakaoApiClient() {
            KakaoApiClient client = mock(KakaoApiClient.class);
            when(client.geocode(org.mockito.ArgumentMatchers.anyString())).thenReturn(new double[0]);
            when(client.geocodeByKeyword(org.mockito.ArgumentMatchers.anyString())).thenReturn(new double[0]);
            when(client.searchRegion(org.mockito.ArgumentMatchers.anyString())).thenReturn(java.util.Map.of());
            return client;
        }
    }
}
