import streamlit as st
from utils.api import get_water_quality, get_water_flow
from utils.anomaly import detect_anomaly

st.set_page_config(page_title="수자이(水Xai)", page_icon="💧", layout="wide")

st.title("💧 수자이(水Xai)")
st.caption("실시간 전국 수질·유량 이상징후 감지 대시보드")

tab1, tab2 = st.tabs(["수질", "유량"])

with tab1:
    st.subheader("실시간 수질 현황 (전국)")
    if st.button("수질 데이터 불러오기"):
        with st.spinner("데이터 조회 중..."):
            try:
                df = get_water_quality()
                df = detect_anomaly(df)
                anomaly_count = df["anomaly"].sum()
                if anomaly_count > 0:
                    st.error(f"⚠️ 이상징후 {anomaly_count}건 감지됨!")
                else:
                    st.success("✅ 전국 수질 정상 범위입니다.")
                st.dataframe(df, use_container_width=True)
            except Exception as e:
                st.error(f"API 오류: {e}")

with tab2:
    st.subheader("실시간 유량 현황 (전국)")
    if st.button("유량 데이터 불러오기"):
        with st.spinner("데이터 조회 중..."):
            try:
                df = get_water_flow()
                st.success(f"✅ {len(df)}개 지점 유량 데이터 조회 완료")
                st.dataframe(df, use_container_width=True)
            except Exception as e:
                st.error(f"API 오류: {e}")
