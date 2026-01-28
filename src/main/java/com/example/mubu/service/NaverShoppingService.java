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
        // 검색어 로깅 추가
        System.out.println("[NAVER_SEARCH] 검색어: " + query);
        
        NaverShoppingItem result = searchWithQuery(query);
        
        // 결과 없으면 2단어로 재시도
        if (result == null && query.contains(" ")) {
            String[] words = query.split("\\s+");
            if (words.length >= 2) {
                String twoWordQuery = words[0] + " " + words[1];
                System.out.println("[NAVER_SEARCH] 2단어 재시도: " + twoWordQuery);
                result = searchWithQuery(twoWordQuery);
            }
        }
        
        return result;
    }

    // 실제 검색 로직 (재사용 가능하도록 분리)
    private NaverShoppingItem searchWithQuery(String query) {
        // 네이버 쇼핑 API 호출
        NaverShoppingResponse response =
                naverShoppingClient.search(query, 20, "asc");

        // 결과 로깅 추가
        int resultCount = (response != null && response.getItems() != null)
                ? response.getItems().size() : 0;
        System.out.println("[NAVER_SEARCH] 결과 수: " + resultCount);

        if (response == null ||
                response.getItems() == null ||
                response.getItems().isEmpty()) {
            return null;
        }

        // 첫 번째 아이템 디버그
        if (resultCount > 0) {
            var firstItem = response.getItems().get(0);
            System.out.println("[NAVER_SEARCH] 첫 번째 아이템 - title: " + firstItem.getTitle() + ", lprice: " + firstItem.getLprice());
        }

        // 가격 있는 아이템 수 확인
        long validCount = response.getItems().stream()
                .filter(item -> item.getLprice() != null && !item.getLprice().isBlank())
                .filter(item -> parsePrice(item.getLprice()) > 0)
                .count();
        System.out.println("[NAVER_SEARCH] 가격 유효 아이템 수: " + validCount);

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
