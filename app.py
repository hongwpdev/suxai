import streamlit as st
import plotly.express as px
from utils.api import get_water_quality, get_water_flow, get_dam_info
from utils.anomaly import detect_anomaly

st.set_page_config(page_title="수자이(水Xai)", page_icon="💧", layout="wide")

st.title("💧 수자이(水Xai)")
st.caption("실시간 수질·유량 이상징후 감지 대시보드")

st.sidebar.header("설정")
num_of_rows = st.sidebar.slider("조회 데이터 수", 5, 50, 10)

tab1, tab2, tab3 = st.tabs(["수질", "유량", "수문(댐)"])

with tab1:
    st.subheader("실시간 수질 현황")
    if st.button("수질 데이터 불러오기"):
        with st.spinner("데이터 조회 중..."):
            try:
                df = get_water_quality(num_of_rows=num_of_rows)
                df = detect_anomaly(df)
                anomaly_count = df["anomaly"].sum()
                if anomaly_count > 0:
                    st.error(f"⚠️ 이상징후 {anomaly_count}건 감지됨!")
                else:
                    st.success("정상 범위입니다.")
                st.dataframe(df, use_container_width=True)
            except Exception as e:
                st.error(f"API 오류: {e}")

with tab2:
    st.subheader("실시간 유량 현황")
    if st.button("유량 데이터 불러오기"):
        with st.spinner("데이터 조회 중..."):
            try:
                df = get_water_flow(num_of_rows=num_of_rows)
                st.dataframe(df, use_container_width=True)
            except Exception as e:
                st.error(f"API 오류: {e}")

with tab3:
    st.subheader("수문 운영 정보 (댐)")
    if st.button("댐 데이터 불러오기"):
        with st.spinner("데이터 조회 중..."):
            try:
                df = get_dam_info(num_of_rows=num_of_rows)
                st.dataframe(df, use_container_width=True)
            except Exception as e:
                st.error(f"API 오류: {e}")
