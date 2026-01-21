package com.example.mubu.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

// AI 분석 + 가격 비교 통합 결과 DTO
@Getter
@Schema(description = "AI 분석 결과와 최저가 정보를 포함한 가격 비교 결과")
public class PriceCompareResult {

    // Summary API 연계를 위한 이미지 식별자
    @Schema(description = "가격 비교 결과 식별용 imageId")
    private String imageId;

    // AI 분석 원문 텍스트
    @Schema(description = "AI가 인식한 상품명 원문")
    private final String aiText;

    // 검색에 사용된 상품명
    @Schema(description = "가격 검색에 사용된 상품명")
    private final String productName;

    // 최저가
    @Schema(description = "최저가 금액", example = "1800")
    private final int lowestPrice;

    // 판매처
    @Schema(description = "최저가 판매처 이름")
    private final String mallName;

    // 상품 링크
    @Schema(description = "상품 상세 페이지 링크")
    private final String link;

    // 상품 이미지
    @Schema(description = "상품 이미지 URL")
    private final String image;

    public PriceCompareResult(
            String aiText,
            String productName,
            int lowestPrice,
            String mallName,
            String link,
            String image
    ) {
        this.aiText = aiText;
        this.productName = productName;
        this.lowestPrice = lowestPrice;
        this.mallName = mallName;
        this.link = link;
        this.image = image;
    }

    // imageId 주입용 setter
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
