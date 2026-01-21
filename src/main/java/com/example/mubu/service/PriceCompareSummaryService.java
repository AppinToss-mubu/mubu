package com.example.mubu.service;

import com.example.mubu.dto.ai.PriceCompareResult;
import com.example.mubu.dto.price.PriceCompareSummaryRequest;
import com.example.mubu.dto.price.PriceCompareSummaryResponse;
import org.springframework.stereotype.Service;

@Service
public class PriceCompareSummaryService {

    private final ExchangeRateService exchangeRateService;

    public PriceCompareSummaryService(
            ExchangeRateService exchangeRateService
    ) {
        this.exchangeRateService = exchangeRateService;
    }

    public PriceCompareSummaryResponse summarize(
            PriceCompareResult compareResult,
            PriceCompareSummaryRequest request
    ) {
        // 1. 현지 가격 → KRW 환산
        int localPriceKrw = exchangeRateService.convertToKrw(
                request.getLocalPrice(),
                request.getCurrency()
        );

        // 2. 한국 최저가
        int koreaPrice = compareResult.getLowestPrice();

        // 3. 절약 금액 계산
        int savedAmount = localPriceKrw - koreaPrice;

        // 4. 요약 문구 생성
        String summary;
        if (savedAmount > 0) {
            summary = "한국에서 사면 " + savedAmount + "원 절약!";
        } else if (savedAmount < 0) {
            summary = "현지에서 사는 것이 더 저렴합니다.";
        } else {
            summary = "한국과 현지 가격이 동일합니다.";
        }

        return new PriceCompareSummaryResponse(
                summary,
                Math.max(savedAmount, 0),
                localPriceKrw,
                koreaPrice
        );
    }
}
