package com.example.mubu.dto.naver;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 네이버 쇼핑 검색 API 응답 최상위 DTO
@Getter
@Setter
public class NaverShoppingResponse {

    // 전체 검색 결과 수
    private int total;

    // 검색된 상품 목록
    private List<NaverShoppingItem> items;
}
