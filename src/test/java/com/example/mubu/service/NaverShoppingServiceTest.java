package com.example.mubu.service;

import com.example.mubu.client.NaverShoppingClient;
import com.example.mubu.dto.naver.NaverShoppingItem;
import com.example.mubu.dto.naver.NaverShoppingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NaverShoppingServiceTest {

    @Mock
    private NaverShoppingClient naverShoppingClient;

    private NaverShoppingService naverShoppingService;

    @BeforeEach
    void setUp() {
        naverShoppingService = new NaverShoppingService(naverShoppingClient);
    }

    // --- 헬퍼 메서드 ---

    private NaverShoppingItem createItem(String title, String lprice, String mallName) {
        NaverShoppingItem item = new NaverShoppingItem();
        item.setTitle(title);
        item.setLprice(lprice);
        item.setMallName(mallName);
        item.setLink("https://example.com");
        item.setImage("https://example.com/img.jpg");
        return item;
    }

    private NaverShoppingResponse createResponse(List<NaverShoppingItem> items) {
        NaverShoppingResponse response = new NaverShoppingResponse();
        response.setItems(items);
        response.setTotal(items.size());
        return response;
    }

    // --- 키워드 매칭 점수 기반 선택 테스트 ---

    @Test
    @DisplayName("키워드 2개 모두 매칭된 상품이 1개만 매칭된 상품보다 우선")
    void shouldPreferItemWithMoreKeywordMatches() {
        // "콜게이트 치약" 검색 시:
        // - "콜게이트 트리플액션 치약" (2개 매칭, 5000원) ← 이걸 선택해야 함
        // - "콜게이트 칫솔" (1개 매칭, 3000원) ← 더 싸지만 관련성 낮음
        NaverShoppingItem bestMatch = createItem("콜게이트 트리플액션 치약 130g", "5000", "쿠팡");
        NaverShoppingItem cheapButWrong = createItem("콜게이트 칫솔 소프트", "3000", "11번가");

        when(naverShoppingClient.search(eq("콜게이트 치약"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(cheapButWrong, bestMatch)));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("콜게이트 치약");

        assertNotNull(result);
        assertTrue(result.getTitle().contains("치약"),
                "키워드 2개 매칭 상품이 선택되어야 함. 실제: " + result.getTitle());
    }

    @Test
    @DisplayName("같은 매칭 수일 때 최저가 선택")
    void shouldPickCheapestAmongSameMatchCount() {
        // 두 상품 모두 "포키" + "초콜릿" 매칭 → 더 싼 걸 선택
        NaverShoppingItem expensive = createItem("포키 초콜릿 더블팩", "3500", "쿠팡");
        NaverShoppingItem cheap = createItem("포키 초콜릿 오리지널", "1800", "11번가");

        when(naverShoppingClient.search(eq("포키 초콜릿"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(expensive, cheap)));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("포키 초콜릿");

        assertNotNull(result);
        assertEquals(1800, Integer.parseInt(result.getLprice()),
                "동일 매칭 수에서 최저가를 선택해야 함");
    }

    @Test
    @DisplayName("관련 없는 상품은 필터링")
    void shouldFilterOutIrrelevantItems() {
        // "비오레 선크림" 검색 → "아이폰 케이스"는 키워드 0개 매칭 → 제외
        NaverShoppingItem relevant = createItem("비오레 UV 아쿠아리치 선크림", "12000", "올리브영");
        NaverShoppingItem irrelevant = createItem("아이폰 15 케이스", "5000", "쿠팡");

        when(naverShoppingClient.search(eq("비오레 선크림"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(irrelevant, relevant)));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("비오레 선크림");

        assertNotNull(result);
        assertTrue(result.getTitle().contains("비오레"),
                "관련 없는 상품은 필터링되어야 함. 실제: " + result.getTitle());
    }

    @Test
    @DisplayName("검색 결과가 비어있으면 null 반환")
    void shouldReturnNullWhenNoResults() {
        when(naverShoppingClient.search(anyString(), anyInt(), anyString()))
                .thenReturn(createResponse(List.of()));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("존재하지않는상품");

        assertNull(result);
    }

    @Test
    @DisplayName("가격이 0이거나 null인 상품 제외")
    void shouldExcludeItemsWithZeroOrNullPrice() {
        NaverShoppingItem noPrice = createItem("콜게이트 치약", "0", "쿠팡");
        NaverShoppingItem nullPrice = createItem("콜게이트 치약 대용량", null, "11번가");
        NaverShoppingItem validPrice = createItem("콜게이트 치약 레귤러", "4500", "옥션");

        when(naverShoppingClient.search(eq("콜게이트 치약"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(noPrice, nullPrice, validPrice)));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("콜게이트 치약");

        assertNotNull(result);
        assertEquals(4500, Integer.parseInt(result.getLprice()));
    }

    // --- 묶음 상품 단가 계산 테스트 ---

    @Test
    @DisplayName("묶음 상품(3개입)이면 단가로 환산")
    void shouldCalculateUnitPriceForBundle() {
        NaverShoppingItem bundle = createItem("콜게이트 치약 3개입", "9000", "쿠팡");

        when(naverShoppingClient.search(eq("콜게이트 치약"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(bundle)));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("콜게이트 치약");

        assertNotNull(result);
        assertEquals(3000, Integer.parseInt(result.getLprice()),
                "3개입 9000원 → 단가 3000원이어야 함");
    }

    @Test
    @DisplayName("묶음 패턴 인식: 개세트, 팩, P, EA")
    void shouldDetectVariousBundlePatterns() {
        // 각각 다른 묶음 패턴으로 테스트
        String[][] testCases = {
                {"포키 초콜릿 2개세트", "4000", "2000"},
                {"포키 초콜릿 5팩", "10000", "2000"},
                {"포키 초콜릿 4P", "8000", "2000"},
                {"포키 초콜릿 3EA", "6000", "2000"},
        };

        for (String[] tc : testCases) {
            NaverShoppingItem item = createItem(tc[0], tc[1], "쿠팡");

            when(naverShoppingClient.search(eq("포키 초콜릿"), anyInt(), anyString()))
                    .thenReturn(createResponse(List.of(item)));

            NaverShoppingItem result = naverShoppingService.findLowestPriceItem("포키 초콜릿");

            assertNotNull(result, "결과가 null: " + tc[0]);
            assertEquals(Integer.parseInt(tc[2]), Integer.parseInt(result.getLprice()),
                    "묶음 단가 계산 실패: " + tc[0]);
        }
    }

    // --- Fallback 검색 테스트 ---

    @Test
    @DisplayName("1차 검색 실패 시 2단어로 재시도")
    void shouldRetryWithTwoWordsOnFirstFailure() {
        // "닌텐도 스위치 프로컨트롤러" → 결과 없음
        // "닌텐도 스위치" → 결과 있음
        when(naverShoppingClient.search(eq("닌텐도 스위치 프로컨트롤러"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of()));
        when(naverShoppingClient.search(eq("닌텐도 스위치"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(
                        createItem("닌텐도 스위치 프로 컨트롤러", "55000", "쿠팡")
                )));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("닌텐도 스위치 프로컨트롤러");

        assertNotNull(result);
        assertTrue(result.getTitle().contains("닌텐도"));
    }

    @Test
    @DisplayName("3차 영문 Fallback 검색")
    void shouldFallbackToEnglishBrand() {
        // 한국어 검색 모두 실패 (1차 + 2차 동일 쿼리) → 영문 브랜드명으로 재시도
        when(naverShoppingClient.search(eq("달리 치약"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of()));

        // extractEnglishWords("상품명: Darli Toothpaste\n가격: 45 THB") → "Darli Toothpaste"
        // getProductType("달리 치약") → "치약"
        // → fallback 검색어: "Darli Toothpaste 치약"
        when(naverShoppingClient.search(eq("Darli Toothpaste 치약"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(
                        createItem("Darli 치약 민트 100g", "3500", "11번가")
                )));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem(
                "달리 치약",
                "상품명: Darli Toothpaste\n가격: 45 THB"
        );

        assertNotNull(result);
        assertTrue(result.getTitle().contains("Darli"));
    }

    // --- HTML 태그 제거 테스트 ---

    @Test
    @DisplayName("상품명의 HTML 태그가 제거되어야 함")
    void shouldStripHtmlTags() {
        NaverShoppingItem item = createItem(
                "<b>콜게이트</b> 트리플액션 <b>치약</b>", "4500", "쿠팡"
        );

        when(naverShoppingClient.search(eq("콜게이트 치약"), anyInt(), anyString()))
                .thenReturn(createResponse(List.of(item)));

        NaverShoppingItem result = naverShoppingService.findLowestPriceItem("콜게이트 치약");

        assertNotNull(result);
        assertFalse(result.getTitle().contains("<b>"),
                "HTML 태그가 제거되어야 함. 실제: " + result.getTitle());
    }
}
