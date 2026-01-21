package com.example.mubu.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;

// 확장/표준 API 요청 DTO
// imageId 기반
@Schema(description = "가격 비교 요청 DTO")
public class PriceCompareRequest {

    @Schema(
            description = "업로드된 이미지 식별자",
            example = "img_123456"
    )
    private String imageId;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
