# API 명세서

| 항목 | 내용 |
|------|------|
| 프로젝트명 | 수자이(水Xai) |
| 버전 | v1.2 |
| 작성일 | 2026-05-25 |
| Base URL | `http://localhost:8080` |

---

## 1. 내부 REST API

### 1.1 정수장 목록 조회

| 항목 | 내용 |
|------|------|
| 메서드 | `GET` |
| URL | `/api/facilities` |
| 설명 | 전국 정수장 목록을 반환한다 (24시간 캐시) |

**요청 파라미터**: 없음

**응답 예시**
```json
[
  {
    "sujCode": "311",
    "facilityName": "반월정수장",
    "address": ""
  },
  {
    "sujCode": "312",
    "facilityName": "와부정수장",
    "address": ""
  }
]
```

**응답 필드**

| 필드 | 타입 | 설명 |
|------|------|------|
| sujCode | String | 수계 코드 (정수장 식별자) |
| facilityName | String | 정수장 명칭 |
| address | String | 주소 (현재 미제공, 빈 문자열) |

**오류 응답**: 빈 배열 `[]` (API 호출 실패 시)

---

### 1.2 수질 현황 조회

| 항목 | 내용 |
|------|------|
| 메서드 | `GET` |
| URL | `/api/waterQuality` |
| 설명 | 선택된 정수장의 지정 기간 수질 데이터를 반환한다 (기간별 5분 캐시) |

**요청 파라미터**

| 파라미터 | 필수 | 타입 | 설명 | 예시 |
|---------|------|------|------|------|
| sujCode | 선택 | String | 수계 코드. 미입력 시 기본값 사용 | `318` |
| startDate | 선택 | String | 조회 시작일 (yyyy-MM-dd). 미입력 시 현재 시각 기준 1시간 전 자동 적용 | `2026-05-18` |
| endDate | 선택 | String | 조회 종료일 (yyyy-MM-dd). 미입력 시 현재 날짜 자동 적용 | `2026-05-25` |

**캐시 키**: `sujCode|startDate|endDate` (TTL: 5분)

**응답 예시**
```json
[
  {
    "id": "4148012318",
    "facilityName": "파주정수장",
    "address": "경기도 파주시",
    "divName": "정수",
    "measuredAt": "2026-05-23 22시",
    "phVal": "7.20",
    "phUnit": "",
    "tbVal": "0.0023",
    "tbUnit": "NTU",
    "clVal": "0.1500",
    "clUnit": "mg/L"
  }
]
```

**응답 필드** (모델: `QualityRecord`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | String | 시설관리번호 (fcltyMngNo) |
| facilityName | String | 시설명 |
| address | String | 시설 주소 |
| divName | String | 구분명 (정수/원수 등) |
| measuredAt | String | 측정시간 (YYYY-MM-DD HH시 형식) |
| phVal | String | pH 측정값 |
| phUnit | String | pH 단위 |
| tbVal | String | 탁도 측정값 |
| tbUnit | String | 탁도 단위 (NTU) |
| clVal | String | 잔류염소 측정값 |
| clUnit | String | 잔류염소 단위 (mg/L) |

---

### 1.3 유량 현황 조회

| 항목 | 내용 |
|------|------|
| 메서드 | `GET` |
| URL | `/api/waterFlow` |
| 설명 | 선택된 정수장의 지정 기간 유량 데이터를 반환한다 (기간별 5분 캐시) |

**요청 파라미터**

| 파라미터 | 필수 | 타입 | 설명 | 예시 |
|---------|------|------|------|------|
| sujCode | 선택 | String | 수계 코드. 미입력 시 기본값 사용 | `318` |
| startDate | 선택 | String | 조회 시작일 (yyyy-MM-dd). 미입력 시 현재 시각 기준 1시간 전 자동 적용 | `2026-05-18` |
| endDate | 선택 | String | 조회 종료일 (yyyy-MM-dd). 미입력 시 현재 날짜 자동 적용 | `2026-05-25` |

**캐시 키**: `sujCode|startDate|endDate` (TTL: 5분)

**응답 예시**
```json
[
  {
    "id": "4148012318",
    "facilityName": "파주정수장",
    "description": "한강수계 파주 유입유량",
    "value": "1250.50",
    "unit": "㎥/분",
    "measuredAt": "2026-05-23 22시",
    "divType": "순간"
  }
]
```

**응답 필드** (모델: `FlowRecord`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | String | 시설관리번호 (fcltyMngNo) |
| facilityName | String | 시설명 |
| description | String | 측정 항목 설명 |
| value | String | 측정값 |
| unit | String | 단위 |
| measuredAt | String | 측정시간 |
| divType | String | 유형 (순간 / 적산) |

---

### 1.4 AI 이상징후 분석

| 항목 | 내용 |
|------|------|
| 메서드 | `GET` |
| URL | `/api/analysis` |
| 설명 | 지정 기간의 수질·유량 데이터를 AI가 분석한 리포트를 반환한다 (기간별 5분 캐시) |

**요청 파라미터**

| 파라미터 | 필수 | 타입 | 설명 | 예시 |
|---------|------|------|------|------|
| sujCode | 선택 | String | 수계 코드. 미입력 시 기본값 사용 | `318` |
| startDate | 선택 | String | 조회 시작일 (yyyy-MM-dd) | `2026-05-18` |
| endDate | 선택 | String | 조회 종료일 (yyyy-MM-dd) | `2026-05-25` |

**캐시 키**: `sujCode|startDate|endDate` (TTL: 5분)

**응답 예시**
```json
{
  "analysis": "**1. 전체 현황**\n현재 파주정수장의 수질 상태는...\n\n**2. 이상징후**\n이상징후 없음\n\n**3. 권고사항**\n특이사항 없음",
  "analyzedAt": "2026-05-23 22:30",
  "wqCount": 8,
  "flCount": 12
}
```

**응답 필드** (모델: `AnalysisResult`)

| 필드 | 타입 | 설명 |
|------|------|------|
| analysis | String | AI 분석 리포트 본문 (`**볼드**` 마크다운 포함) |
| analyzedAt | String | 분석 기준 시각 (YYYY-MM-DD HH:mm) |
| wqCount | int | 분석에 사용된 수질 데이터 건수 |
| flCount | int | 분석에 사용된 유량 데이터 건수 |

---

### 1.5 뉴스 목록 조회

| 항목 | 내용 |
|------|------|
| 메서드 | `GET` |
| URL | `/api/news` |
| 설명 | 수계 관련 뉴스 항목 목록을 반환한다 |

**요청 파라미터**: 없음

**응답 예시**
```json
[
  {
    "level": "crit",
    "content": "충북 화학공장 폐수 무단방류 의혹 — 인근 하천 전기전도도 정상치 3배 초과"
  },
  {
    "level": "warn",
    "content": "낙동강 중류 집중호우로 탁도 기준치(50 NTU) 초과 관측"
  }
]
```

**응답 필드** (모델: `NewsItem`)

| 필드 | 타입 | 값 | 설명 |
|------|------|-----|------|
| level | String | `crit` / `warn` / `ok` | 심각도 |
| content | String | — | 뉴스 내용 |

---

## 2. 외부 API 연동

### 2.1 K-water 수질 API

| 항목 | 내용 |
|------|------|
| URL | `https://apis.data.go.kr/B500001/rwis/waterQuality/list` |
| 인증 | `serviceKey` (쿼리 파라미터) |
| 응답 형식 | JSON |

**요청 파라미터**

| 파라미터 | 필수 | 설명 | 예시 |
|---------|------|------|------|
| serviceKey | 필수 | 공공데이터포털 API 키 | `08854f...` |
| stDt | 필수 | 조회 시작일 (startDate → stDt 매핑, 미입력 시 현재 1시간 전) | `2026-05-18` |
| stTm | 필수 | 조회 시작시간 | `00` |
| edDt | 필수 | 조회 종료일 (endDate → edDt 매핑, 미입력 시 오늘) | `2026-05-25` |
| edTm | 필수 | 조회 종료시간 | `24` |
| sujCode | 권장 | 수계 코드 (미입력 시 응답 지연 44초+) | `318` |
| pageNo | 필수 | 페이지 번호 | `1` |
| numOfRows | 필수 | 페이지당 행 수 | `100` |

**응답 주요 필드**

| 필드 | 설명 |
|------|------|
| fcltyMngNo | 시설관리번호 |
| fcltyMngNm | 시설명 |
| fcltyAddr | 시설 주소 |
| liIndDivName | 구분명 (정수/원수) |
| occrrncDt | 측정일시 (YYYYMMDDhh 형식) |
| phVal / phUnit | pH 값·단위 |
| tbVal / tbUnit | 탁도 값·단위 |
| clVal / clUnit | 잔류염소 값·단위 |

---

### 2.2 K-water 유량 API

| 항목 | 내용 |
|------|------|
| URL | `https://apis.data.go.kr/B500001/rwis/waterFlux/waterFlux` |
| 인증 | `serviceKey` (쿼리 파라미터) |
| 응답 형식 | JSON |

**요청 파라미터**

| 파라미터 | 필수 | 설명 |
|---------|------|------|
| serviceKey | 필수 | API 키 |
| stDt | 필수 | 조회 시작일 (startDate → stDt 매핑) |
| stTm | 필수 | 조회 시작시간 (`00` 고정) |
| edDt | 필수 | 조회 종료일 (endDate → edDt 매핑) |
| edTm | 필수 | 조회 종료시간 (`24` 고정) |
| sujCode | 권장 | 수계 코드 |
| pageNo | 필수 | 페이지 번호 |
| numOfRows | 필수 | 페이지당 행 수 |

**응답 주요 필드**

| 필드 | 설명 |
|------|------|
| fcltyMngNo | 시설관리번호 |
| fcltyNm | 시설명 |
| dataItemDesc | 측정 항목 설명 |
| dataVal | 측정값 |
| itemUnit | 단위 |
| occrrncDt | 측정일시 |
| dataItemDiv | 유형 코드 (`M`: 순간, `D`: 적산) |

---

### 2.3 K-water 정수장 목록 API

| 항목 | 내용 |
|------|------|
| URL | `https://apis.data.go.kr/B500001/rwis/waterQuality/fcltylist/codelist` |
| 인증 | `serviceKey` (쿼리 파라미터) |
| 응답 형식 | JSON |

**요청 파라미터**

| 파라미터 | 값 | 설명 |
|---------|-----|------|
| serviceKey | — | API 키 |
| fcltyDivCode | `2` | 시설 구분 코드 |
| numOfRows | `100` | 최대 행 수 |
| pageNo | `1` | 페이지 번호 |

**응답 주요 필드**

| 필드 | 설명 |
|------|------|
| fcltyMngNm | 정수장 명칭 |
| sujCode | 수계 코드 |
| totalCount | 전체 정수장 수 (41) |

---

### 2.4 Groq AI API

| 항목 | 내용 |
|------|------|
| URL | `https://api.groq.com/openai/v1/chat/completions` |
| 인증 | `Authorization: Bearer {GROQ_API_KEY}` |
| 모델 | `llama-3.3-70b-versatile` |
| 응답 형식 | JSON (OpenAI 호환) |

**요청 본문 (주요 필드)**

```json
{
  "model": "llama-3.3-70b-versatile",
  "messages": [
    {
      "role": "system",
      "content": "당신은 수처리 전문가입니다. 반드시 한국어(한글)로만 답변하세요. 한자를 절대 사용하지 마세요."
    },
    {
      "role": "user",
      "content": "[수질·유량 데이터 및 분석 지시 프롬프트]"
    }
  ],
  "max_tokens": 2048
}
```

**AI 분석 프롬프트 구조**

| 섹션 | 내용 |
|------|------|
| 수질·유량 통합 데이터 | fcltyMngNo 기준 조인된 시설 (최대 10개) |
| 수질 데이터만 있는 시설 | 유량 데이터 없는 시설 (최대 10개) |
| 유량 데이터만 있는 시설 | 수질 데이터 없는 시설 (최대 10개) |
| 분석 요청 | 1.전체현황 / 2.이상징후 / 3.권고사항 |

**응답 필드**

| 필드 | 설명 |
|------|------|
| choices[0].message.content | 분석 리포트 본문 (한국어) |

---

## 3. API 키 관리

| 키 | 저장 위치 | 비고 |
|----|----------|------|
| kwater.api.key | `application.properties` | `.gitignore` 처리 |
| groq.api.key | `application.properties` | `.gitignore` 처리 |
| kwater.suj.code | `application.properties` | 기본 정수장 코드 (기본값: `318`) |

> `application.properties.example` 파일에 키 형식(플레이스홀더)만 커밋하며, 실제 값은 저장소에 포함하지 않는다.
