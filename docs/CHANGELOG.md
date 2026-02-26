# Changelog

All notable changes to this project will be documented in this file.

## [1.0.1] - 2026-02-27

### Improved
- 묶음/세트 상품 단가 계산 로직 추가 (개입, 개세트, 팩, P, EA, xN 등 패턴 감지 → 단가 자동 환산)
- AI 프롬프트 v2: searchKeywordKr 작성 규칙 강화, 5개 카테고리 예시 추가 (치약/과자/선크림/게임컨/감자칩)
- 네이버 쇼핑 관련성 필터 개선: 첫 키워드만 체크 → 전체 키워드 매칭 점수 기반 선택

### Added
- NaverShoppingService 단위 테스트 10개 (키워드 매칭, 묶음 단가, Fallback, HTML 제거)
- GeminiAnalyzeResult 파싱 단위 테스트 8개 (프롬프트 응답, 12개 통화, 엣지 케이스)

## [1.0.0] - 2026-02-25

### Released
- MVP 출시 (토스 앱인토스 미니앱 백엔드)
- Gemini 2.0 Flash 기반 이미지 AI 분석 (상품명/가격/통화/키워드 추출)
- 네이버 쇼핑 API 연동 (3단계 Fallback 검색 전략)
- Frankfurter API 환율 변환 + 미지원 통화 고정 환율 Fallback
- 절약 금액 계산 API
- 외부 쇼핑 링크 조회 API
- Fly.io 도쿄(nrt) 리전 배포 (Docker, scale-to-zero)
