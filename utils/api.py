import requests
import pandas as pd
import os
import streamlit as st
from dotenv import load_dotenv
from datetime import datetime, timedelta

load_dotenv()

# 로컬: .env 파일 / 클라우드: Streamlit Secrets
API_KEY = st.secrets.get("PUBLIC_DATA_API_KEY", os.getenv("PUBLIC_DATA_API_KEY"))
QUALITY_URL = "http://apis.data.go.kr/B500001/rwis/waterQuality/list"
FLOW_URL = "http://apis.data.go.kr/B500001/rwis/waterFlux/waterFlux"


def _date_range(hours=1):
    """현재 시각 기준으로 조회 시작/종료 날짜·시간 반환"""
    now = datetime.now()
    start = now - timedelta(hours=hours)
    return {
        "stDt": start.strftime("%Y-%m-%d"),
        "stTm": start.strftime("%H"),
        "edDt": now.strftime("%Y-%m-%d"),
        "edTm": now.strftime("%H"),
    }


def get_water_quality(num_of_rows=100):
    """실시간 전국 수질 정보 조회 (잔류염소, pH, 탁도)"""
    params = {
        "serviceKey": API_KEY,
        "numOfRows": num_of_rows,
        "pageNo": 1,
        "resultType": "json",
        **_date_range(hours=1),
    }
    response = requests.get(QUALITY_URL, params=params)
    response.raise_for_status()
    items = response.json().get("response", {}).get("body", {}).get("items", [])
    return pd.DataFrame(items)


def get_water_flow(num_of_rows=100):
    """실시간 전국 유량 정보 조회"""
    params = {
        "serviceKey": API_KEY,
        "numOfRows": num_of_rows,
        "pageNo": 1,
        "resultType": "json",
        **_date_range(hours=1),
    }
    response = requests.get(FLOW_URL, params=params)
    response.raise_for_status()
    items = response.json().get("response", {}).get("body", {}).get("items", [])
    return pd.DataFrame(items)
