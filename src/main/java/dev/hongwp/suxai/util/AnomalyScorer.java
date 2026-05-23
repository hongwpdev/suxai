package dev.hongwp.suxai.util;

import dev.hongwp.suxai.model.FlowStation;
import dev.hongwp.suxai.model.WaterQualityStation;

public class AnomalyScorer {

    // data.py의 wq_score() 이식
    public static double wqScore(WaterQualityStation s) {
        double score = 0.0;

        // pH: 정상 6.5~8.5, 중심 7.5
        double phDist = Math.max(0, Math.abs(s.getPh() - 7.5) - 1.0);
        score += Math.min(40, phDist * 80);

        // DO: 5mg/L 이하 위험
        score += Math.min(30, Math.max(0, (5 - s.getDissolvedOxygen()) * 8));

        // 탁도: 20 NTU 초과 시 점수 가산
        score += Math.min(20, Math.max(0, (s.getTurbidity() - 20) * 0.8));

        // 전기전도도: 500 μS/cm 초과 시 점수 가산
        score += Math.min(10, Math.max(0, (s.getConductivity() - 500) * 0.02));

        return Math.round(Math.min(100, score) * 10.0) / 10.0;
    }

    // data.py의 fl_score() 이식
    public static double flScore(FlowStation s) {
        double score = 0.0;

        if (s.getFlow() > 300) {
            score += Math.min(50, (s.getFlow() - 300) * 0.5);
        } else if (s.getFlow() < 10) {
            score += 40;
        }

        if (s.getLevel() > 15) {
            score += Math.min(50, (s.getLevel() - 15) * 8);
        } else if (s.getLevel() < 2) {
            score += 40;
        }

        return Math.round(Math.min(100, score) * 10.0) / 10.0;
    }

    public static String getStatus(double score) {
        if (score >= 40) return "위험";
        if (score >= 15) return "주의";
        return "정상";
    }
}
