package com.example.mubu.service;

import com.example.mubu.client.NaverShoppingClient;
import com.example.mubu.common.util.MallLinkUtils;
import com.example.mubu.dto.naver.NaverShoppingItem;
import com.example.mubu.dto.naver.NaverShoppingResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    // 실제 검색 로직 (키워드 매칭 점수 기반 관련성 필터링)
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

        String[] keywords = query.split("\\s+");

        // 1. 유효 가격 + 최소 1개 키워드 매칭 필터링
        List<NaverShoppingItem> relevantItems = response.getItems().stream()
                .filter(item -> item.getLprice() != null && !item.getLprice().isBlank())
                .filter(item -> parsePrice(item.getLprice()) > 0)
                .filter(item -> countKeywordMatches(item, keywords) > 0)
                .toList();

        System.out.println("[NAVER_SEARCH] 관련성 있는 아이템 수: " + relevantItems.size());
        if (relevantItems.isEmpty()) {
            System.out.println("[NAVER_SEARCH] 관련 상품 없음");
            return null;
        }

        // 2. 가장 관련성 높은 상품(키워드 매칭 수 최대) 중 최저가 선택
        int maxMatches = relevantItems.stream()
                .mapToInt(item -> countKeywordMatches(item, keywords))
                .max().orElse(0);

        System.out.println("[NAVER_SEARCH] 최대 키워드 매칭 수: " + maxMatches);

        return relevantItems.stream()
                .filter(item -> countKeywordMatches(item, keywords) == maxMatches)
                .min(Comparator.comparingInt(item -> parsePrice(item.getLprice())))
                .map(this::sanitizeItem)
                .orElse(null);
    }

    // 상품 타이틀에 검색 키워드가 몇 개 포함되는지 카운트
    private int countKeywordMatches(NaverShoppingItem item, String[] keywords) {
        String title = sanitizeTitle(item.getTitle()).toLowerCase();
        return (int) Arrays.stream(keywords)
                .filter(k -> k.length() > 1)
                .filter(k -> title.contains(k.toLowerCase()))
                .count();
    }

    // HTML 태그 제거 + 앱 딥링크를 웹 URL로 정규화 + 묶음 단가 계산
    private NaverShoppingItem sanitizeItem(NaverShoppingItem item) {
        String cleanTitle = item.getTitle().replaceAll("<[^>]*>", "");
        item.setTitle(cleanTitle);
        item.setLink(MallLinkUtils.normalizeToWebUrl(item.getLink()));

        // 묶음/세트 상품인 경우 단가 계산
        int bundleQty = extractBundleQuantity(cleanTitle);
        if (bundleQty > 1) {
            int originalPrice = parsePrice(item.getLprice());
            if (originalPrice > 0) {
                int unitPrice = originalPrice / bundleQty;
                System.out.println("[NAVER_SEARCH] 묶음 감지: " + cleanTitle +
                        " → " + bundleQty + "개, 원가=" + originalPrice + ", 단가=" + unitPrice);
                item.setLprice(String.valueOf(unitPrice));
            }
        }

        return item;
    }

    /**
     * 상품명에서 묶음 수량을 추출한다.
     * 패턴: "3개입", "3개세트", "3팩", "3P", "3EA", "x3", "X3" 등
     * 1개 또는 인식 불가 시 1을 반환한다.
     */
    private int extractBundleQuantity(String title) {
        if (title == null || title.isEmpty()) return 1;

        // 패턴 목록 (우선순위 높은 것부터)
        Pattern[] patterns = {
                Pattern.compile("(\\d+)\\s*개입"),
                Pattern.compile("(\\d+)\\s*개\\s*세트"),
                Pattern.compile("(\\d+)\\s*개\\s*묶음"),
                Pattern.compile("(\\d+)\\s*팩"),
                Pattern.compile("(\\d+)\\s*[Pp](?![a-zA-Z])"),   // "3P" but not "3Plus"
                Pattern.compile("(\\d+)\\s*[Ee][Aa]"),             // "3EA"
                Pattern.compile("[xX](\\d+)(?![a-zA-Z])"),         // "x3", "X3"
                Pattern.compile("(\\d+)\\s*세트"),
                Pattern.compile("(\\d+)\\s*입"),
                Pattern.compile("(\\d+)\\s*매\\s*입"),
                Pattern.compile("(\\d+)\\s*매"),
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(title);
            if (m.find()) {
                try {
                    int qty = Integer.parseInt(m.group(1));
                    if (qty >= 2 && qty <= 100) {
                        return qty;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return 1;
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
