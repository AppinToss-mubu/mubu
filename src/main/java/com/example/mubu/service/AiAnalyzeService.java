package com.example.mubu.service;

import com.example.mubu.client.GeminiClient;
import com.example.mubu.dto.ai.GeminiAnalyzeResult;
import com.example.mubu.dto.gemini.*;
import org.springframework.stereotype.Service;

import java.util.List;

// Gemini 기반 이미지 분석 서비스
@Service
public class AiAnalyzeService {

    private final GeminiClient geminiClient;
    private final GeminiRequestFactory geminiRequestFactory;

    public AiAnalyzeService(
            GeminiClient geminiClient,
            GeminiRequestFactory geminiRequestFactory
    ) {
        this.geminiClient = geminiClient;
        this.geminiRequestFactory = geminiRequestFactory;
    }

    // 이미지 기반 상품명 한 줄 추출
    public GeminiAnalyzeResult analyze(
            byte[] imageBytes,
            String mimeType
    ) {

        // 가격 비교용 고정 프롬프트
        String prompt = """
        이 이미지에 나온 상품의
        브랜드명과 정확한 상품명을
        검색에 적합한 한 줄로 알려줘
        """;

        // Gemini 요청 DTO 생성
        GeminiRequest request =
                geminiRequestFactory.create(
                        prompt,
                        imageBytes,
                        mimeType
                );

        // Gemini API 호출
        GeminiResponse response =
                geminiClient.generateContent(
                        "gemini-2.0-flash",
                        request
                );

        // 서비스용 결과로 변환
        return GeminiAnalyzeResult.from(response);
    }
}
