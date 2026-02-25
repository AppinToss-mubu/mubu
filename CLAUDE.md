# MUBU Backend - Claude Context

## 프로젝트 개요
**MUBU(무부)** 백엔드 - 해외 쇼핑 가격 비교 서비스 API 서버
- 토스 앱인토스(Apps in Toss) 미니앱의 백엔드
- 사용자가 해외에서 촬영한 상품 사진을 AI로 분석하고, 한국 최저가와 비교

## 현재 상태
- **배포 완료**: Fly.io 도쿄(nrt) 리전
- **프론트엔드**: mubu-frontend (별도 레포) → .ait 빌드 후 토스 검토 요청 완료
- **단계**: MVP 완성 → 유지보수/개선 단계

## 기술 스택
| 구분 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.2.1 |
| 빌드 | Gradle 8.7 |
| AI | Google Gemini 2.0 Flash (이미지 분석) |
| 쇼핑 검색 | Naver Shopping API |
| 환율 | Frankfurter API (ECB 데이터) |
| 문서화 | Springdoc OpenAPI 2.3.0 |
| 배포 | Fly.io (Docker, scale-to-zero) |

## 아키텍처
```
Controller → FacadeService → CoreService → AiAnalyzeService (Gemini)
                                         → NaverShoppingService (Naver API)
                                         → ExchangeRateService (Frankfurter)
```
- **Facade 패턴**: PriceCompareFacadeService가 단일 진입점
- **In-Memory 캐시**: PriceCompareResultStore (ConcurrentHashMap, 30분 TTL)
- **DB 없음**: 현재 인메모리만 사용 (추후 Redis/DB 전환 가능)

## API 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/price/compare-with-image` | **핵심 API** - 이미지 업로드 → AI 분석 → 최저가 비교 |
| POST | `/api/price/result/summary` | 가격 확정 후 절약 금액/요약 계산 |
| GET | `/api/price/external/link` | 네이버 쇼핑 외부 링크 조회 |
| POST | `/api/price/compare` | imageId 기반 비교 (미구현, 확장용) |
| GET | `/health`, `/common/health` | 헬스체크 |

## 핵심 비즈니스 로직
1. **AI 분석** (AiAnalyzeService): Gemini에 구조화된 프롬프트로 상품명/가격/통화/한국어 검색 키워드 추출
2. **쇼핑 검색** (NaverShoppingService): 3단계 Fallback 전략
   - 1차: 전체 키워드 + 첫 단어 관련성 필터
   - 2차: 2단어로 축소 재검색
   - 3차: 영문 브랜드명 + 한글 상품 유형 Fallback
3. **환율 변환** (ExchangeRateService): Frankfurter API + VND/TWD 등 미지원 통화 고정 환율 Fallback
4. **절약 계산** (PriceCompareSummaryService): 현지가격(KRW 환산) - 한국 최저가

## 환경 변수
```
GEMINI_API_KEY      # Google Gemini API 키
NAVER_CLIENT_ID     # 네이버 쇼핑 API Client ID
NAVER_CLIENT_SECRET # 네이버 쇼핑 API Client Secret
```

## 주요 파일 위치
- 서비스 로직: `src/main/java/com/example/mubu/service/`
- 컨트롤러: `src/main/java/com/example/mubu/controller/`
- 외부 클라이언트: `src/main/java/com/example/mubu/client/`
- DTO: `src/main/java/com/example/mubu/dto/`
- 설정: `src/main/java/com/example/mubu/config/`
- CORS: `WebConfig.java` (토스 도메인 `*.tossmini.com` 허용)

## 프론트엔드 연동
- 프론트엔드 레포: `mubu-frontend/`
- API Base URL: `VITE_API_BASE_URL` 환경변수로 주입
- CORS: localhost 개발 포트 + 토스 미니앱 도메인 허용
- 이미지: multipart/form-data (최대 10MB)

## 작업 시 주의사항
- Gemini API는 429 Rate Limit이 발생할 수 있음 → 지수 백오프 재시도 구현됨
- 네이버 쇼핑 검색 결과 title에 HTML 태그 포함 → sanitize 필수
- 쇼핑몰 링크에 앱 딥링크(coupang://) 포함 가능 → MallLinkUtils로 웹 URL 정규화
- fly.toml의 auto_stop=suspend → 콜드 스타트 지연 있음
