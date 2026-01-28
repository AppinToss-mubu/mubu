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

    // 이미지 기반 상품명 + (가능하다면) 현지 가격/통화 정보 추출
    public GeminiAnalyzeResult analyze(
            byte[] imageBytes,
            String mimeType
    ) {

        // 가격 비교용 고정 프롬프트
        // - 구조화된 형식으로 응답하여 파싱 성공률 향상
        String prompt = """
        이 이미지에 나온 상품을 분석해줘.
        응답 형식 (반드시 이 형식으로만 답변):
        상품명: [브랜드 + 제품명, 검색에 적합한 간결한 한 줄]
        가격: [숫자] [통화코드]
        searchKeywordKr: [네이버 쇼핑 검색용 한국어 키워드]
        예시:
        상품명: 세븐일레븐 소시지치즈샌드위치
        가격: 27 THB
        searchKeywordKr: 세븐일레븐 소시지치즈샌드위치
        예시2:
        상품명: Darli Toothpaste
        가격: 45 THB
        searchKeywordKr: 달리 치약
        주의사항:
        - 상품명은 한국어로 번역하거나 영문 그대로 유지
        - searchKeywordKr은 반드시 한국어로 작성 (예: "달리 치약", "포키 과자")
        - 불필요한 설명 없이 위 형식만 출력
        - 가격이 보이지 않으면 "가격: 없음" 으로 표기
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
