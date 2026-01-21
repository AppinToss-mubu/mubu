package com.example.mubu.service;

import com.example.mubu.client.NaverShoppingClient;
import com.example.mubu.dto.naver.NaverShoppingItem;
import com.example.mubu.dto.naver.NaverShoppingResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;


//title HTML 제거
//lprice 파싱
//최저가 상품 1개 선택
@Service
public class NaverShoppingService {

    private final NaverShoppingClient naverShoppingClient;

    public NaverShoppingService(NaverShoppingClient naverShoppingClient) {
        this.naverShoppingClient = naverShoppingClient;
    }

    // 네이버 쇼핑 검색 후 최저가 상품 1개 반환
    public NaverShoppingItem findLowestPriceItem(String query) {

        // 네이버 쇼핑 API 호출
        NaverShoppingResponse response =
                naverShoppingClient.search(query, 20, "asc");

        if (response == null ||
                response.getItems() == null ||
                response.getItems().isEmpty()) {
            return null;
        }

        return response.getItems().stream()
                // 가격 없는 상품 제외
                .filter(item -> item.getLprice() != null && !item.getLprice().isBlank())
                // lprice 문자열 → int 변환
                .filter(item -> parsePrice(item.getLprice()) > 0)
                // 최저가 기준 정렬
                .min(Comparator.comparingInt(item -> parsePrice(item.getLprice())))
                // title HTML 태그 제거
                .map(this::sanitizeItem)
                .orElse(null);
    }

    // HTML 태그 제거
    private NaverShoppingItem sanitizeItem(NaverShoppingItem item) {
        item.setTitle(item.getTitle().replaceAll("<[^>]*>", ""));
        return item;
    }

    // 가격 파싱
    private int parsePrice(String price) {
        try {
            return Integer.parseInt(price);
        } catch (Exception e) {
            return -1;
        }
    }
}
