package com.example.mubu.service;

import com.example.mubu.dto.ai.GeminiAnalyzeResult;
import com.example.mubu.dto.ai.PriceCompareResult;
import com.example.mubu.dto.naver.NaverShoppingItem;
import org.springframework.stereotype.Service;

// AI 분석 + 네이버 쇼핑 조합 전용
@Service
public class PriceCompareService {

    private final AiAnalyzeService aiAnalyzeService;
    private final NaverShoppingService naverShoppingService;

    public PriceCompareService(
            AiAnalyzeService aiAnalyzeService,
            NaverShoppingService naverShoppingService
    ) {
        this.aiAnalyzeService = aiAnalyzeService;
        this.naverShoppingService = naverShoppingService;
    }

    // 이미지 → 상품명 → 최저가
    public PriceCompareResult compare(
            byte[] imageBytes,
            String mimeType
    ) {

        // 1. AI로 상품명 추출
        GeminiAnalyzeResult aiResult =
                aiAnalyzeService.analyze(
                        imageBytes,
                        mimeType
                );

        if (aiResult == null || aiResult.getText() == null) {
            return null;
        }

        // 2. 네이버 쇼핑 최저가 조회
        NaverShoppingItem lowestItem =
                naverShoppingService.findLowestPriceItem(
                        aiResult.getText()
                );

        if (lowestItem == null) {
            return null;
        }

        // 3. 응답 조합
        return new PriceCompareResult(
                aiResult.getText(),
                lowestItem.getTitle(),
                Integer.parseInt(lowestItem.getLprice()),
                lowestItem.getMallName(),
                lowestItem.getLink(),
                lowestItem.getImage()
        );
    }
}
