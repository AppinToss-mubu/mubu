package com.example.mubu.service;

import com.example.mubu.client.NaverShoppingClient;
import com.example.mubu.common.util.MallLinkUtils;
import com.example.mubu.dto.naver.NaverShoppingItem;
import com.example.mubu.dto.naver.NaverShoppingResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;


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
        return findLowestPriceItem(query, null);
    }

    // 네이버 쇼핑 검색 후 최저가 상품 1개 반환 (원본 상품명 포함)
    public NaverShoppingItem findLowestPriceItem(String query, String originalProductName) {
        // 검색어 로깅 추가
        System.out.println("[NAVER_SEARCH] 검색어: " + query);
        if (originalProductName != null) {
            System.out.println("[NAVER_SEARCH] 원본 상품명: " + originalProductName);
        }

        String[] keywords = query.split("\\s+");
        String firstKeyword = (keywords.length > 0 && keywords[0].length() > 1) ? keywords[0] : null;

        // 1차 시도: 전체 키워드로 검색 (관련성 필터링 포함)
        NaverShoppingItem result = searchWithQuery(query, firstKeyword);

        if (result != null) {
            System.out.println("[NAVER_SEARCH] 관련 상품 찾음: " + result.getTitle());
            return result;
        }

        // 2차 시도: 2단어로 재시도
        if (query.contains(" ") && keywords.length >= 2) {
            String twoWordQuery = keywords[0] + " " + keywords[1];
            System.out.println("[NAVER_SEARCH] 2단어 재시도: " + twoWordQuery);
            result = searchWithQuery(twoWordQuery, firstKeyword);

            if (result != null) {
                System.out.println("[NAVER_SEARCH] 2단어 재시도 성공: " + result.getTitle());
                return result;
            }
        }

        // 3차 시도: 영문 브랜드명으로 Fallback
        if (originalProductName != null) {
            String englishBrand = extractEnglishWords(originalProductName);
            if (!englishBrand.isEmpty()) {
                String productType = getProductType(query);
                String fallbackKeyword = englishBrand + (productType.isEmpty() ? "" : " " + productType);
                System.out.println("[NAVER_SEARCH] Fallback 검색어: " + fallbackKeyword);

                String[] fallbackWords = englishBrand.split("\\s+");
                String fallbackFirstKeyword = (fallbackWords.length > 0) ? fallbackWords[0] : null;
                result = searchWithQuery(fallbackKeyword, fallbackFirstKeyword);

                if (result != null) {
                    System.out.println("[NAVER_SEARCH] Fallback 성공: " + result.getTitle());
                    return result;
                }
            }
        }

        System.out.println("[NAVER_SEARCH] 관련 상품을 찾지 못함 → lowestPrice=0 반환");
        return null;
    }

    // 실제 검색 로직 (관련성 필터링 포함)
    private NaverShoppingItem searchWithQuery(String query, String firstKeyword) {
        // 네이버 쇼핑 API 호출 - 관련도순(sim)으로 검색
        NaverShoppingResponse response =
                naverShoppingClient.search(query, 20, "sim");

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

        // 1. 관련 상품만 필터링 (첫 번째 키워드가 포함된 것만)
        List<NaverShoppingItem> relevantItems = response.getItems().stream()
                .filter(item -> item.getLprice() != null && !item.getLprice().isBlank())
                .filter(item -> parsePrice(item.getLprice()) > 0)
                .filter(item -> {
                    if (firstKeyword == null || firstKeyword.length() <= 1) {
                        return true;
                    }
                    String title = sanitizeTitle(item.getTitle()).toLowerCase();
                    return title.contains(firstKeyword.toLowerCase());
                })
                .toList();

        System.out.println("[NAVER_SEARCH] 관련성 있는 아이템 수: " + relevantItems.size());
        if (relevantItems.isEmpty()) {
            System.out.println("[NAVER_SEARCH] 관련 상품 없음");
            return null;
        }

        // 2. 관련 상품 중 최저가 선택
        return relevantItems.stream()
                .min(Comparator.comparingInt(item -> parsePrice(item.getLprice())))
                .map(this::sanitizeItem)
                .orElse(null);
    }

    // HTML 태그 제거 + 앱 딥링크를 웹 URL로 정규화 (토스 인앱 등에서 웹만 열 수 있도록)
    private NaverShoppingItem sanitizeItem(NaverShoppingItem item) {
        item.setTitle(item.getTitle().replaceAll("<[^>]*>", ""));
        item.setLink(MallLinkUtils.normalizeToWebUrl(item.getLink()));
        return item;
    }

    // Title에서 HTML 태그 제거 (문자열만 반환)
    private String sanitizeTitle(String title) {
        return title.replaceAll("<[^>]*>", "");
    }

    // 가격 파싱
    private int parsePrice(String price) {
        try {
            return Integer.parseInt(price);
        } catch (Exception e) {
            return -1;
        }
    }

    // 영문 단어 추출 - 상품명 라인에서만 추출
    private String extractEnglishWords(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // "상품명:" 라인만 추출
        String productLine = text;
        if (text.contains("상품명:")) {
            String[] lines = text.split("\n");
            for (String line : lines) {
                if (line.startsWith("상품명:")) {
                    productLine = line.replace("상품명:", "").trim();
                    break;
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (String word : productLine.split("\\s+")) {
            // 영문만 포함된 단어 추출 (최소 2글자 이상)
            // 숫자나 통화코드(THB, USD 등) 제외
            if (word.matches("[a-zA-Z]{2,}") && !word.matches("(?i)(THB|USD|KRW|JPY|EUR|CNY|SGD|PHP|IDR|VND|HKD|TWD|AUD|CAD|GBP|CHF)")) {
                sb.append(word).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // 한글 상품 유형 추출 (치약, 과자 등)
    private String getProductType(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return "";
        }
        String[] words = keyword.split("\\s+");
        if (words.length > 0) {
            String lastWord = words[words.length - 1];
            // 마지막 단어가 한글이면 상품 유형으로 간주
            if (lastWord.matches(".*[가-힣].*")) {
                return lastWord;
            }
        }
        return "";
    }
}
