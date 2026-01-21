package com.example.mubu.service;

import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {

    public int convertToKrw(int amount, String currency) {

        // 초기 버전: 고정 환율 (나중에 API 교체)
        return switch (currency.toUpperCase()) {
            case "JPY" -> (int) (amount * 0.92);
            case "USD" -> (int) (amount * 1350);
            default -> throw new IllegalArgumentException("지원하지 않는 통화");
        };
    }
}
