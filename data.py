"""
수자이(水Xai) - 측정소 초기 데이터 및 이상징후 점수 계산
"""

import numpy as np

# ── 수질 측정소 ──────────────────────────────────────────
WQ_STATIONS = [
    {"id": "wq01", "name": "한강 광진 수질측정소",    "loc": "서울 광진구",   "ph": 7.1, "do": 8.2, "turb": 9,  "ec": 310},
    {"id": "wq02", "name": "낙동강 구미 수질측정소",  "loc": "경북 구미시",   "ph": 6.8, "do": 7.5, "turb": 18, "ec": 420},
    {"id": "wq03", "name": "금강 공주 수질측정소",    "loc": "충남 공주시",   "ph": 7.4, "do": 8.8, "turb": 7,  "ec": 280},
    {"id": "wq04", "name": "영산강 나주 수질측정소",  "loc": "전남 나주시",   "ph": 7.8, "do": 6.1, "turb": 34, "ec": 580},
    {"id": "wq05", "name": "섬진강 곡성 수질측정소",  "loc": "전남 곡성군",   "ph": 7.0, "do": 9.1, "turb": 5,  "ec": 190},
    {"id": "wq06", "name": "한강 팔당 수질측정소",    "loc": "경기 남양주시", "ph": 7.3, "do": 8.5, "turb": 11, "ec": 295},
    {"id": "wq07", "name": "낙동강 함안 수질측정소",  "loc": "경남 함안군",   "ph": 8.1, "do": 5.8, "turb": 48, "ec": 730},
    {"id": "wq08", "name": "금강 청주 수질측정소",    "loc": "충북 청주시",   "ph": 7.6, "do": 7.9, "turb": 14, "ec": 360},
    {"id": "wq09", "name": "북한강 춘천 수질측정소",  "loc": "강원 춘천시",   "ph": 7.2, "do": 9.4, "turb": 4,  "ec": 165},
    {"id": "wq10", "name": "임진강 연천 수질측정소",  "loc": "경기 연천군",   "ph": 6.6, "do": 7.0, "turb": 22, "ec": 440},
]

# ── 유량 측정소 ──────────────────────────────────────────
FL_STATIONS = [
    {"id": "fl01", "name": "한강 한강대교 유량측정소", "loc": "서울 용산구",  "flow": 142, "level": 5.8},
    {"id": "fl02", "name": "낙동강 왜관 유량측정소",   "loc": "경북 칠곡군", "flow": 89,  "level": 4.2},
    {"id": "fl03", "name": "금강 부여 유량측정소",     "loc": "충남 부여군", "flow": 63,  "level": 3.5},
    {"id": "fl04", "name": "섬진강 압록 유량측정소",   "loc": "전남 곡성군", "flow": 28,  "level": 2.1},
    {"id": "fl05", "name": "영산강 무안 유량측정소",   "loc": "전남 무안군", "flow": 41,  "level": 3.1},
    {"id": "fl06", "name": "한강 이포 유량측정소",     "loc": "경기 여주시", "flow": 318, "level": 9.2},
    {"id": "fl07", "name": "남한강 충주 유량측정소",   "loc": "충북 충주시", "flow": 210, "level": 7.8},
    {"id": "fl08", "name": "북한강 의암 유량측정소",   "loc": "강원 춘천시", "flow": 76,  "level": 4.6},
    {"id": "fl09", "name": "낙동강 안동 유량측정소",   "loc": "경북 안동시", "flow": 55,  "level": 3.8},
    {"id": "fl10", "name": "임진강 적성 유량측정소",   "loc": "경기 파주시", "flow": 33,  "level": 2.9},
]


def wq_score(row: dict) -> float:
    """수질 이상징후 점수 계산 (0~100)"""
    score = 0.0
    # pH: 정상 6.5~8.5, 중심 7.5
    ph_dist = max(0, abs(row["ph"] - 7.5) - 1.0)
    score += min(40, ph_dist * 80)
    # DO: 5mg/L 이하 위험
    score += min(30, max(0, (5 - row["do"]) * 8))
    # 탁도: 20 NTU 초과 시 점수 가산
    score += min(20, max(0, (row["turb"] - 20) * 0.8))
    # EC: 500 μS/cm 초과 시 점수 가산
    score += min(10, max(0, (row["ec"] - 500) * 0.02))
    return round(min(100, score), 1)


def fl_score(row: dict) -> float:
    """유량 이상징후 점수 계산 (0~100)"""
    score = 0.0
    if row["flow"] > 300:
        score += min(50, (row["flow"] - 300) * 0.5)
    elif row["flow"] < 10:
        score += 40
    if row["level"] > 15:
        score += min(50, (row["level"] - 15) * 8)
    elif row["level"] < 2:
        score += 40
    return round(min(100, score), 1)


def get_status(score: float) -> str:
    """점수 → 상태 레이블"""
    if score >= 40:
        return "위험"
    if score >= 15:
        return "주의"
    return "정상"


def jitter(value: float, amp: float, lo: float, hi: float) -> float:
    """실시간 노이즈 시뮬레이션"""
    return round(float(np.clip(value + np.random.uniform(-amp, amp), lo, hi)), 2)


def update_wq(stations: list[dict]) -> list[dict]:
    """수질 데이터 실시간 변동 적용"""
    updated = []
    for s in stations:
        updated.append({
            **s,
            "ph":   jitter(s["ph"],   0.06, 4,    12),
            "do":   jitter(s["do"],   0.18, 0,    15),
            "turb": jitter(s["turb"], 1.8,  0,    200),
            "ec":   jitter(s["ec"],   9,    0,    3000),
        })
    return updated


def update_fl(stations: list[dict]) -> list[dict]:
    """유량 데이터 실시간 변동 적용"""
    updated = []
    for s in stations:
        updated.append({
            **s,
            "flow":  jitter(s["flow"],  3.5,  0, 600),
            "level": jitter(s["level"], 0.12, 0, 25),
        })
    return updated
