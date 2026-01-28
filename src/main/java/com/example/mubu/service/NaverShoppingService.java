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
        return findLowestPriceItem(query, null);
    }

    // 네이버 쇼핑 검색 후 최저가 상품 1개 반환 (원본 상품명 포함)
    public NaverShoppingItem findLowestPriceItem(String query, String originalProductName) {
        // 검색어 로깅 추가
        System.out.println("[NAVER_SEARCH] 검색어: " + query);
        if (originalProductName != null) {
            System.out.println("[NAVER_SEARCH] 원본 상품명: " + originalProductName);
        }
        
        NaverShoppingItem result = searchWithQuery(query);
        
        // 결과 관련성 체크 - 첫 번째 키워드(브랜드명)가 반드시 포함되어야 함
        if (result != null) {
            String firstTitle = sanitizeTitle(result.getTitle()).toLowerCase();
            String[] keywords = query.split("\\s+");
            
            boolean isRelevant = false;
            // 첫 번째 키워드(브랜드명)가 반드시 포함되어야 함
            if (keywords.length > 0 && keywords[0].length() > 1) {
                isRelevant = firstTitle.contains(keywords[0].toLowerCase());
            }
            
            if (!isRelevant) {
                System.out.println("[NAVER_SEARCH] 관련성 낮음 (첫 번째 키워드 '" + keywords[0] + "' 없음), 첫 번째 결과: " + result.getTitle());
                result = null; // 관련성 없으면 null로 설정하여 재시도
            } else {
                System.out.println("[NAVER_SEARCH] 관련성 확인됨 (첫 번째 키워드 '" + keywords[0] + "' 포함): " + result.getTitle());
                return result; // 관련성 있으면 반환
            }
        }
        
        // 결과 없거나 관련성 낮으면 2단어로 재시도
        if (result == null && query.contains(" ")) {
            String[] words = query.split("\\s+");
            if (words.length >= 2) {
                String twoWordQuery = words[0] + " " + words[1];
                System.out.println("[NAVER_SEARCH] 2단어 재시도: " + twoWordQuery);
                result = searchWithQuery(twoWordQuery);
                
                // 2단어 재시도 결과도 관련성 체크 - 첫 번째 키워드(브랜드명)가 반드시 포함되어야 함
                if (result != null) {
                    String firstTitle = sanitizeTitle(result.getTitle()).toLowerCase();
                    String[] twoWords = twoWordQuery.split("\\s+");
                    boolean isRelevant = false;
                    // 첫 번째 키워드(브랜드명)가 반드시 포함되어야 함
                    if (twoWords.length > 0 && twoWords[0].length() > 1) {
                        isRelevant = firstTitle.contains(twoWords[0].toLowerCase());
                    }
                    if (!isRelevant) {
                        System.out.println("[NAVER_SEARCH] 2단어 재시도 결과도 관련성 낮음 (첫 번째 키워드 '" + twoWords[0] + "' 없음)");
                        result = null;
                    } else {
                        System.out.println("[NAVER_SEARCH] 2단어 재시도 결과 관련성 확인됨 (첫 번째 키워드 '" + twoWords[0] + "' 포함)");
                    }
                }
            }
        }
        
        // 관련성 없으면 영문 브랜드명으로 재시도
        if (result == null && originalProductName != null) {
            String englishBrand = extractEnglishWords(originalProductName);
            if (!englishBrand.isEmpty()) {
                String productType = getProductType(query);
                String fallbackKeyword = englishBrand + (productType.isEmpty() ? "" : " " + productType);
                System.out.println("[NAVER_SEARCH] Fallback 검색어: " + fallbackKeyword);
                result = searchWithQuery(fallbackKeyword);
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
