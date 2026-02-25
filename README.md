# MUBU (무부) - Backend API Server

> 해외에서 발견한 상품을 촬영하면, AI가 한국 가격과 비교해주는 서비스

<p align="center">
  <strong>사진 한 장으로 해외 vs 한국 가격 비교</strong>
</p>

## Overview

MUBU는 토스 앱인토스(Apps in Toss) 플랫폼에서 동작하는 미니앱의 백엔드 서버입니다. 해외 여행 중 발견한 상품의 사진을 AI로 분석하여 상품명과 현지 가격을 추출하고, 한국 온라인 쇼핑 최저가와 비교해 절약 금액을 알려줍니다.

## Architecture

```
┌─────────────┐     ┌──────────────────────────────────────────────┐
│  Toss App   │     │              MUBU Backend (Fly.io)           │
│  Mini-App   │────▶│                                              │
│ (React/TDS) │     │  Controller                                  │
└─────────────┘     │      │                                       │
                    │      ▼                                       │
                    │  FacadeService                                │
                    │      │                                       │
                    │      ▼                                       │
                    │  CoreService                                 │
                    │      ├── AiAnalyzeService ──▶ Gemini 2.0 Flash
                    │      ├── NaverShoppingService ──▶ Naver Shopping API
                    │      └── ExchangeRateService ──▶ Frankfurter API
                    │                                              │
                    │  ResultStore (In-Memory, 30min TTL)          │
                    └──────────────────────────────────────────────┘
```

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.1 |
| Build | Gradle 8.7 |
| AI Image Analysis | Google Gemini 2.0 Flash |
| Product Search | Naver Shopping API |
| Exchange Rate | Frankfurter API (ECB data) |
| API Docs | Springdoc OpenAPI 3.0 (Swagger UI) |
| Deploy | Fly.io (Docker, Tokyo region) |

## Key Features

### AI-Powered Product Recognition
Google Gemini 2.0 Flash를 활용한 이미지 분석으로 상품명, 현지 가격, 통화를 자동 추출합니다. 구조화된 프롬프트 엔지니어링으로 파싱 정확도를 높이고, 한국어 검색 키워드를 동시에 생성합니다.

### Multi-Tier Search Fallback
네이버 쇼핑 검색에서 정확한 상품을 찾기 위한 3단계 Fallback 전략:
1. **Full Keyword + Relevance Filter** — 전체 키워드 검색 후 첫 단어 기반 관련성 필터링
2. **Two-Word Retry** — 다중 단어 키워드를 2단어로 축소하여 재검색
3. **English Brand Fallback** — AI 텍스트에서 영문 브랜드명 추출 후 한글 상품 유형과 결합하여 검색

### Real-Time Currency Conversion
Frankfurter API(ECB 기반)를 활용한 실시간 환율 변환과, VND/TWD 등 미지원 통화에 대한 고정 환율 Fallback을 제공합니다.

### Rate Limit Resilience
Gemini API 429 응답에 대한 지수 백오프 재시도(5s → 10s → 15s, 최대 3회)와 요청 간 2초 간격 Burst Control을 구현했습니다.

## API Endpoints

### Price Comparison

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/price/compare-with-image` | 이미지 업로드 → AI 분석 → 최저가 비교 (메인 API) |
| `POST` | `/api/price/result/summary` | 가격 확정 후 절약 금액 및 요약 계산 |
| `GET` | `/api/price/external/link` | 네이버 쇼핑 외부 링크 조회 |

### System

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/health` | Health check |
| `GET` | `/common/version` | Service version |
| `GET` | `/swagger-ui.html` | API 문서 (Swagger UI) |

### Example: Price Comparison Flow

```bash
# 1. 이미지로 가격 비교 요청
curl -X POST https://mubu.fly.dev/api/price/compare-with-image \
  -F "file=@product.jpg" \
  -F "imageId=my-uuid-123"

# Response
{
  "imageId": "my-uuid-123",
  "aiText": "상품명: Pocky Strawberry\n가격: 35 THB\nsearchKeywordKr: 포키 딸기",
  "productName": "포키 딸기맛 과자",
  "lowestPrice": 1800,
  "mallName": "쿠팡",
  "link": "https://www.coupang.com/...",
  "image": "https://image.coupang.com/...",
  "localPrice": 35,
  "localCurrency": "THB"
}

# 2. 절약 금액 요약
curl -X POST https://mubu.fly.dev/api/price/result/summary \
  -H "Content-Type: application/json" \
  -d '{"imageId":"my-uuid-123","localPrice":35,"currency":"THB","priceSource":"AI"}'

# Response
{
  "summary": "한국에서 사면 542원 절약!",
  "savedAmount": 542,
  "localPriceKrw": 2342,
  "koreaPrice": 1800
}
```

## Project Structure

```
src/main/java/com/example/mubu/
├── controller/
│   ├── PriceCompareController.java    # 가격 비교 API
│   ├── CommonController.java          # 헬스체크, 버전
│   └── HealthController.java          # Fly.io 헬스체크
├── service/
│   ├── PriceCompareFacadeService.java # Facade (단일 진입점)
│   ├── PriceCompareCoreService.java   # 핵심 비교 로직 오케스트레이션
│   ├── AiAnalyzeService.java          # Gemini AI 이미지 분석
│   ├── NaverShoppingService.java      # 네이버 쇼핑 검색 + 3단계 Fallback
│   ├── ExchangeRateService.java       # 환율 변환
│   └── PriceCompareSummaryService.java# 절약 금액 계산
├── client/
│   ├── GeminiClient.java              # Gemini API HTTP 클라이언트
│   └── NaverShoppingClient.java       # Naver Shopping API 클라이언트
├── dto/
│   ├── ai/                            # AI 분석 결과 DTO
│   ├── gemini/                        # Gemini API 요청/응답 DTO
│   ├── naver/                         # Naver API 응답 DTO
│   └── price/                         # 가격 비교 요청/응답 DTO
├── repository/
│   └── PriceCompareResultStore.java   # In-Memory 캐시 (30min TTL)
├── config/
│   ├── WebConfig.java                 # CORS 설정
│   ├── RestTemplateConfig.java        # HTTP 클라이언트 타임아웃
│   └── OpenApiConfig.java             # Swagger 설정
└── common/
    ├── exception/                     # 글로벌 예외 핸들러
    └── util/MallLinkUtils.java        # 앱 딥링크 → 웹 URL 변환
```

## Getting Started

### Prerequisites
- Java 17+
- Gradle 8.7+

### Environment Variables
```bash
export GEMINI_API_KEY=your_gemini_api_key
export NAVER_CLIENT_ID=your_naver_client_id
export NAVER_CLIENT_SECRET=your_naver_client_secret
```

### Run Locally
```bash
./gradlew bootRun
# Server starts at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Build & Deploy
```bash
# Docker build
docker build -t mubu .

# Deploy to Fly.io
fly deploy
```

## Deployment

Fly.io에 Docker 컨테이너로 배포됩니다.

| Config | Value |
|--------|-------|
| Region | Tokyo (nrt) |
| VM | Shared CPU, 1GB RAM |
| Auto-stop | Suspend (비용 최적화) |
| Scale | 0 ~ auto (scale-to-zero) |
| HTTPS | Forced |

## Supported Currencies

THB (태국), JPY (일본), USD (미국), EUR (유럽), CNY (중국), SGD (싱가포르), VND (베트남), PHP (필리핀), IDR (인도네시아), HKD (홍콩), TWD (대만), AUD (호주)

## Related

- **Frontend**: [mubu-frontend](https://github.com/AppinToss-mubu/mubu-frontend) — React + TDS 토스 미니앱
- **Platform**: [Apps in Toss](https://apps-in-toss.toss.im/) — 토스 미니앱 플랫폼
