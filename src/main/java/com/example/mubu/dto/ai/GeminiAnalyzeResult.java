package com.example.mubu.dto.ai;

import com.example.mubu.dto.gemini.GeminiCandidate;
import com.example.mubu.dto.gemini.GeminiPart;
import com.example.mubu.dto.gemini.GeminiResponse;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 우리 서비스에서 사용하는 AI 분석 결과 DTO
@Getter
public class GeminiAnalyzeResult {

    // 사용자에게 전달할 최종 분석 텍스트 (상품명 한 줄)
    private final String text;

    // AI가 텍스트에서 함께 인식한 현지 가격/통화 정보(있을 때만 사용)
    // - 예: "27 THB", "1,200 JPY" 등에서 파싱
    private final Integer localPrice;
    private final String localCurrency;

    private GeminiAnalyzeResult(String text, Integer localPrice, String localCurrency) {
        this.text = text;
        this.localPrice = localPrice;
        this.localCurrency = localCurrency;
    }

    // Gemini API 응답을 서비스용 결과로 변환
    public static GeminiAnalyzeResult from(GeminiResponse response) {

        // 1. 응답 자체가 없거나 candidate가 없는 경우
        if (response == null ||
                response.getCandidates() == null ||
                response.getCandidates().isEmpty()) {
            return new GeminiAnalyzeResult("분석 결과 없음", null, null);
        }

        // Gemini는 여러 candidate를 줄 수 있으나,
        // 현재 서비스에서는 첫 번째 응답만 사용
        GeminiCandidate candidate = response.getCandidates().get(0);

        // 2. 모델 응답 content 또는 parts가 없는 경우
        if (candidate.getContent() == null ||
                candidate.getContent().getParts() == null) {
            return new GeminiAnalyzeResult("분석 결과 없음", null, null);
        }

        StringBuilder result = new StringBuilder();

        // 3. Gemini 응답은 text가 여러 part로 나뉘어 올 수 있으므로
        // 모든 text part를 순회하며 하나의 문자열로 결합
        for (GeminiPart part : candidate.getContent().getParts()) {
            if (part != null && part.getText() != null) {
                result.append(part.getText());
            }
        }

        // 4. text part가 하나도 없었던 경우에 대한 방어 처리
        if (result.length() == 0) {
            return new GeminiAnalyzeResult("분석 결과 없음", null, null);
        }

        String text = result.toString();

        // 5. 텍스트 안에서 "숫자 + 통화 코드" 패턴을 간단히 파싱
        //    - 예: "27 THB", "1,200 JPY"
        Pattern pattern = Pattern.compile(
                "(\\d[\\d,]*(?:\\.\\d+)?)\\s*(THB|JPY|USD|CNY|EUR|KRW|SGD|PHP|IDR|VND|HKD|TWD|AUD|CAD|GBP|CHF)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);

        Integer localPrice = null;
        String localCurrency = null;

        if (matcher.find()) {
            String amountText = matcher.group(1).replace(",", "");
            try {
                double parsed = Double.parseDouble(amountText);
                localPrice = (int) parsed;
                localCurrency = matcher.group(2).toUpperCase();
            } catch (NumberFormatException ignored) {
                // 가격 파싱에 실패한 경우에는 현지 가격 정보를 사용하지 않는다.
                localPrice = null;
                localCurrency = null;
            }
        }

        return new GeminiAnalyzeResult(text, localPrice, localCurrency);
    }
}
