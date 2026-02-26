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
        // - v2: searchKeywordKr 작성 규칙 강화, 다양한 카테고리 예시 추가
        String prompt = """
        이 이미지에 나온 상품을 분석해줘.
        응답 형식 (반드시 이 형식으로만 답변):
        상품명: [브랜드 + 제품명]
        가격: [숫자] [통화코드]
        searchKeywordKr: [한국 네이버 쇼핑에서 검색할 최적 한국어 키워드]

        searchKeywordKr 작성 규칙:
        - 한국에서 통용되는 브랜드명(한글 음차 또는 영문) + 제품 유형 조합
        - 2~4 단어가 적당. 용량/맛/색상 등 세부 옵션은 제외
        - 한국에서 잘 알려진 브랜드는 영문 그대로 가능 (예: "Dove 비누")
        - 한국에 없는 현지 브랜드는 한글 음차 + 제품 유형 (예: "달리 치약")
        - 한국에서 같은 제품이 다른 이름으로 팔리면 한국식 이름 사용

        예시1:
        상품명: Colgate Triple Action Toothpaste
        가격: 45 THB
        searchKeywordKr: 콜게이트 치약

        예시2:
        상품명: Pocky Chocolate Sticks
        가격: 35 THB
        searchKeywordKr: 포키 초콜릿

        예시3:
        상품명: Biore UV Aqua Rich Watery Essence
        가격: 780 JPY
        searchKeywordKr: 비오레 선크림

        예시4:
        상품명: Nintendo Switch Pro Controller
        가격: 69.99 USD
        searchKeywordKr: 닌텐도 스위치 프로컨트롤러

        예시5:
        상품명: Lays Classic Potato Chips
        가격: 30 THB
        searchKeywordKr: 레이즈 감자칩

        주의사항:
        - 상품명은 영문 그대로 유지하거나 한국어 번역
        - searchKeywordKr은 반드시 한국어로 작성
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
