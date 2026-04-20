"""
수자이(水Xai) — 실시간 전국 수질·유량 이상징후 감지 대시보드
실행: streamlit run app.py
"""

import time
import copy
import streamlit as st

from data import (
    WQ_STATIONS, FL_STATIONS,
    wq_score, fl_score, get_status,
    update_wq, update_fl,
    fetch_wq_from_api, fetch_fl_from_api,
)

# ── 페이지 설정 ──────────────────────────────────────────
st.set_page_config(
    page_title="수자이(水Xai)",
    page_icon="💧",
    layout="wide",
)

# ── 스타일 ───────────────────────────────────────────────
st.markdown("""
<style>
/* 전체 배경 */
[data-testid="stAppViewContainer"] { background-color: #111318; }
[data-testid="stHeader"]           { background-color: #111318; }
[data-testid="stSidebar"]          { background-color: #0e1117; }
.block-container { padding-top: 2rem; max-width: 900px; }

/* 카드 */
.s-card {
    background: #181b22;
    border: 1px solid #23272f;
    border-radius: 8px;
    padding: 14px 18px;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 16px;
}
.s-card.crit { border-left: 3px solid #e8453c; }
.s-card.warn { border-left: 3px solid #f5a623; }
.s-card.ok   { border-left: 3px solid transparent; }

.rank { font-family: monospace; font-size: 11px; color: #5a6070; min-width: 24px; }
.st-name { font-size: 14px; font-weight: 600; color: #e8eaf0; margin-bottom: 2px; }
.st-loc  { font-size: 11px; color: #5a6070; }

.metrics { display: flex; gap: 20px; margin-left: auto; }
.metric-lbl { font-size: 10px; color: #5a6070; margin-bottom: 2px; text-align: right; }
.metric-val { font-family: monospace; font-size: 13px; font-weight: 500; text-align: right; }
.c-ok   { color: #e8eaf0; }
.c-warn { color: #f5a623; }
.c-crit { color: #e8453c; }

.score-wrap { width: 80px; }
.score-bg   { height: 3px; background: #333844; border-radius: 2px; margin-bottom: 4px; overflow: hidden; }
.score-fill { height: 100%; border-radius: 2px; }
.score-num  { font-family: monospace; font-size: 10px; text-align: right; }

.bdg { padding: 2px 8px; border-radius: 3px; font-size: 10px; font-weight: 600; font-family: monospace; }
.bdg-ok   { background: rgba(62,207,112,.10); color: #3ecf70; }
.bdg-warn { background: rgba(245,166,35,.12); color: #f5a623; }
.bdg-crit { background: rgba(232,69,60,.12);  color: #e8453c; }

/* 탭 스타일 */
.stTabs [data-baseweb="tab-list"] { background-color: #111318; border-bottom: 1px solid #23272f; gap: 0; }
.stTabs [data-baseweb="tab"] { background-color: transparent; color: #5a6070; font-size: 13px; padding: 8px 20px; }
.stTabs [aria-selected="true"] { color: #e8eaf0; border-bottom: 2px solid #e8453c; }
.stTabs [data-baseweb="tab-highlight"] { background-color: transparent !important; }
</style>
""", unsafe_allow_html=True)

# ── 세션 상태 초기화 ──────────────────────────────────────
if "wq_data" not in st.session_state:
    st.session_state.wq_data = copy.deepcopy(WQ_STATIONS)
if "fl_data" not in st.session_state:
    st.session_state.fl_data = copy.deepcopy(FL_STATIONS)

# 실시간 갱신
st.session_state.wq_data = update_wq(st.session_state.wq_data)
st.session_state.fl_data = update_fl(st.session_state.fl_data)

# 테스트용 — app.py 세션 초기화 부분에 임시 추가
#st.session_state.wq_data = []
#st.session_state.fl_data = []

# ── 카드 HTML 생성 ───────────────────────────────────────
def score_color(status):
    return {"위험": "#e8453c", "주의": "#f5a623", "정상": "#3ecf70"}[status]

def metric_html(label, value, cls="c-ok"):
    return (f'<div class="metric">'
            f'<div class="metric-lbl">{label}</div>'
            f'<div class="metric-val {cls}">{value}</div>'
            f'</div>')

def wq_card_html(rank, s):
    sc   = wq_score(s)
    st_  = get_status(sc)
    cls  = {"위험":"crit","주의":"warn","정상":"ok"}[st_]
    sc_c = score_color(st_)
    ph_c = "c-warn" if abs(s["ph"] - 7.5) > 1 else "c-ok"
    do_c = "c-crit" if s["do"] < 3 else "c-warn" if s["do"] < 5 else "c-ok"
    tb_c = "c-crit" if s["turb"] > 50 else "c-warn" if s["turb"] > 30 else "c-ok"
    ph_v   = f'{s["ph"]:.1f}'
    do_v   = f'{s["do"]:.1f} mg/L'
    turb_v = f'{s["turb"]:.0f} NTU'
    sc_v   = f'{sc:.0f}점'
    sc_nc  = sc_c if sc > 10 else "#5a6070"
    name   = s["name"]
    loc    = s["loc"]
    m_ph   = metric_html("pH",   ph_v,   ph_c)
    m_do   = metric_html("DO",   do_v,   do_c)
    m_turb = metric_html("탁도", turb_v, tb_c)
    return (f'<div class="s-card {cls}">'
            f'<span class="rank">#{rank}</span>'
            f'<div style="flex:1;min-width:0">'
            f'<div class="st-name">{name}</div>'
            f'<div class="st-loc">{loc}</div>'
            f'</div>'
            f'<div class="metrics">{m_ph}{m_do}{m_turb}</div>'
            f'<div class="score-wrap">'
            f'<div class="score-bg"><div class="score-fill" style="width:{sc}%;background:{sc_c}"></div></div>'
            f'<div class="score-num" style="color:{sc_nc}">{sc_v}</div>'
            f'</div>'
            f'<span class="bdg bdg-{cls}">{st_}</span>'
            f'</div>')

def fl_card_html(rank, s):
    sc   = fl_score(s)
    st_  = get_status(sc)
    cls  = {"위험":"crit","주의":"warn","정상":"ok"}[st_]
    sc_c = score_color(st_)
    fl_c = "c-warn" if s["flow"] > 300 or s["flow"] < 10 else "c-ok"
    lv_c = "c-crit" if s["level"] > 15 or s["level"] < 2 else "c-ok"
    flow_v  = f'{s["flow"]:.1f} m\u00b3/s'
    level_v = f'{s["level"]:.1f} m'
    sc_v    = f'{sc:.0f}점'
    sc_nc   = sc_c if sc > 10 else "#5a6070"
    name    = s["name"]
    loc     = s["loc"]
    m_flow  = metric_html("유량", flow_v,  fl_c)
    m_level = metric_html("수위", level_v, lv_c)
    return (f'<div class="s-card {cls}">'
            f'<span class="rank">#{rank}</span>'
            f'<div style="flex:1;min-width:0">'
            f'<div class="st-name">{name}</div>'
            f'<div class="st-loc">{loc}</div>'
            f'</div>'
            f'<div class="metrics">{m_flow}{m_level}</div>'
            f'<div class="score-wrap">'
            f'<div class="score-bg"><div class="score-fill" style="width:{sc}%;background:{sc_c}"></div></div>'
            f'<div class="score-num" style="color:{sc_nc}">{sc_v}</div>'
            f'</div>'
            f'<span class="bdg bdg-{cls}">{st_}</span>'
            f'</div>')


# ── 뉴스 더미 데이터 ─────────────────────────────────────
NEWS = [
    {"level": "crit",  "text": "충북 화학공장 폐수 무단방류 의혹 — 인근 하천 전기전도도 정상치 3배 초과"},
    {"level": "warn",  "text": "낙동강 중류 집중호우로 탁도 기준치(50 NTU) 초과 관측"},
    {"level": "warn",  "text": "한강 수계 봄철 녹조 발생 징후 — DO 수치 감소 동반"},
    {"level": "ok",    "text": "충주댐 방류량 정상화 — 한강 유량 안정 단계 진입"},
    {"level": "ok",    "text": "다음 주 전국 강수량 평년 수준 예보 — 하천 수위 안정 전망"},
    {"level": "warn",  "text": "임진강 연천 구간 수위 소폭 상승 — 기상청 강수 영향 분석 중"},
    {"level": "crit",  "text": "영산강 나주 구간 DO 4.8mg/L 기록 — 어류 폐사 우려"},
]

def news_ticker_html(news):
    color_map = {"crit": "#e8453c", "warn": "#f5a623", "ok": "#5a6070"}
    icon_map   = {"crit": "🔴", "warn": "🟡", "ok": "⚪"}
    items_html = "".join(
        f'<span style="margin-right:56px;color:{color_map[n["level"]]};white-space:nowrap;">'
        f'{icon_map[n["level"]]}&nbsp;&nbsp;{n["text"]}'
        f'</span>'
        for n in news
    )
    # 무한 반복을 위해 두 번 출력
    return f"""
<style>
@keyframes ticker {{
    from {{ transform: translateX(0); }}
    to   {{ transform: translateX(-50%); }}
}}
.ticker-wrap {{
    overflow: hidden;
    background: #0e1117;
    border-top: 1px solid #1e2230;
    border-bottom: 1px solid #1e2230;
    padding: 8px 0;
    margin-bottom: 4px;
}}
.ticker-inner {{
    display: inline-flex;
    animation: ticker 40s linear infinite;
    white-space: nowrap;
}}
.ticker-inner:hover {{ animation-play-state: paused; }}
.ticker-label {{
    background: #e8453c;
    color: #fff;
    font-size: 10px;
    font-weight: 700;
    font-family: monospace;
    letter-spacing: .08em;
    padding: 0 12px;
    margin-right: 20px;
    display: inline-flex;
    align-items: center;
    flex-shrink: 0;
    border-radius: 2px;
}}
</style>
<div class="ticker-wrap">
    <div class="ticker-inner">
        <span class="ticker-label">뉴스</span>
        {items_html}
        <span class="ticker-label">뉴스</span>
        {items_html}
    </div>
</div>
"""

# ── 헤더 ─────────────────────────────────────────────────
st.markdown("""
<h1 style='color:#e8eaf0;font-size:22px;font-weight:700;margin-bottom:4px'>
    💧 수자이(水Xai)
</h1>
<p style='color:#5a6070;font-size:12px;margin-bottom:0'>
    실시간 전국 수질·유량 이상징후 감지 대시보드
</p>
""", unsafe_allow_html=True)

st.markdown("<div style='height:10px'></div>", unsafe_allow_html=True)
st.markdown(news_ticker_html(NEWS), unsafe_allow_html=True)

st.markdown("<div style='height:12px'></div>", unsafe_allow_html=True)

# ── 탭 ───────────────────────────────────────────────────
tab_wq, tab_fl = st.tabs(["수질", "유량"])

with tab_wq:
    sorted_wq = sorted(st.session_state.wq_data, key=wq_score, reverse=True)
    warn_n = sum(1 for s in sorted_wq if get_status(wq_score(s)) != "정상")

    # 타이틀 + 조회 버튼
    col_title, col_btn = st.columns([8, 1])
    with col_title:
        st.markdown("""
        <h2 style='color:#e8eaf0;font-size:18px;font-weight:700;margin:16px 0 8px'>
            실시간 수질 현황 (전국)
        </h2>""", unsafe_allow_html=True)
    with col_btn:
        st.markdown("<div style='margin-top:16px'></div>", unsafe_allow_html=True)
        if st.button("조회", key="fetch_wq", type="secondary", use_container_width=True):
            with st.spinner("API 조회 중..."):
                result = fetch_wq_from_api()
                if result:
                    st.session_state.wq_data = result
                    st.success("수질 데이터 갱신 완료")
                else:
                    st.warning("API 미연동 — data.py의 fetch_wq_from_api()를 구현해주세요")

    wq_warn = f"⚠️ {warn_n}개 측정소 주의·위험 &nbsp;|&nbsp; " if warn_n else ""
    wq_cards = "".join(wq_card_html(i+1, s) for i, s in enumerate(sorted_wq))
    if wq_cards:
        st.markdown(
            "<p style='color:#5a6070;font-size:12px;margin-bottom:20px'>"
            + wq_warn + "이상징후 높은 순</p>" + wq_cards,
            unsafe_allow_html=True
        )
    else:
        st.markdown(
            "<p style='color:#5a6070;font-size:13px;padding:40px 0;text-align:center'>"
            "데이터가 존재하지 않습니다.</p>",
            unsafe_allow_html=True
        )

with tab_fl:
    sorted_fl = sorted(st.session_state.fl_data, key=fl_score, reverse=True)
    warn_n = sum(1 for s in sorted_fl if get_status(fl_score(s)) != "정상")

    # 타이틀 + 조회 버튼
    col_title, col_btn = st.columns([8, 1])
    with col_title:
        st.markdown("""
        <h2 style='color:#e8eaf0;font-size:18px;font-weight:700;margin:16px 0 8px'>
            실시간 유량 현황 (전국)
        </h2>""", unsafe_allow_html=True)
    with col_btn:
        st.markdown("<div style='margin-top:16px'></div>", unsafe_allow_html=True)
        if st.button("조회", key="fetch_fl", type="secondary", use_container_width=True):
            with st.spinner("API 조회 중..."):
                result = fetch_fl_from_api()
                if result:
                    st.session_state.fl_data = result
                    st.success("유량 데이터 갱신 완료")
                else:
                    st.warning("API 미연동 — data.py의 fetch_fl_from_api()를 구현해주세요")

    fl_warn = f"⚠️ {warn_n}개 측정소 주의·위험 &nbsp;|&nbsp; " if warn_n else ""
    fl_cards = "".join(fl_card_html(i+1, s) for i, s in enumerate(sorted_fl))
    if fl_cards:
        st.markdown(
            "<p style='color:#5a6070;font-size:12px;margin-bottom:20px'>"
            + fl_warn + "이상징후 높은 순</p>" + fl_cards,
            unsafe_allow_html=True
        )
    else:
        st.markdown(
            "<p style='color:#5a6070;font-size:13px;padding:40px 0;text-align:center'>"
            "데이터가 존재하지 않습니다.</p>",
            unsafe_allow_html=True
        )

# ── 자동 갱신 (2.5초) ────────────────────────────────────
time.sleep(2.5)
st.rerun()
