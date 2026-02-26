package com.example.mubu.dto.ai;

import com.example.mubu.dto.gemini.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiAnalyzeResultTest {

    // --- 헬퍼 메서드 ---

    private GeminiResponse createResponse(String text) {
        GeminiPart part = new GeminiPart();
        part.setText(text);

        GeminiContent content = new GeminiContent();
        content.setParts(List.of(part));

        GeminiCandidate candidate = new GeminiCandidate();
        candidate.setContent(content);

        GeminiResponse response = new GeminiResponse();
        response.setCandidates(List.of(candidate));

        return response;
    }

    // --- 프롬프트 응답 파싱 테스트 ---

    @Test
    @DisplayName("v2 프롬프트 정상 응답 파싱")
    void shouldParseV2PromptResponse() {
        String aiText = """
                상품명: Colgate Triple Action Toothpaste
                가격: 45 THB
                searchKeywordKr: 콜게이트 치약""";

        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

        assertNotNull(result);
        assertTrue(result.getText().contains("Colgate"));
        assertEquals(45, result.getLocalPrice());
        assertEquals("THB", result.getLocalCurrency());
        assertEquals("콜게이트 치약", result.getSearchKeywordKr());
    }

    @Test
    @DisplayName("일본엔 가격 파싱")
    void shouldParseJpyPrice() {
        String aiText = """
                상품명: Biore UV Aqua Rich Watery Essence
                가격: 780 JPY
                searchKeywordKr: 비오레 선크림""";

        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

        assertEquals(780, result.getLocalPrice());
        assertEquals("JPY", result.getLocalCurrency());
        assertEquals("비오레 선크림", result.getSearchKeywordKr());
    }

    @Test
    @DisplayName("콤마가 포함된 가격 파싱 (1,200 JPY)")
    void shouldParseCommaPrice() {
        String aiText = """
                상품명: Nintendo Switch Pro Controller
                가격: 7,980 JPY
                searchKeywordKr: 닌텐도 스위치 프로컨트롤러""";

        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

        assertEquals(7980, result.getLocalPrice());
        assertEquals("JPY", result.getLocalCurrency());
    }

    @Test
    @DisplayName("가격이 없음일 때 null")
    void shouldHandleNoPriceGracefully() {
        String aiText = """
                상품명: 알 수 없는 상품
                가격: 없음
                searchKeywordKr: 알수없는 상품""";

        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

        assertNotNull(result);
        assertNull(result.getLocalPrice());
        assertNull(result.getLocalCurrency());
        assertEquals("알수없는 상품", result.getSearchKeywordKr());
    }

    @Test
    @DisplayName("searchKeywordKr이 없는 응답")
    void shouldHandleMissingSearchKeyword() {
        String aiText = """
                상품명: Some Product
                가격: 100 THB""";

        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

        assertNotNull(result);
        assertEquals(100, result.getLocalPrice());
        assertNull(result.getSearchKeywordKr());
    }

    @Test
    @DisplayName("소수점 가격 파싱 (69.99 USD)")
    void shouldParseDecimalPrice() {
        String aiText = """
                상품명: Nintendo Switch Pro Controller
                가격: 69.99 USD
                searchKeywordKr: 닌텐도 스위치 프로컨트롤러""";

        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

        assertEquals(69, result.getLocalPrice()); // int 변환으로 소수점 버림
        assertEquals("USD", result.getLocalCurrency());
    }

    @Test
    @DisplayName("빈 응답 처리")
    void shouldHandleNullResponse() {
        GeminiAnalyzeResult result = GeminiAnalyzeResult.from(null);

        assertNotNull(result);
        assertEquals("분석 결과 없음", result.getText());
        assertNull(result.getLocalPrice());
    }

    @Test
    @DisplayName("12개 통화 코드 모두 인식")
    void shouldRecognizeAllSupportedCurrencies() {
        String[] currencies = {"THB", "JPY", "USD", "EUR", "CNY", "SGD", "VND", "PHP", "IDR", "HKD", "TWD", "AUD"};

        for (String currency : currencies) {
            String aiText = "상품명: Test\n가격: 100 " + currency + "\nsearchKeywordKr: 테스트";
            GeminiAnalyzeResult result = GeminiAnalyzeResult.from(createResponse(aiText));

            assertEquals(100, result.getLocalPrice(), "가격 파싱 실패: " + currency);
            assertEquals(currency, result.getLocalCurrency(), "통화 인식 실패: " + currency);
        }
    }
}
