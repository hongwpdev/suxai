import requests
import pandas as pd
import os
import streamlit as st
from dotenv import load_dotenv

load_dotenv()

# 로컬: .env 파일 / 클라우드: Streamlit Secrets
API_KEY = st.secrets.get("PUBLIC_DATA_API_KEY", os.getenv("PUBLIC_DATA_API_KEY"))
BASE_URL = "http://apis.data.go.kr/B500001/rwis"


def get_water_quality(page_no=1, num_of_rows=10):
    """실시간 수질 정보 조회 (잔류염소, pH, 탁도)"""
    url = f"{BASE_URL}/waterQuality/list"
    params = {
        "serviceKey": API_KEY,
        "pageNo": page_no,
        "numOfRows": num_of_rows,
        "resultType": "json",
    }
    response = requests.get(url, params=params)
    response.raise_for_status()
    items = response.json().get("response", {}).get("body", {}).get("items", [])
    return pd.DataFrame(items)


def get_water_flow(page_no=1, num_of_rows=10):
    """실시간 유량 정보 조회"""
    url = f"{BASE_URL}/waterFlow/list"
    params = {
        "serviceKey": API_KEY,
        "pageNo": page_no,
        "numOfRows": num_of_rows,
        "resultType": "json",
    }
    response = requests.get(url, params=params)
    response.raise_for_status()
    items = response.json().get("response", {}).get("body", {}).get("items", [])
    return pd.DataFrame(items)


def get_dam_info(page_no=1, num_of_rows=10):
    """수문 운영 정보 조회 (댐수위, 강우량, 유입량 등)"""
    url = f"{BASE_URL}/dam/list"
    params = {
        "serviceKey": API_KEY,
        "pageNo": page_no,
        "numOfRows": num_of_rows,
        "resultType": "json",
    }
    response = requests.get(url, params=params)
    response.raise_for_status()
    items = response.json().get("response", {}).get("body", {}).get("items", [])
    return pd.DataFrame(items)
