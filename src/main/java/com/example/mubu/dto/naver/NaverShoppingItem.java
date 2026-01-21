package com.example.mubu.dto.naver;

import lombok.Getter;
import lombok.Setter;

// 네이버 쇼핑 상품 단위 DTO
@Getter
@Setter
public class NaverShoppingItem {

    // 상품명 (HTML 태그 포함 → 후처리 필요)
    private String title;

    // 상품 상세 링크
    private String link;

    // 상품 이미지 URL
    private String image;

    // 최저가 (문자열로 내려옴)
    private String lprice;

    // 최고가
    private String hprice;

    // 판매처 이름
    private String mallName;

    // 네이버 상품 ID
    private String productId;

    // 브랜드명
    private String brand;

    // 제조사
    private String maker;

    // 카테고리 정보
    private String category1;
    private String category2;
    private String category3;
}
