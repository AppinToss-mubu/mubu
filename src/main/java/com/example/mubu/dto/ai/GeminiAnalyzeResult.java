package com.example.mubu.dto.ai;

import com.example.mubu.dto.gemini.GeminiCandidate;
import com.example.mubu.dto.gemini.GeminiPart;
import com.example.mubu.dto.gemini.GeminiResponse;
import lombok.Getter;

// 우리 서비스에서 사용하는 AI 분석 결과 DTO
@Getter
public class GeminiAnalyzeResult {

    // 사용자에게 전달할 최종 분석 텍스트
    private final String text;

    private GeminiAnalyzeResult(String text) {
        this.text = text;
    }

    // Gemini API 응답을 서비스용 결과로 변환
    public static GeminiAnalyzeResult from(GeminiResponse response) {

        // 1. 응답 자체가 없거나 candidate가 없는 경우
        if (response == null ||
                response.getCandidates() == null ||
                response.getCandidates().isEmpty()) {
            return new GeminiAnalyzeResult("분석 결과 없음");
        }

        // Gemini는 여러 candidate를 줄 수 있으나,
        // 현재 서비스에서는 첫 번째 응답만 사용
        GeminiCandidate candidate = response.getCandidates().get(0);

        // 2. 모델 응답 content 또는 parts가 없는 경우
        if (candidate.getContent() == null ||
                candidate.getContent().getParts() == null) {
            return new GeminiAnalyzeResult("분석 결과 없음");
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
            return new GeminiAnalyzeResult("분석 결과 없음");
        }

        return new GeminiAnalyzeResult(result.toString());
    }
}
