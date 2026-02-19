package com.example.mubu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    // Frankfurter 최신 환율 API
    // 예: base=JPY, symbols=KRW
    private static final String API_URL =
            "https://api.frankfurter.dev/v1/latest?base=%s&symbols=%s";

    private final RestTemplate restTemplate;

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 현지 통화를 KRW로 변환
     * - AI 인식 가격 / 사용자 입력 가격 모두 이 메서드를 탄다
     * - 환율 계산 책임은 무조건 백엔드
     */
    public int convertToKrw(int amount, String currency) {

        // 이미 KRW면 변환 불필요
        if ("KRW".equalsIgnoreCase(currency)) {
            return amount;
        }

        // 지원 안 되는 통화 fallback (고정 환율)
        // Frankfurter API는 ECB 데이터 기반이라 VND, TWD 등 일부 통화 미지원
        Double fallbackRate = getFallbackRate(currency);
        if (fallbackRate != null) {
            log.info("[EXCHANGE_RATE] {} → KRW = {} (fallback 사용)", currency, fallbackRate);
            return (int) (amount * fallbackRate);
        }

        // Frankfurter API 호출
        String url = String.format(API_URL, currency, "KRW");

        try {
            FrankfurterResponse response =
                    restTemplate.getForObject(url, FrankfurterResponse.class);

            // 방어 코드 (API 장애 대비)
            if (response == null
                    || response.getRates() == null
                    || !response.getRates().containsKey("KRW")) {
                throw new IllegalStateException("환율 정보를 가져올 수 없습니다.");
            }

            double rate = response.getRates().get("KRW");

            // 🔍 환율 API 실제 호출 확인용 로그
            log.info("[EXCHANGE_RATE] {} → KRW = {}", currency, rate);

            // 소수점 버림 (UX 단순화)
            return (int) (amount * rate);
        } catch (RestClientException e) {
            // API 호출 실패 시 (404 등) fallback 재시도
            log.warn("[EXCHANGE_RATE] API 호출 실패: {}", e.getMessage());
            Double retryFallbackRate = getFallbackRate(currency);
            if (retryFallbackRate != null) {
                log.info("[EXCHANGE_RATE] API 실패, {} → KRW = {} (fallback 사용)", currency, retryFallbackRate);
                return (int) (amount * retryFallbackRate);
            }
            throw new IllegalStateException("환율 정보를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 지원 안 되는 통화에 대한 고정 환율 반환
     * - VND, TWD 등 Frankfurter API에서 지원하지 않는 통화
     * - 환율은 대략적인 값이며, 필요시 주기적으로 업데이트 필요
     * 
     * @param currency 통화 코드
     * @return 환율 (없으면 null)
     */
    private Double getFallbackRate(String currency) {
        if (currency == null) {
            return null;
        }
        
        String upperCurrency = currency.toUpperCase();
        
        // VND (베트남 동): 1 VND ≈ 0.057 KRW
        if ("VND".equals(upperCurrency)) {
            return 0.057;
        }
        
        // TWD (대만 달러): 1 TWD ≈ 44 KRW
        if ("TWD".equals(upperCurrency)) {
            return 44.0;
        }
        
        return null;
    }

    /**
     * Frankfurter 응답 DTO
     * 필요한 건 rates 뿐이라 최소 필드만 사용
     */
    private static class FrankfurterResponse {
        private Map<String, Double> rates;

        public Map<String, Double> getRates() {
            return rates;
        }

        public void setRates(Map<String, Double> rates) {
            this.rates = rates;
        }
    }
}
