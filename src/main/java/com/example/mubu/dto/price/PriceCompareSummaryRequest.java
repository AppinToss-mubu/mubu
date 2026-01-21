package com.example.mubu.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가격 비교 결과 요약 요청 DTO")
public class PriceCompareSummaryRequest {

    @Schema(description = "이미지 식별자", example = "img_001")
    private String imageId;

    @Schema(description = "확정된 현지 가격 (AI 추출 또는 사용자 입력)", example = "25000")
    private Integer localPrice;

    @Schema(description = "현지 통화", example = "JPY")
    private String currency;

    @Schema(description = "가격 출처 (AI | USER)", example = "AI")
    private String priceSource;

    public String getImageId() {
        return imageId;
    }

    public Integer getLocalPrice() {
        return localPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPriceSource() {
        return priceSource;
    }
}
