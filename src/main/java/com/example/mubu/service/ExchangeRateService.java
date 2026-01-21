package com.example.mubu.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExchangeRateService {

    // Frankfurter 최신 환율 API
    // 예: base=JPY, symbols=KRW
    private static final String API_URL =
            "https://api.frankfurter.dev/v1/latest?base=%s&symbols=%s";

    private final RestTemplate restTemplate = new RestTemplate();

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

        // Frankfurter API 호출
        String url = String.format(API_URL, currency, "KRW");

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
        System.out.println(
                "[EXCHANGE_RATE] " + currency + " → KRW = " + rate
        );

        // 소수점 버림 (UX 단순화)
        return (int) (amount * rate);
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
