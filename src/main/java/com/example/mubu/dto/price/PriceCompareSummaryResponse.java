package com.example.mubu.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가격 비교 결과 요약 응답 DTO")
public class PriceCompareSummaryResponse {

    @Schema(description = "요약 문구", example = "한국에서 사면 8,200원 절약!")
    private final String summary;

    @Schema(description = "절약 금액 (KRW)", example = "8200")
    private final int savedAmount;

    @Schema(description = "현지 가격 (KRW 환산)", example = "23000")
    private final int localPriceKrw;

    @Schema(description = "한국 최저가", example = "14800")
    private final int koreaPrice;

    public PriceCompareSummaryResponse(
            String summary,
            int savedAmount,
            int localPriceKrw,
            int koreaPrice
    ) {
        this.summary = summary;
        this.savedAmount = savedAmount;
        this.localPriceKrw = localPriceKrw;
        this.koreaPrice = koreaPrice;
    }

    public String getSummary() {
        return summary;
    }

    public int getSavedAmount() {
        return savedAmount;
    }

    public int getLocalPriceKrw() {
        return localPriceKrw;
    }

    public int getKoreaPrice() {
        return koreaPrice;
    }
}
