import pandas as pd

# 수질 이상징후 기준값 (WHO 및 K-water 기준)
THRESHOLDS = {
    "ph": (6.5, 8.5),         # 정상 범위
    "turbidity": (0, 0.5),    # 탁도 NTU
    "residual_chlorine": (0.1, 4.0),  # 잔류염소 mg/L
}


def detect_anomaly(df: pd.DataFrame) -> pd.DataFrame:
    """데이터프레임에서 이상징후 컬럼을 추가해 반환"""
    df = df.copy()
    df["anomaly"] = False

    if "ph" in df.columns:
        df["anomaly"] |= (df["ph"] < THRESHOLDS["ph"][0]) | (df["ph"] > THRESHOLDS["ph"][1])

    if "turbidity" in df.columns:
        df["anomaly"] |= df["turbidity"] > THRESHOLDS["turbidity"][1]

    if "residual_chlorine" in df.columns:
        df["anomaly"] |= (
            (df["residual_chlorine"] < THRESHOLDS["residual_chlorine"][0]) |
            (df["residual_chlorine"] > THRESHOLDS["residual_chlorine"][1])
        )

    return df
