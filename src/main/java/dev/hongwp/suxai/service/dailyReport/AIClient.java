package dev.hongwp.suxai.service.dailyReport;

import dev.hongwp.suxai.model.AnalysisResult;
import dev.hongwp.suxai.model.dailyReport.AIRequest;
import dev.hongwp.suxai.model.dailyReport.AIResponse;
import dev.hongwp.suxai.model.dailyReport.DailyReport;
import dev.hongwp.suxai.service.AnalysisService;
import org.springframework.stereotype.Service;

@Service
public class AIClient {

    private final AnalysisService analysisService;

    public AIClient(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    public AIResponse analyze(AIRequest request) {
        AnalysisResult result = analysisService.analyze(
            request.getSujCode(),
            request.getStartDate(),
            request.getEndDate()
        );

        return new AIResponse(
            result.getAnalysis(),
            result.getAnalyzedAt(),
            result.getWqCount(),
            result.getFlCount()
        );
    }
}
