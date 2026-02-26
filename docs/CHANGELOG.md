# Changelog

All notable changes to this project will be documented in this file.

## [1.0.1] - 2026-02-27

### Improved
- 묶음/세트 상품 단가 계산 로직 추가 (개입, 개세트, 팩, P, EA, xN 등 패턴 감지 → 단가 자동 환산)

## [1.0.0] - 2026-02-25

### Released
- MVP 출시 (토스 앱인토스 미니앱 백엔드)
- Gemini 2.0 Flash 기반 이미지 AI 분석 (상품명/가격/통화/키워드 추출)
- 네이버 쇼핑 API 연동 (3단계 Fallback 검색 전략)
- Frankfurter API 환율 변환 + 미지원 통화 고정 환율 Fallback
- 절약 금액 계산 API
- 외부 쇼핑 링크 조회 API
- Fly.io 도쿄(nrt) 리전 배포 (Docker, scale-to-zero)
